# tests/test_queue_manager.py
import unittest
from unittest.mock import MagicMock, patch, call
import time
import threading

from download_manager.queue_manager import QueueManager
from download_manager.downloader import Downloader # For spec and Downloader.num_workers


class TestQueueManager(unittest.TestCase):

    def setUp(self):
        self.mock_downloader = MagicMock(spec=Downloader)
        self.mock_downloader.num_workers = 2 # Simulate 2 worker slots

        # Store statuses that mock_downloader.get_status will return
        self.downloader_task_statuses = {}
        # Store generated downloader IDs to verify calls
        self.downloader_generated_ids = {}

        def mock_start_download_fn(url, filepath):
            # Generate a unique ID for the downloader based on URL/filepath for test predictability
            # This simulates Downloader generating its own ID.
            dl_id = f"dl_{os.path.basename(filepath).split('.')[0]}"
            self.downloader_task_statuses[dl_id] = {'status': 'downloading', 'progress': 0} # Initial status
            self.downloader_generated_ids[qm_id_map_key(url, filepath)] = dl_id # Store for later checks
            # print(f"DEBUG mock_start_download: url={url}, fp={filepath} -> dl_id={dl_id}")
            return dl_id

        def qm_id_map_key(url, filepath): # Helper to create a consistent key for mapping qm_id to dl_id
            return f"{url}|{filepath}"

        self.mock_downloader.start_download.side_effect = mock_start_download_fn

        def mock_get_status_fn(download_id):
            # print(f"DEBUG mock_get_status: dl_id={download_id}, returning {self.downloader_task_statuses.get(download_id)}")
            return self.downloader_task_statuses.get(download_id)

        self.mock_downloader.get_status.side_effect = mock_get_status_fn
        self.mock_downloader.cancel_download = MagicMock()

        self.queue_manager = QueueManager(downloader_instance=self.mock_downloader)
        # Do not start processing here, start it in tests where needed to control timing.

    def tearDown(self):
        self.queue_manager.stop_processing()
        # Explicitly join the thread if it's alive
        if self.queue_manager.processing_thread and self.queue_manager.processing_thread.is_alive():
            self.queue_manager.processing_thread.join(timeout=1.0) # Increased timeout

    def test_add_download_to_queue_sorted_by_priority(self):
        self.queue_manager.start_processing() # Start for this test
        qm_id1 = self.queue_manager.add_download("http://example.com/file1.zip", "file1.zip", priority=1)
        qm_id0 = self.queue_manager.add_download("http://example.com/file0.zip", "file0.zip", priority=0)

        status = self.queue_manager.get_queue_status()
        self.assertEqual(status['pending_tasks_count'], 2)
        # QM sorts pending_queue internally. Lower number = higher priority.
        self.assertEqual(status['pending_tasks'][0]['qm_id'], qm_id0)
        self.assertEqual(status['pending_tasks'][1]['qm_id'], qm_id1)

    def test_process_queue_dispatches_tasks_respecting_num_workers(self):
        self.queue_manager.start_processing()
        qm_id1 = self.queue_manager.add_download("http://example.com/fileA.zip", "d/fileA.zip", priority=0)
        qm_id2 = self.queue_manager.add_download("http://example.com/fileB.zip", "d/fileB.zip", priority=0)
        qm_id3 = self.queue_manager.add_download("http://example.com/fileC.zip", "d/fileC.zip", priority=0)

        time.sleep(0.1) # Time for QM to process initial queue

        # Downloader should have been called for the first two tasks (num_workers = 2)
        self.assertEqual(self.mock_downloader.start_download.call_count, 2)
        self.mock_downloader.start_download.assert_any_call("http://example.com/fileA.zip", "d/fileA.zip")
        self.mock_downloader.start_download.assert_any_call("http://example.com/fileB.zip", "d/fileB.zip")

        status_after_dispatch = self.queue_manager.get_queue_status()
        self.assertEqual(status_after_dispatch['active_tasks_count'], 2)
        self.assertEqual(status_after_dispatch['pending_tasks_count'], 1)
        self.assertEqual(status_after_dispatch['pending_tasks'][0]['qm_id'], qm_id3)

    def test_process_queue_handles_completed_downloads_and_dispatches_next(self):
        self.queue_manager.start_processing()
        # Add two tasks, first one will complete
        qm_id_A = self.queue_manager.add_download("http://example.com/fileD.zip", "d/fileD.zip")
        qm_id_B = self.queue_manager.add_download("http://example.com/fileE.zip", "d/fileE.zip") # This will be pending

        time.sleep(0.1) # Allow QM to dispatch fileD (and fileE if num_workers=2)

        # Get the downloader ID assigned to fileD.zip
        downloader_id_D = self.downloader_generated_ids.get(qm_id_map_key("http://example.com/fileD.zip", "d/fileD.zip"))
        self.assertIsNotNone(downloader_id_D, "Downloader ID for fileD should have been captured.")

        # Simulate fileD.zip completing
        self.downloader_task_statuses[downloader_id_D] = {'status': 'completed', 'progress': 100}

        # QM's loop sleeps for 1 second. Wait for it to process the completion and dispatch next.
        time.sleep(1.1)

        status_after_completion = self.queue_manager.get_queue_status()
        # fileD should be gone from active tasks
        # fileE should now be active (or was already active if num_workers >= 2)
        active_qm_ids = [task['qm_id'] for task in status_after_completion['active_tasks']]
        self.assertNotIn(qm_id_A, active_qm_ids, "Completed task A should not be active.")

        if self.mock_downloader.num_workers == 1: # If only 1 worker, B should now be active
            self.assertIn(qm_id_B, active_qm_ids, "Task B should become active after A completes (1 worker).")
            self.assertEqual(status_after_completion['active_tasks_count'], 1)
            self.assertEqual(status_after_completion['pending_tasks_count'], 0)
            self.mock_downloader.start_download.assert_any_call("http://example.com/fileE.zip", "d/fileE.zip")
        elif self.mock_downloader.num_workers >=2: # If 2+ workers, B was already active
             self.assertIn(qm_id_B, active_qm_ids, "Task B should be active (2+ workers).")
             self.assertEqual(status_after_completion['active_tasks_count'], 1) # Only B remains active

        # Ensure fileD was called, and fileE was also called eventually
        self.mock_downloader.start_download.assert_any_call("http://example.com/fileD.zip", "d/fileD.zip")
        self.mock_downloader.start_download.assert_any_call("http://example.com/fileE.zip", "d/fileE.zip")


    def test_remove_from_pending_queue(self):
        self.queue_manager.start_processing()
        qm_id1 = self.queue_manager.add_download("http://example.com/rem1.zip", "d/rem1.zip")
        qm_id2 = self.queue_manager.add_download("http://example.com/rem2.zip", "d/rem2.zip")

        self.assertTrue(self.queue_manager.remove_download(qm_id1))
        status = self.queue_manager.get_queue_status()
        self.assertEqual(status['pending_tasks_count'], 1)
        self.assertEqual(status['pending_tasks'][0]['qm_id'], qm_id2)

        self.assertFalse(self.queue_manager.remove_download("non_existent_qm_id"))

    def test_remove_active_download_calls_downloader_cancel(self):
        self.queue_manager.start_processing()
        qm_id_active = self.queue_manager.add_download("http://example.com/active_rem.zip", "d/active_rem.zip")

        time.sleep(0.1) # Allow task to become active

        downloader_id_active = self.downloader_generated_ids.get(qm_id_map_key("http://example.com/active_rem.zip", "d/active_rem.zip"))
        self.assertIsNotNone(downloader_id_active, "Downloader ID for active_rem should be known.")

        # Ensure it's in QM's active_downloads by checking status (indirectly)
        # QM's active_downloads maps: downloader_id -> task_details_dict (which contains 'qm_id')
        current_qm_status = self.queue_manager.get_queue_status()
        found_in_active = False
        for task in current_qm_status['active_tasks']:
            if task['qm_id'] == qm_id_active and task['downloader_id'] == downloader_id_active:
                found_in_active = True
                break
        self.assertTrue(found_in_active, "Task should be in active downloads list of QM.")

        # Now, try to remove it using its QM ID
        self.assertTrue(self.queue_manager.remove_download(qm_id_active))

        # Verify that downloader.cancel_download was called with the correct DOWNLOADER ID
        self.mock_downloader.cancel_download.assert_called_once_with(downloader_id_active)


    def test_get_task_status_pending_active_and_not_found(self):
        self.queue_manager.start_processing()
        qm_id_pending = self.queue_manager.add_download("http://example.com/pending.file", "d/pending.file", priority=10) # Low priority
        qm_id_active = self.queue_manager.add_download("http://example.com/active.file", "d/active.file", priority=0) # High priority

        time.sleep(0.1) # Let active.file be dispatched

        downloader_id_active = self.downloader_generated_ids.get(qm_id_map_key("http://example.com/active.file", "d/active.file"))
        self.assertIsNotNone(downloader_id_active)
        self.downloader_task_statuses[downloader_id_active] = {'status': 'downloading', 'progress': 50, 'error': None}


        status_pending = self.queue_manager.get_task_status(qm_id_pending)
        self.assertEqual(status_pending['status'], 'queued')

        status_active = self.queue_manager.get_task_status(qm_id_active)
        self.assertEqual(status_active['downloader_id'], downloader_id_active)
        self.assertEqual(status_active['status'], 'processing') # QM's view of status
        self.assertEqual(status_active['downloader_actual_status'], 'downloading')
        self.assertEqual(status_active['live_status']['progress'], 50)

        status_not_found = self.queue_manager.get_task_status("non_existent_id")
        self.assertEqual(status_not_found['status'], 'not_found')

if __name__ == '__main__':
    unittest.main()

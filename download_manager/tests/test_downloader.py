# tests/test_downloader.py
import unittest
from unittest.mock import patch, MagicMock, mock_open, call
import os
import time
import threading
import shutil # For robust teardown
import requests # For requests.exceptions.RequestException

from download_manager.downloader import Downloader
from download_manager.storage_optimizer import StorageOptimizer


class TestDownloader(unittest.TestCase):

    def setUp(self):
        self.test_base_dir = "temp_downloader_tests" # Base dir for all test files/dirs
        self.test_downloads_dir = os.path.join(self.test_base_dir, "downloader_files")

        # Clean up before each test
        if os.path.exists(self.test_base_dir):
            shutil.rmtree(self.test_base_dir)
        os.makedirs(self.test_downloads_dir, exist_ok=True)

        self.downloader = Downloader(num_workers=1)
        # Mock storage optimizer for most tests to isolate downloader logic
        # The actual StorageOptimizer is tested in its own file.
        # Here, we just want to ensure Downloader calls it when expected.
        self.mock_storage_optimizer_instance = MagicMock(spec=StorageOptimizer)
        self.downloader.storage_optimizer = self.mock_storage_optimizer_instance

    def tearDown(self):
        # Ensure all threads from Downloader are joined if any are still alive.
        # This is important to prevent threads from one test affecting another.
        with self.downloader.lock: # Access download_tasks safely
            for task_id, task_details in list(self.downloader.download_tasks.items()):
                thread = task_details.get('thread')
                if thread and thread.is_alive():
                    thread.join(timeout=0.5) # Give thread a chance to finish
                    if thread.is_alive():
                        print(f"Warning: Thread for task {task_id} did not finish in teardown.")
                # Clean up task entry to prevent interference if downloader instance were reused (it's not here)
                # del self.downloader.download_tasks[task_id]


        # Clean up test files/dirs created during tests
        if os.path.exists(self.test_base_dir):
            shutil.rmtree(self.test_base_dir)


    @patch('requests.get')
    # We also want to patch os.makedirs from the downloader module perspective if it's called there
    @patch('download_manager.downloader.os.makedirs')
    def test_start_download_success(self, mock_os_makedirs, mock_requests_get):
        mock_response = MagicMock()
        mock_response.headers = {'content-length': '100'}
        mock_response.iter_content.return_value = [b"chunk1", b"chunk2"] # Total 12 bytes
        mock_response.raise_for_status = MagicMock() # Does nothing, i.e. status is OK
        mock_requests_get.return_value = mock_response

        url = "http://example.com/file.txt"
        filepath = os.path.join(self.test_downloads_dir, "file.txt")

        # Use mock_open for the file operations within _download_file
        m_open = mock_open()
        with patch('builtins.open', m_open):
            download_id = self.downloader.start_download(url, filepath)
            self.assertIsNotNone(download_id)

            # Wait for the thread to complete its work.
            # A more robust way would be an event or callback, but time.sleep is simpler for testing.
            thread_to_join = None
            with self.downloader.lock: # Access tasks safely
                if download_id in self.downloader.download_tasks:
                    thread_to_join = self.downloader.download_tasks[download_id].get('thread')

            if thread_to_join:
                thread_to_join.join(timeout=1.0) # Wait up to 1 sec

            status = self.downloader.get_status(download_id)
            self.assertIsNotNone(status, "Status should not be None after download attempt.")
            self.assertEqual(status['status'], 'completed', f"Status error: {status.get('error')}")
            self.assertEqual(status['progress'], 100.0)
            self.assertEqual(status['bytes_downloaded'], 12) # len(b"chunk1") + len(b"chunk2")
            self.assertEqual(status['total_size'], 100) # From content-length header

        # Check if os.makedirs was called for the directory of filepath
        # os.path.dirname(filepath) will give the correct path for the assertion
        expected_dir_path = os.path.dirname(filepath)
        if expected_dir_path: # Only called if there's a directory part
            mock_os_makedirs.assert_called_with(expected_dir_path, exist_ok=True)
        else: # If filepath is just a filename, makedirs might not be called or called with CWD/empty.
              # Current Downloader._download_file logic: if file_dir: os.makedirs(...)
            mock_os_makedirs.assert_not_called()


        m_open.assert_called_once_with(filepath, 'wb')
        handle = m_open() # Get the mock file handle
        handle.write.assert_any_call(b"chunk1")
        handle.write.assert_any_call(b"chunk2")
        self.assertEqual(handle.write.call_count, 2)


    @patch('requests.get')
    @patch('download_manager.downloader.os.makedirs') # Mock os.makedirs
    def test_download_failure_request_exception(self, mock_os_makedirs, mock_requests_get):
        mock_requests_get.side_effect = requests.exceptions.RequestException("Network error")

        url = "http://example.com/file_error.txt"
        filepath = os.path.join(self.test_downloads_dir, "file_error.txt")

        download_id = self.downloader.start_download(url, filepath)

        thread_to_join = None
        with self.downloader.lock:
            if download_id in self.downloader.download_tasks:
                thread_to_join = self.downloader.download_tasks[download_id].get('thread')
        if thread_to_join:
            thread_to_join.join(timeout=1.0)

        status = self.downloader.get_status(download_id)
        self.assertEqual(status['status'], 'failed')
        self.assertIn("Network error", status['error'])
        self.mock_storage_optimizer_instance.cleanup_incomplete_download.assert_called_once_with(filepath)


    @patch('requests.get')
    @patch('download_manager.downloader.os.makedirs')
    def test_download_cancellation(self, mock_os_makedirs, mock_requests_get):
        mock_response = MagicMock()
        mock_response.headers = {'content-length': '10000'}

        # Simulate a download that provides chunks slowly
        chunks = [b"chunk1_data", b"chunk2_data", b"chunk3_data"]
        iter_count = 0
        def slow_iter_content(chunk_size):
            nonlocal iter_count
            if iter_count < len(chunks):
                # print(f"DEBUG: Yielding chunk {iter_count}")
                yield chunks[iter_count]
                iter_count += 1
                time.sleep(0.1) # Sleep to simulate time taken to get next chunk
            else:
                # print("DEBUG: No more chunks")
                yield None # End of stream

        mock_response.iter_content = MagicMock(side_effect=slow_iter_content)
        mock_response.raise_for_status = MagicMock()
        mock_requests_get.return_value = mock_response

        url = "http://example.com/cancellable.txt"
        filepath = os.path.join(self.test_downloads_dir, "cancellable.txt")

        m_open = mock_open() # Mock file opening
        with patch('builtins.open', m_open):
            download_id = self.downloader.start_download(url, filepath)
            self.assertIsNotNone(download_id)

            time.sleep(0.05) # Allow download to start and be in the loop

            self.downloader.cancel_download(download_id) # Request cancellation

            thread_to_join = None
            with self.downloader.lock:
                 if download_id in self.downloader.download_tasks:
                    thread_to_join = self.downloader.download_tasks[download_id].get('thread')
            if thread_to_join:
                thread_to_join.join(timeout=1.0) # Wait for thread to process cancellation & exit

            status = self.downloader.get_status(download_id)
            self.assertIsNotNone(status)
            self.assertEqual(status['status'], 'cancelled', f"Status error: {status.get('error')}")

        # Check if cleanup was called
        self.mock_storage_optimizer_instance.cleanup_incomplete_download.assert_called_with(filepath)


    def test_pause_download_status_change(self):
        download_id = "test_dl_id_pause"
        # Manually set up a task to simulate it's downloading state
        # Thread mock is needed if pause logic tries to interact with it. Current logic doesn't.
        mock_thread = MagicMock(spec=threading.Thread)
        with self.downloader.lock:
            self.downloader.download_tasks[download_id] = {'status': 'downloading', 'thread': mock_thread}

        self.downloader.pause_download(download_id)

        with self.downloader.lock:
            self.assertEqual(self.downloader.download_tasks[download_id]['status'], 'paused')

    def test_resume_download_status_change(self):
        download_id = "test_dl_id_resume"
        # Manually set up a task to simulate it's paused state
        with self.downloader.lock:
            self.downloader.download_tasks[download_id] = {
                'status': 'paused',
                'url': 'http://example.com/resume.file',
                'filepath': os.path.join(self.test_downloads_dir, 'resume.file'),
                'thread': None # Paused implies thread might be gone or idle
            }

        self.downloader.resume_download(download_id)

        with self.downloader.lock:
            # Current resume is a placeholder, changes status to 'pending' for QM
            self.assertEqual(self.downloader.download_tasks[download_id]['status'], 'pending')

if __name__ == '__main__':
    unittest.main()

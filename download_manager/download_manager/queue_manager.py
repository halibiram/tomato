import collections
import threading
import time
import uuid # For generating download IDs for QM internal tracking if needed, but Downloader provides the main ID.
# from .downloader import Downloader # This will be used by the calling code, not directly imported for type hinting here unless Python 3.9+

class QueueManager:
    def __init__(self, downloader_instance):
        self.downloader = downloader_instance
        # pending_queue stores tasks as dictionaries:
        # {'qm_id': qm_id, 'url': url, 'filepath': filepath, 'priority': priority, 'time_added': time.time(), 'status': 'queued'}
        self.pending_queue = []
        # active_downloads maps downloader_id to task details from pending_queue
        # {downloader_id: {'qm_id': qm_id, 'url': url, ... , 'status': 'processing'}}
        self.active_downloads = {}
        self.queue_lock = threading.Lock()
        self.processing_thread = None # Initialized in start_processing
        self.stop_event = threading.Event()
        self._qm_id_counter = 0 # Simple counter for unique QM internal IDs

    def _generate_qm_id(self):
        self._qm_id_counter += 1
        return f"qm_{self._qm_id_counter}"

    def add_download(self, url, filepath, priority=0):
        with self.queue_lock:
            qm_id = self._generate_qm_id()
            task_details = {
                'qm_id': qm_id,
                'url': url,
                'filepath': filepath,
                'priority': priority,
                'time_added': time.time(),
                'status': 'queued' # Status within QueueManager
            }
            self.pending_queue.append(task_details)
            # Sort by priority (lower first), then by time_added (earlier first)
            self.pending_queue.sort(key=lambda x: (x['priority'], x['time_added']))
            print(f"QueueManager: Added to queue (QM ID: {qm_id}): {url}")
            return qm_id # Return QM's internal ID for this request

    def remove_download(self, qm_id_to_remove):
        with self.queue_lock:
            # Check pending queue first
            initial_len = len(self.pending_queue)
            self.pending_queue = [task for task in self.pending_queue if task['qm_id'] != qm_id_to_remove]
            if len(self.pending_queue) != initial_len:
                print(f"QueueManager: Removed QM ID {qm_id_to_remove} from pending queue.")
                return True

            # If not in pending, check if it's active and try to cancel via downloader
            # We need to find the downloader_id associated with this qm_id
            downloader_id_to_cancel = None
            for dl_id, task_details in self.active_downloads.items():
                if task_details['qm_id'] == qm_id_to_remove:
                    downloader_id_to_cancel = dl_id
                    break

            if downloader_id_to_cancel:
                print(f"QueueManager: Requesting cancellation for active download (Downloader ID: {downloader_id_to_cancel}, QM ID: {qm_id_to_remove}).")
                self.downloader.cancel_download(downloader_id_to_cancel)
                # The _process_queue_loop will handle removal from active_downloads based on downloader status update.
                return True

            print(f"QueueManager: QM ID {qm_id_to_remove} not found in pending or active for removal.")
            return False

    def _process_queue_loop(self):
        print("QueueManager: Processing loop started.")
        while not self.stop_event.is_set():
            with self.queue_lock:
                # --- Update status of active downloads ---
                completed_or_failed_downloader_ids = []
                for downloader_id, task_details in list(self.active_downloads.items()):
                    status_info = self.downloader.get_status(downloader_id) # This is downloader_id
                    if status_info:
                        current_downloader_status = status_info.get('status')
                        # Update the status in QueueManager's view of the active task
                        self.active_downloads[downloader_id]['status'] = current_downloader_status
                        if current_downloader_status in ['completed', 'failed', 'cancelled']:
                            print(f"QueueManager: Download (Downloader ID: {downloader_id}, QM ID: {task_details['qm_id']}) finished/stopped with status: {current_downloader_status}.")
                            completed_or_failed_downloader_ids.append(downloader_id)
                            if current_downloader_status == 'failed':
                                print(f"QueueManager: Error for {downloader_id}: {status_info.get('error')}")
                    else:
                        # Task might have been removed from downloader unexpectedly or ID is stale
                        print(f"QueueManager: Warning - could not get status for active Downloader ID {downloader_id}. Assuming it's gone.")
                        completed_or_failed_downloader_ids.append(downloader_id)


                for dl_id in completed_or_failed_downloader_ids:
                    if dl_id in self.active_downloads:
                        del self.active_downloads[dl_id]
                        print(f"QueueManager: Removed Downloader ID {dl_id} from active_downloads.")

                # --- Dispatch new tasks if capacity allows ---
                if self.pending_queue:
                    if len(self.active_downloads) < self.downloader.num_workers:
                        task_to_dispatch = self.pending_queue.pop(0) # Highest priority due to sort

                        qm_id = task_to_dispatch['qm_id']
                        url = task_to_dispatch['url']
                        filepath = task_to_dispatch['filepath']

                        print(f"QueueManager: Attempting to dispatch task (QM ID: {qm_id}) to Downloader: {url}")
                        # Downloader's start_download returns its own download_id
                        downloader_id = self.downloader.start_download(url, filepath)

                        if downloader_id:
                            # Store the task with the downloader_id as key
                            task_to_dispatch['status'] = 'processing' # Update QM status
                            self.active_downloads[downloader_id] = task_to_dispatch
                            print(f"QueueManager: Dispatched to downloader (Downloader ID: {downloader_id}, QM ID: {qm_id}). Active: {len(self.active_downloads)}")
                        else:
                            # Downloader did not start the task (e.g., immediate validation error, though current Downloader doesn't do this)
                            print(f"QueueManager: Downloader did not start task (QM ID: {qm_id}). Re-queueing.")
                            task_to_dispatch['status'] = 'queued' # Reset status
                            self.pending_queue.insert(0, task_to_dispatch) # Add back to front (or sort again)
                            self.pending_queue.sort(key=lambda x: (x['priority'], x['time_added']))
                    # else:
                    #     print(f"QueueManager: Downloader at capacity ({len(self.active_downloads)}/{self.downloader.num_workers}). Waiting.")
                # else:
                #     print("QueueManager: No tasks in pending_queue.")

            time.sleep(1) # Interval for checking queue and statuses
        print("QueueManager: Processing loop stopped.")

    def start_processing(self):
        if self.processing_thread and self.processing_thread.is_alive():
            print("QueueManager: Processing thread already running.")
            return

        self.stop_event.clear()
        self.processing_thread = threading.Thread(target=self._process_queue_loop, daemon=True)
        self.processing_thread.start()
        print("QueueManager: Started processing queue.")

    def stop_processing(self):
        print("QueueManager: Attempting to stop queue processing...")
        self.stop_event.set()
        if self.processing_thread and self.processing_thread.is_alive():
            self.processing_thread.join(timeout=5) # Wait for thread to finish
            if self.processing_thread.is_alive():
                print("QueueManager: Warning - processing thread did not stop in time.")
        print("QueueManager: Queue processing stopped.")

    def get_queue_status(self):
        with self.queue_lock:
            pending = [{k: v for k, v in task.items()} for task in self.pending_queue] # Deep copy for safety
            active = []
            for downloader_id, task_details_from_qm in self.active_downloads.items():
                live_status_from_downloader = self.downloader.get_status(downloader_id)

                # Merge QM's known details with live details from Downloader
                # task_details_from_qm is the original dict from pending_queue
                # (qm_id, url, filepath, priority, time_added, status from QM's perspective)
                # live_status_from_downloader is dict from Downloader
                # (status, progress, bytes_downloaded, total_size etc)

                # Create a new dictionary for merged_details to ensure no side effects
                merged_details = {**task_details_from_qm} # Start with a copy of QM's details
                merged_details['downloader_id'] = downloader_id
                if live_status_from_downloader:
                    merged_details['live_status'] = live_status_from_downloader
                else:
                    # If downloader.get_status returns None (e.g., task just finished and removed from Downloader's tasks)
                    # We might want to reflect that the status is now effectively unknown from Downloader's perspective
                    # or use the last known status from QM. For now, mark as unknown/stale.
                    merged_details['live_status'] = {'status': 'unknown_at_downloader', 'progress': task_details_from_qm.get('progress', 0)}

                active.append(merged_details)

            return {
                "pending_tasks_count": len(self.pending_queue), # Keep counts for quick overview
                "active_tasks_count": len(self.active_downloads),
                "pending_tasks": pending, # This is the list of pending task dicts
                "active_tasks": active    # This is the list of merged_details dicts for active tasks
            }

    def get_task_status(self, qm_id):
        with self.queue_lock:
            # Check pending queue
            for task in self.pending_queue:
                if task['qm_id'] == qm_id:
                    return {"qm_id": qm_id, "status": task['status'], "details": "Pending in queue"}

            # Check active downloads
            for downloader_id, task_details in self.active_downloads.items():
                if task_details['qm_id'] == qm_id:
                    downloader_status = self.downloader.get_status(downloader_id)
                    return {
                        "qm_id": qm_id,
                        "downloader_id": downloader_id,
                        "status": task_details.get('status', 'unknown'), # QM's view of status
                        "downloader_actual_status": downloader_status.get('status') if downloader_status else "unknown",
                        "error": downloader_status.get('error') if downloader_status else task_details.get('error'),
                        "details": "Active in downloader"
                    }
            return {"qm_id": qm_id, "status": "not_found", "details": "Task not found in pending or active."}

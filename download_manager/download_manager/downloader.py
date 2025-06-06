import threading
import requests
import uuid
import os
import time
from .storage_optimizer import StorageOptimizer

class Downloader:
    def __init__(self, num_workers=5):
        self.num_workers = num_workers
        self.download_tasks = {} # To store download task details
        self.lock = threading.Lock()
        self.storage_optimizer = StorageOptimizer()
        # TODO: Implement a queue to manage download requests if num_workers is exceeded
        # TODO: Implement worker threads

    def _download_file(self, url, filepath, download_id):
        bytes_downloaded = 0
        total_size = 0

        # Preliminary check for task validity before any I/O
        with self.lock:
            if download_id not in self.download_tasks:
                print(f"Download {download_id} task disappeared before I/O operations. Cancelling.")
                return # Task was removed or cancelled very early
            # Mark as downloading if it was pending (Downloader initiated)
            # If QueueManager is used, QM would set it to 'processing' or similar.
            # This internal status is mainly for Downloader's direct use if any.
            if self.download_tasks[download_id]['status'] == 'pending':
                 self.download_tasks[download_id]['status'] = 'downloading'


        try:
            file_dir = os.path.dirname(filepath)
            if file_dir: # Only create if filepath includes a directory path
                os.makedirs(file_dir, exist_ok=True)

            response = requests.get(url, stream=True)
            response.raise_for_status() # Raise an exception for bad status codes

            total_size = int(response.headers.get('content-length', 0))
            with self.lock:
                if download_id in self.download_tasks: # Check if task still valid
                    task = self.download_tasks[download_id]
                    if task['status'] == 'cancelling': # Check if cancelled while making dirs or getting headers
                        task['status'] = 'cancelled'
                        print(f"Download {download_id} cancelled before starting file write.")
                        self.storage_optimizer.cleanup_incomplete_download(filepath)
                        return
                    task['total_size'] = total_size
                    task['progress'] = 0.0
                    task['bytes_downloaded'] = 0
                    task['speed'] = 0
                else:
                    print(f"Download {download_id} not found in tasks before actual download loop. Possibly cancelled.")
                    return

            start_time = time.time()

            with open(filepath, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    with self.lock:
                        if download_id not in self.download_tasks: # Task removed externally
                            print(f"Download {download_id} task removed externally during download. Stopping.")
                            # Don't auto-cleanup here, might be intentional move/management
                            return
                        task = self.download_tasks[download_id]
                        if task['status'] == 'cancelling':
                            task['status'] = 'cancelled'
                            print(f"Download {download_id} cancelled during download loop.")
                            self.storage_optimizer.cleanup_incomplete_download(filepath)
                            return

                    if chunk:
                        f.write(chunk)
                        bytes_downloaded += len(chunk)
                        with self.lock:
                            if download_id in self.download_tasks:
                                task = self.download_tasks[download_id]
                                # Re-check status, as it could change while f.write() was happening (though unlikely for cancelling)
                                if task['status'] == 'cancelling':
                                    task['status'] = 'cancelled'
                                    print(f"Download {download_id} cancelled during write operation.")
                                    self.storage_optimizer.cleanup_incomplete_download(filepath)
                                    return # Exit loop & function

                                task['bytes_downloaded'] = bytes_downloaded
                                if total_size > 0:
                                    percentage = (bytes_downloaded / total_size) * 100
                                    task['progress'] = percentage
                                # elapsed_time = time.time() - start_time
                                # if elapsed_time > 0:
                                #    task['speed'] = bytes_downloaded / elapsed_time

            # If loop completes without returning early (i.e., not cancelled)
            with self.lock:
                if download_id in self.download_tasks:
                    task = self.download_tasks[download_id]
                    if task['status'] == 'downloading': # Ensure it wasn't changed to 'cancelling' just before this lock
                        if total_size > 0 and bytes_downloaded == total_size:
                            task['progress'] = 100.0
                        task['status'] = 'completed'
                        print(f"Download {download_id} completed: {filepath}")
                    elif task['status'] == 'cancelling': # Should have been caught earlier, but as safeguard
                        task['status'] = 'cancelled'
                        print(f"Download {download_id} found as 'cancelling' post-loop, now 'cancelled'.")
                        self.storage_optimizer.cleanup_incomplete_download(filepath)

        except IOError as e: # This will catch errors from os.makedirs or open()
            with self.lock:
                if download_id in self.download_tasks:
                    self.download_tasks[download_id]['status'] = 'failed'
                    self.download_tasks[download_id]['error'] = f"File/Directory error: {e}"
            print(f"Download {download_id} I/O error: {e}")
            # No cleanup here, as file might not have been created or is not accessible.
            return # Exit if directory/file cannot be prepared/opened

        except requests.exceptions.RequestException as e:
            with self.lock:
                if download_id in self.download_tasks:
                    self.download_tasks[download_id]['status'] = 'failed'
                    self.download_tasks[download_id]['error'] = str(e)
            print(f"Download {download_id} failed (RequestException): {e}")
            self.storage_optimizer.cleanup_incomplete_download(filepath)

        except Exception as e: # Catch any other unexpected errors
            with self.lock:
                if download_id in self.download_tasks:
                    self.download_tasks[download_id]['status'] = 'failed'
                    self.download_tasks[download_id]['error'] = f"Unexpected error during download: {e}"
            print(f"Download {download_id} unexpected error: {e}")
            # Potentially cleanup, depending on when this unexpected error occurred
            if os.path.exists(filepath) and bytes_downloaded > 0 : # If some bytes were written
                 self.storage_optimizer.cleanup_incomplete_download(filepath)


    def start_download(self, url, filepath):
        download_id = str(uuid.uuid4())
        # Ensure directory exists (moved to _download_file to handle it just before writing)
        # if os.path.dirname(filepath):
        #      os.makedirs(os.path.dirname(filepath), exist_ok=True)

        with self.lock:
            self.download_tasks[download_id] = {
                'url': url,
                'filepath': filepath,
                'status': 'pending',
                'thread': None,
                'error': None,
                'progress': 0.0,
                'bytes_downloaded': 0,
                'total_size': 0
            }

        thread = threading.Thread(target=self._download_file, args=(url, filepath, download_id))
        with self.lock:
            self.download_tasks[download_id]['thread'] = thread
            self.download_tasks[download_id]['status'] = 'downloading'
        thread.start()
        print(f"Download {download_id} started for {url} to {filepath}")
        return download_id

    def get_status(self, download_id):
        with self.lock:
            task = self.download_tasks.get(download_id)
            if task:
                # Return a copy to prevent external modification of the internal dict
                return {
                    'status': task.get('status'),
                    'error': task.get('error'),
                    'progress': task.get('progress', 0.0),
                    'bytes_downloaded': task.get('bytes_downloaded', 0),
                    'total_size': task.get('total_size', 0),
                    'filepath': task.get('filepath'),
                    'url': task.get('url')
                    # 'speed': task.get('speed', 0) # if implemented
                }
            return None

    def pause_download(self, download_id):
        print(f"Attempting to pause download {download_id}")
        with self.lock:
            if download_id in self.download_tasks:
                task = self.download_tasks[download_id]
                if task['status'] == 'downloading':
                    task['status'] = 'paused'
                    # Actual pausing is complex and not implemented here.
                    # This just sets a status. The download thread continues.
                    print(f"Download {download_id} marked as paused (basic implementation).")
                elif task['status'] == 'pending': # If it's pending, it can be "paused" before starting
                    task['status'] = 'paused'
                    print(f"Download {download_id} was pending, now marked as paused.")
                else:
                    print(f"Download {download_id} is not in 'downloading' or 'pending' state (current state: {task['status']}). Cannot pause.")
            else:
                print(f"Download ID {download_id} not found. Cannot pause.")


    def resume_download(self, download_id):
        print(f"Attempting to resume download {download_id}")
        with self.lock:
            task = self.download_tasks.get(download_id)
            if task and task['status'] == 'paused':
                # To truly resume, logic would depend on whether it was paused mid-download or before starting
                # For now, if it was 'paused' from 'downloading', it's a conceptual resume as thread may still be running.
                # If it was 'paused' from 'pending', changing to 'pending' might allow QM to pick it up.
                # Or, if Downloader manages its own worker pool, it might restart.
                # For this setup, let's assume QueueManager will re-trigger if it becomes 'pending'.
                # This is a simplification. True resume for HTTP needs Range headers.
                task['status'] = 'pending' # Let QM re-evaluate. Or 'downloading' if Downloader manages its own active pool.
                print(f"Download {download_id} marked as pending to be resumed/restarted by manager.")
            elif task:
                print(f"Download {download_id} is not in 'paused' state (current state: {task['status']}). Cannot resume.")
            else:
                print(f"Download ID {download_id} not found. Cannot resume.")


    def cancel_download(self, download_id):
        print(f"Attempting to cancel download {download_id}")
        with self.lock:
            if download_id in self.download_tasks:
                task = self.download_tasks[download_id]
                current_status = task['status']
                if current_status not in ['completed', 'failed', 'cancelled', 'cancelling']:
                    task['status'] = 'cancelling' # Signal the thread or prevent start
                    print(f"Download {download_id} (status: {current_status}) marked for cancellation (cancelling).")
                    # If thread exists and is alive, it will see 'cancelling' and then set 'cancelled'.
                    # If thread hasn't started (e.g. status 'pending'), _download_file will see 'cancelling' if/when it starts.
                    # If the task is 'pending' and we want to ensure it doesn't start,
                    # we could also remove it or directly set to 'cancelled' if no thread is associated yet.
                    # For simplicity, 'cancelling' state is used, and _download_file handles it.
                elif current_status == 'cancelling':
                    print(f"Download {download_id} is already being cancelled.")
                else: # completed, failed, already cancelled
                    print(f"Download {download_id} is already in a terminal state ({current_status}). Cannot cancel again, but ensuring it's marked 'cancelled'.")
                    task['status'] = 'cancelled' # Ensure it's marked as cancelled if it was, e.g., completed.
            else:
                print(f"Download ID {download_id} not found. Cannot cancel.")

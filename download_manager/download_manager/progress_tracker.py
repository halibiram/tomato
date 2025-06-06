import time
import os # For os.linesep or clearing screen

class ProgressTracker:
    def __init__(self, queue_manager):
        self.queue_manager = queue_manager

    def get_human_readable_size(self, size_in_bytes):
        if size_in_bytes is None: # Handle cases where size might be None
            return "N/A"
        if size_in_bytes == 0:
            return "0B"
        size_name = ("B", "KB", "MB", "GB", "TB")
        i = 0
        # Ensure size_in_bytes is treated as a number for comparison
        try:
            num_size = float(size_in_bytes)
        except ValueError:
            return "Invalid Size"

        while num_size >= 1024 and i < len(size_name) - 1:
            num_size /= 1024.0
            i += 1
        return f"{num_size:.2f}{size_name[i]}"

    def display_overall_progress(self, clear_screen=False):
        if clear_screen:
            os.system('cls' if os.name == 'nt' else 'clear')

        statuses = self.queue_manager.get_queue_status() # From QueueManager

        print("--- Download Progress ---")

        print("\n== Pending Tasks ==")
        pending_tasks = statuses.get('pending_tasks', [])
        if pending_tasks:
            for task in pending_tasks:
                # Adjusted to use 'qm_id' as per QueueManager's structure
                print(f"  ID (QM): {task.get('qm_id', 'N/A')} | URL: {task.get('url', 'N/A')} | Status: {task.get('status', 'N/A')} | Priority: {task.get('priority', 'N/A')}")
        else:
            print("  No tasks in queue.")

        print("\n== Active Downloads ==")
        active_tasks_from_qm = statuses.get('active_tasks', [])

        if active_tasks_from_qm:
            for qm_task_view in active_tasks_from_qm:
                # qm_task_view is the merged_details dictionary from QueueManager.get_queue_status()
                qm_id = qm_task_view.get('qm_id', 'N/A') # QM's internal task ID
                downloader_id = qm_task_view.get('downloader_id', 'N/A') # Downloader's task ID
                url = qm_task_view.get('url', 'N/A')
                live_status_info = qm_task_view.get('live_status')

                if live_status_info:
                    status = live_status_info.get('status', 'N/A')
                    bytes_down = live_status_info.get('bytes_downloaded', 0)
                    total_bytes = live_status_info.get('total_size', 0)
                    progress_percent = live_status_info.get('progress', 0.0)

                    readable_bytes_down = self.get_human_readable_size(bytes_down)
                    readable_total_bytes = self.get_human_readable_size(total_bytes)

                    progress_bar_length = 30
                    # Ensure progress_percent is a float for calculation
                    try:
                        filled_length = int(progress_bar_length * float(progress_percent) // 100)
                    except ValueError:
                        filled_length = 0

                    bar = '█' * filled_length + '-' * (progress_bar_length - filled_length)

                    print(f"  ID (QM): {qm_id} | Downloader ID: {downloader_id} | {status.upper()}")
                    print(f"     URL: {url}")
                    print(f"     {bar} {float(progress_percent):.2f}%")
                    print(f"     {readable_bytes_down} / {readable_total_bytes}")

                    error_info = live_status_info.get('error')
                    if error_info:
                        print(f"     Error: {error_info}")
                    # speed = live_status_info.get('speed', 0) # Placeholder for speed
                    # if speed > 0:
                    #    print(f"     Speed: {self.get_human_readable_size(speed)}/s")
                else:
                    # Fallback if live_status is not available
                    print(f"  ID (QM): {qm_id} | Downloader ID: {downloader_id} | URL: {url} | Status: {qm_task_view.get('status', 'checking...')}")
        else:
            print("  No active downloads.")

        print("\n--- End of Report ---")

    def start_live_display(self, interval_seconds=1, clear_screen_each_time=True):
        try:
            # Hide cursor for cleaner display if clearing screen (POSIX specific)
            if clear_screen_each_time and os.name == 'posix':
                 print("\x1b[?25l", end="") # Hide cursor

            while True:
                self.display_overall_progress(clear_screen=clear_screen_each_time)
                time.sleep(interval_seconds)
        except KeyboardInterrupt:
            print("Stopping live display...")
        finally:
            # Ensure cursor is visible or any other cleanup if needed
            if clear_screen_each_time and os.name == 'posix':
                print("\x1b[?25h", end="") # Show cursor
            print("Live display terminated.")

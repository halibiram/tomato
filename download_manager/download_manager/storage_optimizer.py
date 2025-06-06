import os
import shutil

class StorageOptimizer:
    def __init__(self, default_download_dir="downloads"):
        self.default_download_dir = os.path.abspath(default_download_dir)
        try:
            os.makedirs(self.default_download_dir, exist_ok=True)
        except OSError as e:
            # This might happen if the path is a file or other permission issues
            print(f"Warning: Could not create default download directory '{self.default_download_dir}': {e}")


    def get_free_space(self, path=None):
        check_path = path or self.default_download_dir
        # Ensure we are checking a directory for disk_usage
        if os.path.isfile(check_path):
            check_path_dir = os.path.dirname(check_path)
        else:
            check_path_dir = check_path

        # If check_path_dir is empty (e.g. relative file in CWD), use CWD
        if not check_path_dir: # Handles cases like filepath being just "file.txt"
            check_path_dir = os.getcwd()

        try:
            # Ensure the directory actually exists before calling disk_usage
            if not os.path.exists(check_path_dir) or not os.path.isdir(check_path_dir):
                # Try to create it, or fallback to CWD if it's the default path that failed earlier
                if check_path_dir == self.default_download_dir:
                    os.makedirs(self.default_download_dir, exist_ok=True) # Try creating default again
                else: # If it's a custom path, don't try to create, just report error for that path
                    print(f"Error: Path '{check_path_dir}' for free space check does not exist or is not a directory.")
                    return 0

            total, used, free = shutil.disk_usage(check_path_dir)
            return free
        except Exception as e:
            print(f"Error getting free space for '{check_path_dir}': {e}")
            return 0

    def get_file_size(self, filepath):
        try:
            if os.path.exists(filepath) and os.path.isfile(filepath):
                return os.path.getsize(filepath)
            return 0 # Return 0 if file doesn't exist or is not a file
        except OSError as e:
            print(f"Error getting size for file {filepath}: {e}")
            return 0

    def delete_file(self, filepath):
        try:
            if os.path.exists(filepath) and os.path.isfile(filepath):
                os.remove(filepath)
                print(f"StorageOptimizer: File {filepath} deleted successfully.")
                return True
            else:
                # print(f"StorageOptimizer: File {filepath} not found or is not a file for deletion.")
                return False # Less verbose if file just isn't there
        except OSError as e:
            print(f"StorageOptimizer: Error deleting file {filepath}: {e}")
            return False

    def suggest_filepath(self, url, filename=None, directory=None):
        target_dir = directory or self.default_download_dir

        try:
            os.makedirs(target_dir, exist_ok=True)
        except OSError as e:
            print(f"Warning: Could not create target directory '{target_dir}' for suggested filepath. Using default. Error: {e}")
            target_dir = self.default_download_dir # Fallback to ensure default_download_dir is used if custom fails
            os.makedirs(target_dir, exist_ok=True) # Attempt to create default if it also failed earlier

        if not filename:
            try:
                # Extract filename from URL, remove query parameters
                filename_from_url = url.split('?')[0].split('/')[-1]
                # If the path ends with a slash or is empty after split, assign a default name
                if not filename_from_url or filename_from_url.endswith('.'): # handle cases like 'http://host/.'
                    filename_from_url = "downloaded_file"
                filename = filename_from_url.strip()
                if not filename: # if strip results in empty
                    filename = "downloaded_file"
            except Exception:
                filename = "downloaded_file" # Default filename if URL parsing fails

        # Basic sanitization for filename
        # Allow alphanumeric, dot, underscore, hyphen. Replace others.
        # This is a very basic sanitization. More robust would be needed for production.
        sanitized_filename = "".join(c if (c.isalnum() or c in ['.', '_', '-']) else '_' for c in filename).strip('_.- ')

        if not sanitized_filename: # If sanitization removed everything or resulted in only invalid chars
            sanitized_filename = "sanitized_download_file"

        # Prevent excessively long filenames (OS limits)
        max_len = 200 # A conservative max length for the filename part
        if len(sanitized_filename) > max_len:
            name_part, ext_part = os.path.splitext(sanitized_filename)
            ext_len = len(ext_part)
            name_part = name_part[:max_len - ext_len -1] # -1 for the dot if ext_part is not empty
            sanitized_filename = name_part + ext_part

        return os.path.join(target_dir, sanitized_filename)

    def cleanup_incomplete_download(self, filepath):
        print(f"StorageOptimizer: Attempting to clean up incomplete download: {filepath}")
        return self.delete_file(filepath)

    def ensure_directory_exists(self, directory_path):
        """Ensures that a directory exists, creating it if necessary."""
        if not directory_path: return False # Or raise error
        try:
            os.makedirs(directory_path, exist_ok=True)
            return True
        except OSError as e:
            print(f"StorageOptimizer: Error creating directory {directory_path}: {e}")
            return False

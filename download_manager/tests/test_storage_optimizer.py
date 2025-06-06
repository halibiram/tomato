# tests/test_storage_optimizer.py
import unittest
from unittest.mock import patch, MagicMock
import os
import shutil
from download_manager.storage_optimizer import StorageOptimizer

class TestStorageOptimizer(unittest.TestCase):

    def setUp(self):
        self.test_dir = "test_optimizer_downloads"
        # Clean up before test if it exists
        if os.path.exists(self.test_dir):
            shutil.rmtree(self.test_dir)
        # Create it fresh for each test
        os.makedirs(self.test_dir, exist_ok=True)
        self.optimizer = StorageOptimizer(default_download_dir=self.test_dir)

    def tearDown(self):
        # Clean up test directory
        if os.path.exists(self.test_dir):
            shutil.rmtree(self.test_dir)

    def test_default_directory_creation(self):
        self.assertTrue(os.path.exists(self.test_dir))
        # Also test the internal default_download_dir attribute
        self.assertEqual(self.optimizer.default_download_dir, os.path.abspath(self.test_dir))

    def test_suggest_filepath_default_dir(self):
        url = "http://example.com/testfile.zip"
        filepath = self.optimizer.suggest_filepath(url)
        self.assertEqual(os.path.dirname(filepath), os.path.abspath(self.test_dir))
        self.assertEqual(os.path.basename(filepath), "testfile.zip")

    def test_suggest_filepath_custom_dir(self):
        url = "http://example.com/another.txt"
        custom_dir_name = "custom"
        custom_dir_path = os.path.join(self.test_dir, custom_dir_name)

        filepath = self.optimizer.suggest_filepath(url, directory=custom_dir_path)

        self.assertTrue(os.path.exists(custom_dir_path)) # Check if custom dir was created
        self.assertEqual(os.path.dirname(filepath), os.path.abspath(custom_dir_path))
        self.assertEqual(os.path.basename(filepath), "another.txt")

    def test_suggest_filepath_with_filename(self):
        url = "http://example.com/ignored"
        custom_filename = "my_file.dat"
        filepath = self.optimizer.suggest_filepath(url, filename=custom_filename)
        self.assertEqual(os.path.basename(filepath), custom_filename)
        # Ensure it's in the default directory if no directory is specified
        self.assertEqual(os.path.dirname(filepath), os.path.abspath(self.test_dir))

    def test_suggest_filepath_url_parsing_and_sanitization(self):
        url1 = "http://example.com/path/to/file.with.dots.tar.gz?query=param&another=val"
        filepath1 = self.optimizer.suggest_filepath(url1)
        self.assertEqual(os.path.basename(filepath1), "file.with.dots.tar.gz")

        url2 = "http://example.com/" # No filename part
        filepath2 = self.optimizer.suggest_filepath(url2)
        self.assertEqual(os.path.basename(filepath2), "downloaded_file")

        url3 = "http://example.com/filename with spaces.txt"
        filepath3 = self.optimizer.suggest_filepath(url3)
        self.assertEqual(os.path.basename(filepath3), "filename_with_spaces.txt")

        url4 = "http://example.com/../../../../../etc/passwd" # Path traversal attempt
        filepath4 = self.optimizer.suggest_filepath(url4)
        self.assertEqual(os.path.basename(filepath4), ".._.._.._.._.._etc_passwd") # Sanitized

        url5 = "http://example.com/verylongfilename" + "a"*250 + ".txt"
        filepath5 = self.optimizer.suggest_filepath(url5)
        self.assertTrue(len(os.path.basename(filepath5)) < 255) # Check if shortened


    @patch('os.path.getsize')
    @patch('os.path.exists')
    @patch('os.path.isfile')
    def test_get_file_size(self, mock_isfile, mock_exists, mock_getsize):
        mock_exists.return_value = True
        mock_isfile.return_value = True
        mock_getsize.return_value = 1024
        self.assertEqual(self.optimizer.get_file_size("dummy/path.txt"), 1024)

        mock_exists.return_value = False
        self.assertEqual(self.optimizer.get_file_size("dummy/nonexistent.txt"), 0)

        mock_exists.return_value = True
        mock_isfile.return_value = False # It's a directory
        self.assertEqual(self.optimizer.get_file_size("dummy/directory"), 0)


    @patch('os.remove')
    @patch('os.path.exists')
    @patch('os.path.isfile')
    def test_delete_file(self, mock_isfile, mock_exists, mock_remove):
        # Test successful deletion
        mock_exists.return_value = True
        mock_isfile.return_value = True
        self.assertTrue(self.optimizer.delete_file("dummy/path.txt"))
        mock_remove.assert_called_once_with("dummy/path.txt")

        # Test file not found
        mock_exists.return_value = False
        self.assertFalse(self.optimizer.delete_file("dummy/other.txt"))

        # Test path is a directory, not a file
        mock_exists.return_value = True
        mock_isfile.return_value = False
        self.assertFalse(self.optimizer.delete_file("dummy/dir_path"))


    @patch('shutil.disk_usage')
    @patch('os.path.exists') # Mock exists for get_free_space directory check
    @patch('os.path.isdir') # Mock isdir for get_free_space directory check
    def test_get_free_space(self, mock_isdir, mock_exists, mock_disk_usage):
        mock_disk_usage.return_value = (1000, 500, 500) # total, used, free

        # Test with default directory
        mock_exists.return_value = True # For self.test_dir
        mock_isdir.return_value = True  # For self.test_dir
        self.assertEqual(self.optimizer.get_free_space(), 500) # Uses default_download_dir

        # Test with a specific path that is a directory
        specific_dir = "/tmp/specific_dir_test"
        mock_exists.return_value = True # For specific_dir
        mock_isdir.return_value = True  # For specific_dir
        self.assertEqual(self.optimizer.get_free_space(specific_dir), 500)
        mock_disk_usage.assert_called_with(specific_dir)

        # Test with a file path (should use dirname)
        file_in_test_dir = os.path.join(self.test_dir, "file.txt")
        mock_exists.return_value = True # For self.test_dir (dirname of file_in_test_dir)
        mock_isdir.return_value = True  # For self.test_dir
        self.assertEqual(self.optimizer.get_free_space(file_in_test_dir), 500)
        mock_disk_usage.assert_called_with(self.test_dir) # Called with dirname

        # Test path does not exist
        mock_exists.return_value = False
        self.assertEqual(self.optimizer.get_free_space("/non/existent/path"), 0)


    def test_cleanup_incomplete_download_deletes_file(self):
        # This test uses real file system operations within test_dir
        temp_file_name = "incomplete.part"
        temp_file_path = os.path.join(self.test_dir, temp_file_name)

        with open(temp_file_path, "w") as f:
            f.write("temp data for cleanup test")

        self.assertTrue(os.path.exists(temp_file_path)) # Ensure file is created

        self.optimizer.cleanup_incomplete_download(temp_file_path)

        self.assertFalse(os.path.exists(temp_file_path)) # Ensure file is deleted

    def test_ensure_directory_exists(self):
        new_dir_path = os.path.join(self.test_dir, "newly_created_dir")
        self.assertFalse(os.path.exists(new_dir_path))
        self.optimizer.ensure_directory_exists(new_dir_path)
        self.assertTrue(os.path.exists(new_dir_path))
        self.assertTrue(os.path.isdir(new_dir_path))


if __name__ == '__main__':
    unittest.main()

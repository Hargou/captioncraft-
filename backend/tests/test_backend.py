import sqlite3
import requests
import os
import json
from pathlib import Path
import atexit
import uuid
import base64

# Test configuration
BASE_URL = "http://localhost:8000"
TEST_IMAGE_PATH = "test_image.jpg"
TEST_IMAGE_DIR = "user_post_images"

# Generate unique test username
TEST_USERNAME = f"testuser_{uuid.uuid4().hex[:8]}"

# Cleanup function to remove test files
def cleanup_test_files():
    if os.path.exists(TEST_IMAGE_PATH):
        try:
            os.remove(TEST_IMAGE_PATH)
        except:
            pass
    
    if os.path.exists(TEST_IMAGE_DIR):
        for file in os.listdir(TEST_IMAGE_DIR):
            try:
                os.remove(os.path.join(TEST_IMAGE_DIR, file))
            except:
                pass
        try:
            os.rmdir(TEST_IMAGE_DIR)
        except:
            pass

# Register cleanup function
atexit.register(cleanup_test_files)

def test_database_setup():
    print("\n1. Testing Database Setup...")
    try:
        conn = sqlite3.connect('CapRank.db')
        cursor = conn.cursor()
        
        # Check if all tables exist
        tables = ['User', 'Post', 'Caption', 'UserLikedPosts', 'UserLikedCaptions']
        for table in tables:
            cursor.execute(f"SELECT name FROM sqlite_master WHERE type='table' AND name='{table}'")
            if not cursor.fetchone():
                print(f"❌ Table {table} not found")
                return False
            print(f"✅ Table {table} exists")
        
        conn.close()
        return True
    except Exception as e:
        print(f"❌ Database setup test failed: {e}")
        return False

def test_user_registration():
    print("\n2. Testing User Registration...")
    try:
        # Test data with unique username
        test_user = {
            "username": TEST_USERNAME,
            "name": "Test User",
            "password": "testpass123",
            "profilePicture": None
        }
        
        response = requests.post(f"{BASE_URL}/register", json=test_user)
        
        if response.status_code == 201:
            print("✅ User registration successful")
            return True
        else:
            print(f"❌ User registration failed: {response.text}")
            return False
    except Exception as e:
        print(f"❌ User registration test failed: {e}")
        return False

def test_user_login():
    print("\n3. Testing User Login...")
    try:
        # Test data
        login_data = {
            "username": TEST_USERNAME,
            "password": "testpass123"
        }
        
        response = requests.post(f"{BASE_URL}/login", json=login_data)
        
        if response.status_code == 200:
            print("✅ User login successful")
            return True
        else:
            print(f"❌ User login failed: {response.text}")
            return False
    except Exception as e:
        print(f"❌ User login test failed: {e}")
        return False

def test_post_creation():
    print("\n4. Testing Post Creation...")
    try:
        # Create test image directory if it doesn't exist
        os.makedirs(TEST_IMAGE_DIR, exist_ok=True)
        
        # Create a test image file
        with open(TEST_IMAGE_PATH, 'wb') as f:
            f.write(b'dummy image data')
        
        # Test data
        with open(TEST_IMAGE_PATH, 'rb') as f:
            # Send all data as form fields with correct types
            data = {
                'userId': (None, '1'),  # Send as form field
                'password': (None, 'testpass123'),  # Send as form field
                'userCaptionText': (None, 'Test caption'),  # Send as form field
                'image': ('test.jpg', f, 'image/jpeg')  # Send as file
            }
            
            print(f"Debug - Request URL: {BASE_URL}/post/create")
            print(f"Debug - Data: {data}")
            
            # Send multipart form data
            response = requests.post(
                f"{BASE_URL}/post/create",
                files=data
            )
            
            print(f"Debug - Response status: {response.status_code}")
            print(f"Debug - Response headers: {response.headers}")
            print(f"Debug - Response content: {response.text}")
        
        if response.status_code == 201:
            print("✅ Post creation successful")
            return True
        else:
            print(f"❌ Post creation failed: {response.text}")
            return False
    except Exception as e:
        print(f"❌ Post creation test failed: {e}")
        return False

def test_caption_creation():
    print("\n5. Testing Caption Creation...")
    try:
        # First create a post to get a valid post ID
        post_response = test_post_creation()
        if not post_response:
            print("❌ Cannot create caption without a valid post")
            return False
            
        # Get the post ID from the response
        post_id = 1  # Use the first post ID since we know it exists
        
        # Test data
        data = {
            "postId": post_id,
            "userId": 1,
            "password": "testpass123",
            "text": "Test caption"
        }
        
        print(f"Debug - Request URL: {BASE_URL}/caption/create")
        print(f"Debug - Data: {data}")
        
        # Send JSON data
        response = requests.post(
            f"{BASE_URL}/caption/create",
            json=data
        )
        
        print(f"Debug - Response status: {response.status_code}")
        print(f"Debug - Response headers: {response.headers}")
        print(f"Debug - Response content: {response.text}")
        
        if response.status_code == 201:
            print("✅ Caption creation successful")
            return True
        else:
            print(f"❌ Caption creation failed: {response.text}")
            return False
    except Exception as e:
        print(f"❌ Caption creation test failed: {e}")
        return False

def test_like_functionality():
    print("\n6. Testing Like Functionality...")
    try:
        # Test liking a post
        like_data = {
            "userId": 1,
            "postId": 1,
            "password": "testpass123"
        }
        
        response = requests.post(f"{BASE_URL}/post/like", json=like_data)
        
        if response.status_code == 200:
            print("✅ Post like successful")
        else:
            print(f"❌ Post like failed: {response.text}")
            return False
        
        # Test liking a caption
        like_data = {
            "userId": 1,
            "captionId": 1,
            "password": "testpass123"
        }
        
        response = requests.post(f"{BASE_URL}/caption/like", json=like_data)
        
        if response.status_code == 200:
            print("✅ Caption like successful")
            return True
        else:
            print(f"❌ Caption like failed: {response.text}")
            return False
    except Exception as e:
        print(f"❌ Like functionality test failed: {e}")
        return False

def run_all_tests():
    print("Starting Backend Tests...")
    
    # Clean up any existing test files
    cleanup_test_files()
    
    tests = [
        test_database_setup,
        test_user_registration,
        test_user_login,
        test_post_creation,
        test_caption_creation,
        test_like_functionality
    ]
    
    results = []
    for test in tests:
        results.append(test())
    
    success_count = sum(1 for result in results if result)
    total_tests = len(results)
    
    print(f"\nTest Summary: {success_count}/{total_tests} tests passed")
    return all(results)

if __name__ == "__main__":
    run_all_tests() 
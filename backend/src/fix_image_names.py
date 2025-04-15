import sqlite3
import os
from pathlib import Path

def fix_image_names():
    # Connect to the database
    connection = sqlite3.connect('CapRank.db')
    cursor = connection.cursor()

    # First, delete all posts that don't have corresponding images
    cursor.execute("DELETE FROM Post")
    connection.commit()
    print("Cleared all posts from database")

    # Get list of actual image files
    image_dir = Path('src/user_post_images')
    actual_files = os.listdir(image_dir)
    print(f"Found {len(actual_files)} files in {image_dir}")
    print("Files:", actual_files)

    # Create new posts for each existing image
    for filename in actual_files:
        if '_' in filename:
            user_id = int(filename.split('_')[0])
            cursor.execute("""
                INSERT INTO Post (userId, imageName)
                VALUES (?, ?)
            """, (user_id, filename))
            print(f"Created new post for image {filename}")

    # Commit changes and close connection
    connection.commit()
    connection.close()

if __name__ == "__main__":
    fix_image_names() 
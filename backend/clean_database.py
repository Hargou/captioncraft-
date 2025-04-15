import sqlite3
import os
import shutil

# Connect to the database
conn = sqlite3.connect('CapRank.db')
cursor = conn.cursor()

# Get the post image folder
image_folder = 'src/user_post_images'

# Get the 2 most recent posts
cursor.execute("""
    SELECT id, imageName 
    FROM Post 
    ORDER BY created_at DESC 
    LIMIT 2
""")
posts_to_keep = cursor.fetchall()

if not posts_to_keep:
    print("No posts found in the database")
    conn.close()
    exit()

# Get the IDs of the posts to keep
post_ids_to_keep = [post[0] for post in posts_to_keep]
image_names_to_keep = [post[1] for post in posts_to_keep]

print(f"Keeping posts with IDs: {post_ids_to_keep}")
print(f"Keeping images: {image_names_to_keep}")

# Delete all captions for posts we're removing
cursor.execute("""
    DELETE FROM Caption 
    WHERE postId NOT IN ({})
""".format(','.join(['?' for _ in post_ids_to_keep])), post_ids_to_keep)
print(f"Deleted {cursor.rowcount} captions")

# Delete all likes for posts we're removing
cursor.execute("""
    DELETE FROM UserLikedPosts 
    WHERE postId NOT IN ({})
""".format(','.join(['?' for _ in post_ids_to_keep])), post_ids_to_keep)
print(f"Deleted {cursor.rowcount} post likes")

# Delete all posts except the 2 most recent
cursor.execute("""
    DELETE FROM Post 
    WHERE id NOT IN ({})
""".format(','.join(['?' for _ in post_ids_to_keep])), post_ids_to_keep)
print(f"Deleted {cursor.rowcount} posts")

# Commit the changes
conn.commit()
conn.close()

# Clean up image files
if os.path.exists(image_folder):
    # Get all image files
    all_images = os.listdir(image_folder)
    
    # Delete images not in the keep list
    for image_name in all_images:
        if image_name not in image_names_to_keep:
            file_path = os.path.join(image_folder, image_name)
            try:
                os.remove(file_path)
                print(f"Deleted image file: {image_name}")
            except Exception as e:
                print(f"Error deleting {image_name}: {e}")

print("Database cleanup complete!") 
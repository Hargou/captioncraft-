import sqlite3

def view_db():
    connection = sqlite3.connect('CapRank.db')
    cursor = connection.cursor()

    # View all posts
    print("\nCurrent posts in database:")
    cursor.execute("SELECT * FROM Post")
    posts = cursor.fetchall()
    for post in posts:
        print(f"ID: {post[0]}, UserID: {post[1]}, ImageName: {post[2]}, Created: {post[3]}, Likes: {post[4]}, TopCaptionId: {post[5]}")

    # Ask if user wants to delete posts
    response = input("\nDo you want to delete all posts? (yes/no): ")
    if response.lower() == 'yes':
        cursor.execute("DELETE FROM Post")
        connection.commit()
        print("All posts deleted.")

    # Ask if user wants to add test posts
    response = input("\nDo you want to add test posts with correct image names? (yes/no): ")
    if response.lower() == 'yes':
        # Add both existing images
        cursor.execute("""
            INSERT INTO Post (userId, imageName)
            VALUES 
                (1, '1_811f3f0b4744413aa364b3241a4ef643.jpg'),
                (2, '2_471afa0d3a6c4ea6bac7ee0bb0f2042a.jpg')
        """)
        connection.commit()
        print("Test posts added.")

    # View updated posts
    print("\nUpdated posts in database:")
    cursor.execute("SELECT * FROM Post")
    posts = cursor.fetchall()
    for post in posts:
        print(f"ID: {post[0]}, UserID: {post[1]}, ImageName: {post[2]}, Created: {post[3]}, Likes: {post[4]}, TopCaptionId: {post[5]}")

    connection.close()

if __name__ == "__main__":
    view_db() 
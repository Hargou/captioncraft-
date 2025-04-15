import sqlite3

def create_comments_table():
    # Connect to database
    conn = sqlite3.connect('CapRank.db')
    cursor = conn.cursor()
    
    # Enable foreign keys
    cursor.execute("PRAGMA foreign_keys = ON")
    
    # Create the table if it doesn't exist
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS CaptionComments (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        captionId INTEGER,
        userId INTEGER,
        text TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY(captionId) REFERENCES Caption(id) ON DELETE CASCADE,
        FOREIGN KEY(userId) REFERENCES User(id)
    )
    ''')
    
    # Commit changes and close connection
    conn.commit()
    conn.close()
    
    print('CaptionComments table created successfully')

if __name__ == '__main__':
    create_comments_table() 
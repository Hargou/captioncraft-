import sqlite3

def setupDatabase():

    databaseName = 'CapRank.db'
    connection = sqlite3.connect(databaseName)
    cursor = connection.cursor()
    cursor.execute("PRAGMA foreign_keys = ON;")

    schemaCommand = """
        CREATE TABLE IF NOT EXISTS User (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            name TEXT NOT NULL,
            password TEXT NOT NULL,
            profilePicture TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        );

        CREATE TABLE IF NOT EXISTS Post (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            userId INTEGER NOT NULL,
            imageName TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            likes INTEGER DEFAULT 0,
            topCaptionId INTEGER,
            
            FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE,
            FOREIGN KEY (topCaptionId) REFERENCES Caption(id) ON DELETE SET NULL
        );

        CREATE TABLE IF NOT EXISTS Caption (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            postId INTEGER NOT NULL,
            userId INTEGER NOT NULL,
            text TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            likes INTEGER DEFAULT 0,

            FOREIGN KEY (postId) REFERENCES Post(id) ON DELETE CASCADE,
            FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS UserLikedPosts (
            userId INTEGER NOT NULL,
            postId INTEGER NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            
            PRIMARY KEY (userId, postId),
            FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE,
            FOREIGN KEY (postId) REFERENCES Post(id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS UserLikedCaptions (
            userId INTEGER NOT NULL,
            captionId INTEGER NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            
            PRIMARY KEY (userId, captionId),
            FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE,
            FOREIGN KEY (captionId) REFERENCES Caption(id) ON DELETE CASCADE
        );
    """

    cursor.executescript(schemaCommand)
    connection.commit()
    connection.close()
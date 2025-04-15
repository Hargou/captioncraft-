from litestar import Controller, get, status_codes, post, patch, delete
from litestar.exceptions import HTTPException

from src.modules.data_types import DT_CaptionCreate, DT_CommentCreate

import sqlite3





class Controller_Caption(Controller):

    path = '/captions'



    @get("/{captionId:int}", status_code=status_codes.HTTP_200_OK)
    async def getCaption(self, captionId: int) -> dict:
        try:

            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            cursor.execute("""
                SELECT *
                FROM Caption
                WHERE id = ?
            """, (captionId,))


            queriedCaption = cursor.fetchone()
            connection.close()

            if queriedCaption == None:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f"No caption with id: {captionId}")
            
            return {
                'status': 'green',
                'message': 'Caption queried successfully',
                'data': queriedCaption
            }
        
        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f'ERROR: {e}')



    @get("/post/{postId:int}", status_code=status_codes.HTTP_200_OK)
    async def getCaptionsByPost(self, postId: int) -> dict:
        try:
            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            cursor.execute("""
                SELECT c.*, u.username
                FROM Caption c
                JOIN User u ON c.userId = u.id
                WHERE c.postId = ?
                ORDER BY c.likes DESC, c.created_at ASC
            """, (postId,))

            queriedCaptions = cursor.fetchall()
            connection.close()
            
            return {
                'status': 'green',
                'message': 'Captions for post queried successfully',
                'data': queriedCaptions
            }
        
        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f'ERROR: {e}')


    @get("/", status_code=status_codes.HTTP_200_OK)
    async def getAllCaptions(self) -> dict:
        try:

            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            cursor.execute("""
                SELECT *
                FROM Caption
                ORDER BY created_at DESC
            """)

            queriedCaptions = cursor.fetchall()
            connection.close()
            
            return {
                'status': 'green',
                'message': 'All captions queried successfully',
                'data': queriedCaptions
            }
        

        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f'ERROR: {e}')




    @post("/", status_code=status_codes.HTTP_201_CREATED)
    async def createCaption(self, data: DT_CaptionCreate) -> dict:
        connection = None
        try:
            # Establish connection with timeout and immediate mode
            connection = sqlite3.connect('CapRank.db', timeout=20)
            connection.execute('PRAGMA journal_mode=WAL')  # Write-Ahead Logging for better concurrency
            cursor = connection.cursor()

            # Check if captionCount column exists in Post table
            cursor.execute("""
                SELECT COUNT(*) 
                FROM pragma_table_info('Post') 
                WHERE name='captionCount'
            """)
            has_caption_count = cursor.fetchone()[0] > 0

            if not has_caption_count:
                # Add captionCount column if it doesn't exist
                cursor.execute("""
                    ALTER TABLE Post
                    ADD COLUMN captionCount INTEGER DEFAULT 0
                """)
                connection.commit()

            # Verify user credentials
            cursor.execute("""
                SELECT *
                FROM User
                WHERE id = ? AND password = ?
            """, (data.userId, data.password))

            user = cursor.fetchone()
            if not user:
                if connection:
                    connection.close()
                raise HTTPException(status_code=status_codes.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

            # Verify post exists
            cursor.execute("""
                SELECT *
                FROM Post
                WHERE id = ?
            """, (data.postId,))

            post = cursor.fetchone()
            if not post:
                if connection:
                    connection.close()
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail="Post not found")

            # Create caption
            cursor.execute("""
                INSERT INTO Caption (postId, userId, text)
                VALUES (?, ?, ?)
            """, (data.postId, data.userId, data.text))

            caption_id = cursor.lastrowid

            # Update post's caption count if the column exists
            if has_caption_count:
                cursor.execute("""
                    UPDATE Post
                    SET captionCount = captionCount + 1
                    WHERE id = ?
                """, (data.postId,))

            connection.commit()
            connection.close()

            return {
                'status': 'green',
                'message': 'Caption created successfully',
                'data': {
                    'captionId': caption_id
                }
            }

        except sqlite3.OperationalError as e:
            if connection:
                connection.rollback()
                connection.close()
            if "database is locked" in str(e):
                raise HTTPException(
                    status_code=status_codes.HTTP_503_SERVICE_UNAVAILABLE,
                    detail="Database is temporarily busy, please try again"
                )
            raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail=f"Database error: {e}")
        except Exception as e:
            if connection:
                connection.rollback()
                connection.close()
            raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail=f"ERROR: {e}")



    @post("/like", status_code=status_codes.HTTP_200_OK)
    async def likeCaption(self, data: dict) -> dict:
        try:
            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            # Verify user credentials
            cursor.execute("""
                SELECT *
                FROM User
                WHERE id = ? AND password = ?
            """, (data['userId'], data['password']))

            user = cursor.fetchone()
            if not user:
                raise HTTPException(status_code=status_codes.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

            # Check if user already liked the caption
            cursor.execute("""
                SELECT *
                FROM UserLikedCaptions
                WHERE userId = ? AND captionId = ?
            """, (data['userId'], data['captionId']))

            existing_like = cursor.fetchone()

            if existing_like:
                # Unlike
                cursor.execute("""
                    DELETE FROM UserLikedCaptions
                    WHERE userId = ? AND captionId = ?
                """, (data['userId'], data['captionId']))

                cursor.execute("""
                    UPDATE Caption
                    SET likes = likes - 1
                    WHERE id = ?
                """, (data['captionId'],))
            else:
                # Like
                cursor.execute("""
                    INSERT INTO UserLikedCaptions (userId, captionId)
                    VALUES (?, ?)
                """, (data['userId'], data['captionId']))

                cursor.execute("""
                    UPDATE Caption
                    SET likes = likes + 1
                    WHERE id = ?
                """, (data['captionId'],))

            connection.commit()
            connection.close()

            return {
                'status': 'green',
                'message': 'Caption like updated successfully'
            }

        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail=f"ERROR: {e}")



    # /caption/captionId_userId_password
    @delete('/{captionIdUserIdPassword:str}', status_code=status_codes.HTTP_200_OK)
    async def deleteCaption(self, captionIdUserIdPassword: str) -> dict:
        try:


            captionIdUserIdPassword = captionIdUserIdPassword.split("_")
            captionId = captionIdUserIdPassword[0]
            userId = captionIdUserIdPassword[1]
            password = captionIdUserIdPassword[2]

            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()
            cursor.execute("PRAGMA foreign_keys = ON;")


            cursor.execute("""
                SELECT * 
                FROM User 
                WHERE id = ? AND password = ? 
            """, (userId, password))
            
            queriedUser = cursor.fetchone()

            if queriedUser == None:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail="Unauthorized to delete caption")
            
            
            cursor.execute("""
                SELECT * 
                FROM Caption 
                WHERE id = ? AND userId = ? 
            """, (captionId, userId))
            
            queriedCaption = cursor.fetchone()

            if queriedCaption is None:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail="Unable to delete someone else caption")
            
            cursor.execute("""
                SELECT postId 
                FROM Caption 
                WHERE id = ?
            """, (captionId,))
            
            postId = cursor.fetchone()[0]
            
            cursor.execute("""
                SELECT topCaptionId 
                FROM Post 
                WHERE id = ?
            """, (postId,))
            
            topCaptionId = cursor.fetchone()[0]

            cursor.execute("""
                DELETE FROM Caption 
                WHERE id = ?
            """, (captionId,))
            
            if str(topCaptionId) == captionId:


                cursor.execute("""
                    SELECT id 
                    FROM Caption 
                    WHERE postId = ? 
                    ORDER BY likes DESC 
                    LIMIT 1
                """, (postId,))
                
                newTopCaption = cursor.fetchone()

                
                if newTopCaption is None:
                    cursor.execute("""
                        UPDATE Post 
                        SET topCaptionId = NULL 
                        WHERE id = ?
                    """, (postId,))
                else:
                    cursor.execute("""
                        UPDATE Post 
                        SET topCaptionId = ? 
                        WHERE id = ?
                    """, (newTopCaption[0], postId))

            connection.commit()
            connection.close()

            return {
                'status': 'green',
                'message': 'Caption deleted successfully'
            }
        
        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f"ERROR: {e}")




    # /caption/captionId_userId_password
    @patch('/{captionIdUserIdPassword:str}', status_code=status_codes.HTTP_200_OK)
    async def updateCaptionLikes(self, captionIdUserIdPassword: str) -> dict:

        try:
            captionIdUserIdPassword = captionIdUserIdPassword.split('_')

            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()
            cursor.execute("PRAGMA foreign_keys = ON;")



            cursor.execute("""
                SELECT *
                FROM User
                WHERE id = ? and password = ?
            """, (captionIdUserIdPassword[1], captionIdUserIdPassword[2]))

            queriedUser = cursor.fetchone()

            if queriedUser == None:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail="Unauthorized to delete caption")
                
            
            cursor.execute("""
                SELECT *
                FROM Caption
                WHERE id = ?
            """, (captionIdUserIdPassword[0],))

            queriedCaption = cursor.fetchone()

            if queriedCaption == None:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail="no caption with that id")
                

            cursor.execute("""
                SELECT *
                FROM UserLikedCaptions
                WHERE userId = ? and captionId = ?
            """, (captionIdUserIdPassword[1], captionIdUserIdPassword[0]))



            queriedLikedCaptions = cursor.fetchone()

            if queriedLikedCaptions == None:
                
                cursor.execute("""
                    UPDATE Caption
                    SET likes = likes + 1
                    WHERE id = ?
                """, (captionIdUserIdPassword[0],))

                cursor.execute("""
                    INSERT INTO
                    UserLikedCaptions (userId, captionId)
                        Values(?,?)
                """, (captionIdUserIdPassword[1], captionIdUserIdPassword[0]))


            else:
                cursor.execute("""
                    UPDATE Caption
                    SET likes = likes - 1
                    WHERE id = ?
                """, (captionIdUserIdPassword[0],))

                cursor.execute("""
                    DELETE FROM UserLikedCaptions
                    WHERE userId = ? and postId = ?
                """, (captionIdUserIdPassword[1], captionIdUserIdPassword[0]))


            cursor.execute("SELECT *  FROM Caption where postId = ?", (queriedCaption[1],))
            queriedPost = cursor.fetchone()


            cursor.execute("SELECT id FROM Caption WHERE postId + ? Order by likes DESC LIMIT 1", (queriedCaption[1]))

            highestLikedCaptionId = cursor.fetchone()

            if highestLikedCaptionId == captionIdUserIdPassword[0] and queriedPost[5] != highestLikedCaptionId:
                cursor.execute("Update Post SET topCaptionId = ? Where id = ?", (queriedCaption[0], queriedCaption[1]))


            connection.commit()
            connection.close()


            return {
                'status': 'green',
                'message': 'Caption updated successfully'
            }
        

        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f"ERROR: {e}")


    @post("/comment", status_code=status_codes.HTTP_201_CREATED)
    async def addComment(self, data: DT_CommentCreate) -> dict:
        try:
            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            # Verify user credentials
            cursor.execute("""
                SELECT *
                FROM User
                WHERE id = ? AND password = ?
            """, (data.userId, data.password))

            user = cursor.fetchone()
            if not user:
                raise HTTPException(status_code=status_codes.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

            # Verify caption exists
            cursor.execute("""
                SELECT *
                FROM Caption
                WHERE id = ?
            """, (data.captionId,))

            caption = cursor.fetchone()
            if not caption:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail="Caption not found")

            # Create comment
            cursor.execute("""
                INSERT INTO CaptionComments (captionId, userId, text)
                VALUES (?, ?, ?)
            """, (data.captionId, data.userId, data.text))

            comment_id = cursor.lastrowid
            connection.commit()
            connection.close()

            return {
                'status': 'green',
                'message': 'Comment added successfully',
                'data': {
                    'commentId': comment_id
                }
            }

        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail=f"ERROR: {e}")
            
    @get("/comments/{captionId:int}", status_code=status_codes.HTTP_200_OK)
    async def getComments(self, captionId: int) -> dict:
        try:
            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            # Verify caption exists
            cursor.execute("""
                SELECT *
                FROM Caption
                WHERE id = ?
            """, (captionId,))

            caption = cursor.fetchone()
            if not caption:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail="Caption not found")

            # Get comments
            cursor.execute("""
                SELECT cc.id, cc.captionId, cc.userId, u.username, cc.text, cc.created_at
                FROM CaptionComments cc
                JOIN User u ON cc.userId = u.id
                WHERE cc.captionId = ?
                ORDER BY cc.created_at ASC
            """, (captionId,))

            comments = cursor.fetchall()
            connection.close()

            return {
                'status': 'green',
                'message': 'Comments retrieved successfully',
                'data': comments
            }

        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail=f"ERROR: {e}")




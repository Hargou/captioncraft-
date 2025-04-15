from litestar import Controller, get, status_codes, post, patch, delete
from litestar.exceptions import HTTPException
from litestar.params import Body
from litestar.datastructures import UploadFile
from litestar.response import Response
import sqlite3

import uuid
import os
from pathlib import Path
from typing import Optional

from src.modules.data_types import DT_PostCreate

postImageFolder = 'src/user_post_images'


class Controller_Post(Controller):

    path = '/post'


    @get("/{postId:int}", status_code=status_codes.HTTP_200_OK)
    async def getPost(self, postId: int) -> dict:
        try:

            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            cursor.execute("""
                SELECT *
                FROM Post
                WHERE id = ?
            """, (postId,))

            queriedPost = cursor.fetchone()

            if queriedPost == None:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f"No post with id: {postId} found")
            
            return {
                'status': 'green',
                'message': 'Post queried successfully',
                'data': queriedPost
            }
        
        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f'ERROR: {e}')


    @get("/{postId:int}/captions", status_code=status_codes.HTTP_200_OK)
    async def getPostCaptions(self, postId: int) -> dict:
        try:
            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            cursor.execute("""
                SELECT *
                FROM Caption
                WHERE postId = ?
                ORDER BY likes DESC, created_at ASC
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
    async def getAllPosts(self, userId: Optional[int] = None) -> dict:
        try:
            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            if userId is not None:
                cursor.execute("""
                    SELECT *
                    FROM Post
                    WHERE userId = ?
                """, (userId,))
            else:
                cursor.execute("""
                    SELECT *
                    FROM Post
                """)

            queriedPosts = cursor.fetchall()

            return {
                'status': 'green',
                'message': 'Post queried successfully',
                'data': queriedPosts
            }
        
        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f'ERROR: {e}')



    @post("/create", status_code=status_codes.HTTP_201_CREATED)
    async def createPost(self, 
        data: DT_PostCreate = Body(media_type="multipart/form-data")
    ) -> dict:
        try:
            # Verify user credentials
            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            cursor.execute("""
                SELECT *
                FROM User
                WHERE id = ? AND password = ?
            """, (data.userId, data.password))

            user = cursor.fetchone()
            if not user:
                raise HTTPException(status_code=status_codes.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

            # Create image directory if it doesn't exist
            os.makedirs(postImageFolder, exist_ok=True)

            # Generate unique filename
            image_extension = os.path.splitext(data.image.filename)[1]
            image_name = f"{data.userId}_{uuid.uuid4()}{image_extension}"
            image_path = os.path.join(postImageFolder, image_name)

            # Save the image
            with open(image_path, 'wb') as f:
                f.write(await data.image.read())

            # Insert post into database
            cursor.execute("""
                INSERT INTO Post (userId, imageName)
                VALUES (?, ?)
            """, (data.userId, image_name))

            post_id = cursor.lastrowid

            # If user provided a caption, create it
            if data.userCaptionText:
                cursor.execute("""
                    INSERT INTO Caption (postId, userId, text)
                    VALUES (?, ?, ?)
                """, (post_id, data.userId, data.userCaptionText))

                caption_id = cursor.lastrowid
                cursor.execute("""
                    UPDATE Post
                    SET topCaptionId = ?
                    WHERE id = ?
                """, (caption_id, post_id))

            connection.commit()
            connection.close()

            return {
                'status': 'green',
                'message': 'Post created successfully',
                'data': {
                    'postId': post_id,
                    'imageName': image_name
                }
            }

        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail=f"ERROR: {e}")


    # /post/postId_userId_password
    @delete('/{postIdUserIdPassword:str}', status_code=status_codes.HTTP_200_OK)
    async def deletePost(self, postIdUserIdPassword: str) -> dict:

        try:

            postIdUserIdPassword = postIdUserIdPassword.split("_")


            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            cursor.execute("SELECT * FROM User WHERE id = ? and password = ? ", (postIdUserIdPassword[1], postIdUserIdPassword[2]))
            queriedUser = cursor.fetchone()

            if queriedUser == None:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f"Unathorized to delete")
            
            
            cursor.execute("SELECT * FROM Post WHERE id = ? and userId = ? ", (postIdUserIdPassword[0], postIdUserIdPassword[1]))
            queriedPost = cursor.fetchone() 

            if queriedPost == None:
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f"Unathorized to delete someone else post")
            

            cursor.execute("DELETE FROM Post WHERE id = ?", (postIdUserIdPassword[0],))
            connection.commit()
            connection.close()
            

            return {
                'status': 'green',
                'message': 'Post deleted',
            }
        
        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f"ERROR: {e}")
    



    @post("/like", status_code=status_codes.HTTP_200_OK)
    async def likePost(self, data: dict) -> dict:
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

            # Check if user already liked the post
            cursor.execute("""
                SELECT *
                FROM UserLikedPosts
                WHERE userId = ? AND postId = ?
            """, (data['userId'], data['postId']))

            existing_like = cursor.fetchone()

            if existing_like:
                # Unlike
                cursor.execute("""
                    DELETE FROM UserLikedPosts
                    WHERE userId = ? AND postId = ?
                """, (data['userId'], data['postId']))

                cursor.execute("""
                    UPDATE Post
                    SET likes = likes - 1
                    WHERE id = ?
                """, (data['postId'],))
            else:
                # Like
                cursor.execute("""
                    INSERT INTO UserLikedPosts (userId, postId)
                    VALUES (?, ?)
                """, (data['userId'], data['postId']))

                cursor.execute("""
                    UPDATE Post
                    SET likes = likes + 1
                    WHERE id = ?
                """, (data['postId'],))

            connection.commit()
            connection.close()

            return {
                'status': 'green',
                'message': 'Post like updated successfully'
            }

        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail=f"ERROR: {e}")

    # Add a route to serve image files
    @get("/user_post_images/{image_name:str}", status_code=status_codes.HTTP_200_OK)
    async def get_post_image(self, image_name: str) -> Response:
        try:
            image_path = os.path.join(postImageFolder, image_name)
            if not os.path.exists(image_path):
                raise HTTPException(status_code=status_codes.HTTP_404_NOT_FOUND, detail=f"Image {image_name} not found")
            
            return Response(content=open(image_path, "rb").read(), media_type="image/jpeg")
        except Exception as e:
            if isinstance(e, HTTPException):
                raise e
            raise HTTPException(status_code=status_codes.HTTP_500_INTERNAL_SERVER_ERROR, detail=f"Error serving image: {e}")
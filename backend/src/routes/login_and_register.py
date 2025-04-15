from litestar import Controller, post, status_codes
from litestar.exceptions import HTTPException
import sqlite3

from src.modules.data_types import DT_UserRegister, DT_UserLogin


class Controller_LoginAndRegister(Controller):
    

    @post('/register', status_code=status_codes.HTTP_201_CREATED)
    async def register(self, data: DT_UserRegister) -> dict:
        try:

            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()


            cursor.execute("""
                SELECT *
                FROM User
                WHERE username = ?
            """, (data.username,))


            userQueried = cursor.fetchone()

            if userQueried != None:
                raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail="Username already exists, choose a differnet one")

            cursor.execute("""
                INSERT INTO
                User (username, name, password, profilePicture)
                    VALUES(?,?,?,?)
            """, (data.username, data.name, data.password, data.profilePicture))

            connection.commit()
            connection.close()

            return {
                'status': 'green',
                'message': 'User successfully created'
            }
        
        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail=f"ERROR: {e}")


    @post('/login', status_code=status_codes.HTTP_200_OK)
    async def login(self, data: DT_UserLogin) -> dict:
        try:

            connection = sqlite3.connect('CapRank.db')
            cursor = connection.cursor()

            cursor.execute("""
                SELECT *
                FROM User
                WHERE username = ? and password = ?
            """, (data.username, data.password))

            userQueried = cursor.fetchone()
            connection.close()


            if userQueried == None:
                raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail="Username or password incorrect")


            return {
                'status': 'green',
                'message': 'User successfully logged in',
                'data': {
                    'id': userQueried[0],
                    'username': userQueried[1],
                    'name': userQueried[2],
                    'password': userQueried[3],
                    'profilePicture': userQueried[4],
                    'created_at': userQueried[5]
                }
            }
        except Exception as e:
            raise HTTPException(status_code=status_codes.HTTP_400_BAD_REQUEST, detail=f"ERROR: {e}")

    
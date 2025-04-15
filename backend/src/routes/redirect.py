from litestar import Controller, get, status_codes
from litestar.exceptions import HTTPException
import sqlite3

class Controller_Redirect(Controller):
    """
    Controller to handle requests using the plural 'posts' path.
    This matches what the Android client is expecting.
    """
    
    path = '/posts'
    
    @get("/{postId:int}/captions", status_code=status_codes.HTTP_200_OK)
    async def posts_captions(self, postId: int) -> dict:
        """Handle requests to /posts/{id}/captions directly"""
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
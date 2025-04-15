from litestar import Litestar, get
from litestar.config.cors import CORSConfig

from src.setupDatabase import setupDatabase

from src.routes.login_and_register import Controller_LoginAndRegister
from src.routes.user import Controller_User
from src.routes.post import Controller_Post
from src.routes.caption import Controller_Caption
from src.routes.redirect import Controller_Redirect

from litestar.static_files.config import StaticFilesConfig
from pathlib import Path

setupDatabase()

@get("/")
async def root() -> dict:
    return {
        "status": "ok",
        "message": "CapRank API is running",
        "version": "1.0.0"
    }

# Define allowed origins for production
ALLOWED_ORIGINS = [
    "http://localhost:8000",  # Development
    "http://localhost:3000",  # Development
    "https://your-production-domain.com"  # Production
]

app = Litestar(
    cors_config=CORSConfig(
        allow_origins=ALLOWED_ORIGINS,
        allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        allow_headers=["Content-Type", "Authorization"],
        allow_credentials=True,
        max_age=3600  # Cache preflight requests for 1 hour
    ),
    route_handlers=[
        root,
        Controller_LoginAndRegister,
        Controller_User,
        Controller_Post,
        Controller_Caption,
        Controller_Redirect
    ],
    static_files_config=[
        StaticFilesConfig(
            directories=[Path("src/user_post_images")],
            path="/user_post_images",
            html_mode=False,
            name="static_files"
        )
    ]
)


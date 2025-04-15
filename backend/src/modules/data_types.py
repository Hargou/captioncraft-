from pydantic import BaseModel, Field, ConfigDict
from litestar.datastructures import UploadFile

from typing import Optional, Annotated


class DT_UserRegister(BaseModel):

    username: Annotated[str, Field(min_length=3, max_length=30)]
    name: Annotated[str, Field(min_length=1, max_length=30)]
    password: Annotated[str, Field(min_length=3, max_length=30)]
    profilePicture: Optional[str] = None


class DT_UserLogin(BaseModel):
    username: Annotated[str, Field(min_length=1)]
    password: Annotated[str, Field(min_length=1)]


class DT_UserUpdate(BaseModel):
    userId: Annotated[int, Field(ge=1)]
    currentPassword: Annotated[str, Field(min_length=1)]

    newUsername: Optional[Annotated[str, Field(min_length=3, max_length=30)]] = None
    newName: Optional[Annotated[str, Field(min_length=1, max_length=30)]] = None
    newPassword: Optional[Annotated[str, Field(min_length=3, max_length=30)]] = None
    newProfilePicture: Optional[str] = None


class DT_UserDelete(BaseModel):
    userId: Annotated[int, Field(ge=1)]
    password: Annotated[str, Field(min_length=1)]



class DT_PostGet(BaseModel):
    postId: Annotated[int, Field(ge=1)]


class DT_PostCreate(BaseModel):
    model_config = ConfigDict(arbitrary_types_allowed=True)

    userId: Annotated[int, Field(ge=1)]
    password: Annotated[str, Field(min_length=1)]
    image: UploadFile
    userCaptionText: Optional[Annotated[str, Field(min_length=1)]] = None


class DT_CaptionCreate(BaseModel):
    postId: int
    userId: int
    password: str
    text: str


class DT_CommentCreate(BaseModel):
    captionId: int
    userId: int
    password: str
    text: str

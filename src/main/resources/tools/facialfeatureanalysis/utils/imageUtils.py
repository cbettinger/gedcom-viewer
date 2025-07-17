import mediapipe as mp
import numpy as np
from PIL import Image


def readImage(filePath, boxPoints=None):
    img = np.asarray(Image.open(filePath))
    if boxPoints:
        minCol, minRow = boxPoints[0]
        maxCol, maxRow = boxPoints[1]
        img = img[minRow:maxRow, minCol:maxCol]
    mpImage = mp.Image(image_format=mp.ImageFormat.SRGB, data=np.array(img))
    return mpImage

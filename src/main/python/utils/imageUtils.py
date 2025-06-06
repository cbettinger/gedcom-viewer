import cv2
import mediapipe as mp
import matplotlib.pyplot as plt
import os
import numpy as np
from PIL import Image

def readImage(filePath, boxPoints=None):
    img = np.asarray(Image.open(filePath))
    if boxPoints:
        minCol, minRow = boxPoints[0]
        maxCol, maxRow = boxPoints[1]
        img = img[minRow:maxRow, minCol:maxCol]
    mpImg = mp.Image(image_format=mp.ImageFormat.SRGB, data=np.array(img))
    return mpImg

def show(img):
    plt.imshow(img)
    plt.show()

def saveImage(img, filePath):
    os.makedirs(os.path.dirname(filePath), exist_ok=True)
    image = Image.fromarray(img.astype(np.uint8))
    image.save(filePath)
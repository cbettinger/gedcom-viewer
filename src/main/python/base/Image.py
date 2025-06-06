from utils import imageUtils
import os

class Image:
    def __init__(self, filePath, boxPoints):
        self.fileName = os.path.basename(filePath)
        self.mpImg = imageUtils.readImage(filePath, boxPoints)  # to get numpy: mpImg.numpy_view()

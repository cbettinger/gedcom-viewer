from utils import imageUtils

class Image:
    def __init__(self, filePath, boxPoints):
        self.fileName = filePath
        self.mpImg = imageUtils.readImage(filePath, boxPoints)

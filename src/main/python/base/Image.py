from utils import imageUtils

class Image:
    def __init__(self, filePath):
        self.fileName = filePath
        self.mpImg = imageUtils.readImage(filePath)

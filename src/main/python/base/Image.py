from utils import imageUtils

class Image:
    def __init__(self, filePath, outputFilePath=None):
        self.fileName = filePath if outputFilePath is None else outputFilePath
        self.mpImg = imageUtils.readImage(filePath)

import mediapipe as mp

def readImage(filePath):
    return mp.Image.create_from_file(filePath)

from landmarkDetection.LandmarkDetector import LandmarkDetector
import numpy as np
import math
from utils.meshTriangles import FACES
import cv2
from utils.mathUtils import getRotationMatrixToAlignVectors

class FaceLandmarks:

    def __init__(self, mpImg):
        normalizedLandmarks = None
        try:
            normalizedLandmarks = np.asarray(LandmarkDetector.detectLandmarks(mpImg))
        except:
            raise Exception("Es konnte kein Gesicht erkannt werden.")

        img = mpImg.numpy_view()

        imgWidth = img.shape[1]
        imgHeight = img.shape[0]

        realLandmarks = []
        imageLandmarks = []
        normLandmarks = []
        dn = max(imgWidth, imgHeight)
        for lm in normalizedLandmarks:
            realLandmarks.append([lm.x*imgWidth/dn, (1.0-lm.y)*imgHeight/dn, lm.z*imgWidth/dn])
            imageLandmarks.append([min(math.floor(lm.x * imgWidth), imgWidth - 1), min(math.floor(lm.y * imgHeight), imgHeight - 1)])
            normLandmarks.append([lm.x, lm.y, lm.z])
  
        self.realLandmarks = np.asarray(realLandmarks)
        self.imageLandmarks = np.asarray(imageLandmarks)
        self.normalizedLandmarks= np.asarray(normLandmarks)

        self._rotateRealLandmarks()

        self.srcImgWidth = imgWidth
        self.srcImgHeight = imgHeight

    def _rotateRealLandmarks(self):
        faceUp = np.subtract(self.realLandmarks[10], self.realLandmarks[152])
        matToAlignUp = getRotationMatrixToAlignVectors(faceUp, [0, 0, 1])

        r = []
        for i in range(len(self.realLandmarks)):
            r.append(np.subtract(self.realLandmarks[i], self.realLandmarks[4]))
            r[i] = np.matmul(matToAlignUp, r[i])

        faceSide = np.subtract(r[263], r[33])
        matToAlignSide = getRotationMatrixToAlignVectors([faceSide[0], faceSide[1], 0], [0, -1, 0])

        self.realLandmarks = [ np.matmul(matToAlignSide, lm) for lm in r ]
        self.realLandmarks = np.asarray(self.realLandmarks)

    def drawMesh(self, indices=np.arange(468), edgeColor=[0, 0, 0], fillColor=[255, 255, 255], bgColor=[255, 255, 255], additionalFaces=None):
        facesToDraw = FACES.tolist()
        if additionalFaces:
            for f in additionalFaces:
                facesToDraw.append(f)
        trianglesToDraw = np.asarray([self.imageLandmarks[np.asarray(f)] for f in facesToDraw if all(np.isin(f, indices))])

        img = None
        if len(bgColor) > 1:
            img = np.full((self.srcImgHeight, self.srcImgWidth, len(bgColor)), bgColor)
        else:
            img = np.full((self.srcImgHeight, self.srcImgWidth), bgColor[0])

        for t in trianglesToDraw:
            cv2.fillPoly(img, pts=[t], color=fillColor)
            cv2.polylines(img, [t], isClosed=True, color=edgeColor)

        return img
    
    def getMask(self, indices=np.arange(468), additionalFaces=None):
        return self.drawMesh(indices, [255], bgColor=[0], additionalFaces=additionalFaces).astype("uint8")

    def drawLines(self, lineIndices, color=[0, 0, 0], bgColor=[255, 255, 255]):
        img = np.full((self.srcImgHeight, self.srcImgWidth, 3), bgColor)
        for line in lineIndices:
            lmLine = np.asarray(self.imageLandmarks[np.asarray(line)])
            cv2.polylines(img, [lmLine], isClosed=False, color=color)
        return img
    
    def getBoundingBox(self, indices=np.arange(468)):
        lms = self.imageLandmarks[np.asarray(indices)]
        xArr = lms[:, 0]
        yArr = lms[:, 1]
        return [[np.min(xArr), np.min(yArr)], [np.max(xArr), np.max(yArr)]]
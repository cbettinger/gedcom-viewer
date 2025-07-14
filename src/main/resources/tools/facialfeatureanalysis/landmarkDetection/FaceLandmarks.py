import math

import numpy as np
from landmarkDetection.LandmarkDetector import LandmarkDetector
from utils.mathUtils import getRotationMatrixToAlignVectors


class FaceLandmarks:

    def __init__(self, mpImg):
        normalizedLandmarks = None
        try:
            normalizedLandmarks = np.asarray(LandmarkDetector.detectLandmarks(mpImg))
        except:
            raise Exception("Failed to detect a face")

        img = mpImg.numpy_view()

        imgWidth = img.shape[1]
        imgHeight = img.shape[0]

        realLandmarks = []
        imageLandmarks = []
        normLandmarks = []
        dn = max(imgWidth, imgHeight)
        for lm in normalizedLandmarks:
            realLandmarks.append(
                [
                    lm.x * imgWidth / dn,
                    (1.0 - lm.y) * imgHeight / dn,
                    lm.z * imgWidth / dn,
                ]
            )
            imageLandmarks.append(
                [
                    min(math.floor(lm.x * imgWidth), imgWidth - 1),
                    min(math.floor(lm.y * imgHeight), imgHeight - 1),
                ]
            )
            normLandmarks.append([lm.x, lm.y, lm.z])

        self.realLandmarks = np.asarray(realLandmarks)
        self.imageLandmarks = np.asarray(imageLandmarks)
        self.normalizedLandmarks = np.asarray(normLandmarks)

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
        matToAlignSide = getRotationMatrixToAlignVectors(
            [faceSide[0], faceSide[1], 0], [0, -1, 0]
        )

        self.realLandmarks = [np.matmul(matToAlignSide, lm) for lm in r]
        self.realLandmarks = np.asarray(self.realLandmarks)

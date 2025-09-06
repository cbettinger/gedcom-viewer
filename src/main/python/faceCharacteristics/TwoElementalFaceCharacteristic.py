from faceCharacteristics.FaceCharacteristic import FaceCharacteristic
from main.python.faceCharacteristics.OneElementalFaceCharacteristic import OneElementalFaceCharacteristic


class TwoElementalFaceCharacteristic(FaceCharacteristic):

    def __init__(self, name, faceLandmarks, name1, name2, landmarkIndices1, landmarkIndices2, allAlignIndex1, allAlignIndex2, zAlignIndex1, zAlignIndex2, additionalFaces1=None, additionalFaces2=None, classifier1=None, classifier2=None):
        super().__init__(name)

        self.firstPart = OneElementalFaceCharacteristic(name1, faceLandmarks, landmarkIndices1, allAlignIndex1, zAlignIndex1, additionalFaces1, classifier1)
        self.secondPart = OneElementalFaceCharacteristic(name2, faceLandmarks, landmarkIndices2, allAlignIndex2, zAlignIndex2, additionalFaces2, classifier2)

    def calculateSimilarityTo(self, other):
        assert(type(self) is type(other))
        return (self.firstPart.calculateSimilarityTo(other.firstPart) + self.secondPart.calculateSimilarityTo(other.secondPart))/2

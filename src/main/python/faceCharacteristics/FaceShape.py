from faceCharacteristics.OneElementalFacePart import OneElementalFacePart
from classifiers.classifiers import XGB_CLASSIFIERS
import numpy as np

class FaceShape(OneElementalFacePart):
    contourLineIndices = [[10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400, 377, 152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109, 10], [103, 68, 70, 35, 117, 50, 207, 214, 210, 211, 32, 140, 176], [332, 298, 300, 265, 346, 280, 427, 434, 430, 431, 262, 369, 400]]
    landmarkIndices = [10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400, 377, 152, 
                       148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109, 68, 70, 
                       35, 117, 50, 207, 214, 210, 211, 32, 140, 298, 300, 265, 346, 280, 427, 434, 430, 431, 262, 369]
    def __init__(self, faceLandmarks):
        self.contourLineIndices = self._generateContourLineIndices(FaceShape.contourLineIndices)
        super().__init__('Gesichtsform', faceLandmarks, FaceShape.landmarkIndices, 10, 152, None, XGB_CLASSIFIERS['faceshape'])

    def _generateContourLineIndices(self, originalIndices):
        if originalIndices is None:
            return None
        lines = []
        newIndexList = np.arange(478)[self.landmarkIndices]
        for line in originalIndices:
            l = []
            for i in line:
                l.append(np.where(newIndexList==i)[0][0])
            lines.append(l)
        return lines

    def _generateRealMesh(self):
        return [None, None] #todo
    
    def _generateEdgesVector(self):
        flatEdges = []
        for line in self.contourLineIndices:
            for i in range(len(line)-1):
                i1 = line[i]
                i2 = line[i+1]
                edge = self.realLandmarks[i1]-self.realLandmarks[i2]
                flatEdges.extend(edge)
        return flatEdges
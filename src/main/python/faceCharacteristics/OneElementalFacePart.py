from faceCharacteristics.FaceCharacteristic import FaceCharacteristic
import numpy as np
from utils import meshTriangles

class OneElementalFacePart(FaceCharacteristic):

    Z_LOW = -1
    def __init__(self, name, faceLandmarks, landmarkIndices,  allAlignIndex, zAlignIndex, additionalFaces=None, classifier=None):
        super().__init__(name)

        self.classifier = classifier
        
        self.landmarkIndices = np.asarray(landmarkIndices)
        self.additionalFaces = additionalFaces

        # so skalieren, dass alle Instanzen eines Gesichtsteils aligned sind
        self.realLandmarks = self._getAlignedRealLandmarks(faceLandmarks.realLandmarks[self.landmarkIndices], landmarkIndices.index(allAlignIndex), landmarkIndices.index(zAlignIndex))
        self.mesh, self.meshTriangles = self._generateRealMesh()
        self.edgesVector = self._generateEdgesVector()

    def _getAlignedRealLandmarks(self, originalRealLandmarks, allAlignIndex, zAlignIndex):
        landmarks = np.asarray(originalRealLandmarks)
        landmarks = np.asarray([np.subtract(l, originalRealLandmarks[allAlignIndex]) for l in landmarks])
        scalingFactor = OneElementalFacePart.Z_LOW / landmarks[zAlignIndex][2]
        landmarks = landmarks * scalingFactor
        return landmarks

    def _generateRealMesh(self):
        vertices = self.realLandmarks
        newIndexList = np.arange(478)[self.landmarkIndices]
        mesh = []
        meshTris = [] 
        for originalFace in meshTriangles.FACES:
            if all(np.isin(originalFace, self.landmarkIndices)):
                i1 = np.where(newIndexList==originalFace[0])[0][0]
                i2 = np.where(newIndexList==originalFace[1])[0][0]
                i3 = np.where(newIndexList==originalFace[2])[0][0]
                mesh.append([vertices[i1], vertices[i2], vertices[i3]])
                meshTris.append([i1, i2, i3])
        if self.additionalFaces:
            for f in self.additionalFaces:
                i1 = np.where(newIndexList==f[0])[0][0]
                i2 = np.where(newIndexList==f[1])[0][0]
                i3 = np.where(newIndexList==f[2])[0][0]
                mesh.append([vertices[i1], vertices[i2], vertices[i3]])
                meshTris.append([i1, i2, i3])
        return mesh, meshTris
    
    def _generateEdgesVector(self):
        flatEdges = []
        doneEdges = []
        for face in self.mesh:
            v0, v1, v2 = face
            a0 = v0.tolist()
            a1 = v1.tolist()
            a2 = v2.tolist()
            if [a0, a1] not in doneEdges:
                edge = v0-v1
                flatEdges.extend(edge)
                doneEdges.extend([[a0, a1], [a1, a0]])
            if [a0, a2] not in doneEdges:
                edge = v0-v2
                flatEdges.extend(edge)
                doneEdges.extend([[a0, a2], [a2, a0]])
            if [a2, a1] not in doneEdges:
                edge = v1-v2
                flatEdges.extend(edge)
                doneEdges.extend([[a1, a2], [a2, a1]])
        return flatEdges

    def calculateSimilarityTo(self, other):
        if self.classifier is None:
            return None
        
        inputData = []
        fx = self.edgesVector
        fy = other.edgesVector
        for i in range(len(fx)):
            inputData.append(fx[i]-fy[i])
        inputData = np.asarray([inputData])

        return self.classifier.getMatchProbability(inputData)[0]
from face.FacePart import FacePart
from face.OneElementalFacePart import OneElementalFacePart


class TwoElementalFacePart(FacePart):

    def __init__(
        self,
        name,
        faceLandmarks,
        name1,
        name2,
        landmarkIndices1,
        landmarkIndices2,
        allAlignIndex1,
        allAlignIndex2,
        zAlignIndex1,
        zAlignIndex2,
        additionalFaces1=None,
        additionalFaces2=None,
        classifier1=None,
        classifier2=None,
    ):
        super().__init__(name)

        self.firstPart = OneElementalFacePart(
            name1,
            faceLandmarks,
            landmarkIndices1,
            allAlignIndex1,
            zAlignIndex1,
            additionalFaces1,
            classifier1,
        )
        self.secondPart = OneElementalFacePart(
            name2,
            faceLandmarks,
            landmarkIndices2,
            allAlignIndex2,
            zAlignIndex2,
            additionalFaces2,
            classifier2,
        )

    def similarity(self, other):
        assert type(self) is type(other)
        return (
            self.firstPart.similarity(other.firstPart)
            + self.secondPart.similarity(other.secondPart)
        ) / 2

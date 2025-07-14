from landmarkDetection.FaceLandmarks import FaceLandmarks
from faceCharacteristics.faceCharacteristics import CHARACTERISTICS


class Face:

    def __init__(self, image, characteristicsToUse):
        self.srcImg = image
        landmarks = FaceLandmarks(image.mpImg)
        self.characteristics = {}
        for c in characteristicsToUse:
            self.characteristics.update({c: CHARACTERISTICS.get(c)(landmarks)})

    def getSimilaritiesTo(self, other):
        assert (
            type(self) is type(other)
            and self.characteristics.keys() == other.characteristics.keys()
        )
        similarities = {}
        for c, o in self.characteristics.items():
            similarities.update(
                {c: o.calculateSimilarityTo(other.characteristics.get(c))}
            )
        return similarities

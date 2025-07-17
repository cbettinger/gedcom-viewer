from faceCharacteristics.faceCharacteristics import CHARACTERISTICS
from landmarkdetection.FaceLandmarks import FaceLandmarks


class Face:
    def __init__(self, image, characteristicsToUse):
        self.image = image
        landmarks = FaceLandmarks(image.mp_image)
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

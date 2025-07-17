from base.config import FACIAL_FEATURES
from faceCharacteristics.faceCharacteristics import CHARACTERISTICS
from landmarkdetection.Landmarks import Landmarks


class Face:
    def __init__(self, image):
        self.image = image

        landmarks = Landmarks(image)

        self.characteristics = {}
        for c in FACIAL_FEATURES:
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

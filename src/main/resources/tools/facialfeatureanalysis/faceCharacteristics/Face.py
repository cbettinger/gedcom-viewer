from faceCharacteristics.faceCharacteristics import CHARACTERISTICS
from landmarkdetection.Landmarks import Landmarks


class Face:
    FEATURES = [
        "CHEEKS",
        "CHIN",
        "EYEBROWS",
        "EYESHAPE",
        "FACESHAPE",
        "LIPS",
        "NOSE",
    ]

    def __init__(self, image):
        self.image = image

        landmarks = Landmarks(image)

        self.characteristics = {}
        for c in Face.FEATURES:
            self.characteristics.update({c: CHARACTERISTICS.get(c)(landmarks)})

    def similarities(self, other):
        assert (
            type(self) is type(other)
            and self.characteristics.keys() == other.characteristics.keys()
        )

        similarities = {}

        for c, o in self.characteristics.items():
            similarities.update({c: o.similarity(other.characteristics.get(c))})

        return similarities

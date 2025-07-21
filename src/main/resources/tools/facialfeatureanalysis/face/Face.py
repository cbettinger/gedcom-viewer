from face.Cheeks import Cheeks
from face.Chin import Chin
from face.Eyebrows import Eyebrows
from face.EyeShape import EyeShape
from face.FaceShape import FaceShape
from face.Lips import Lips
from face.Nose import Nose
from landmarkdetection.Landmarks import Landmarks

class Face:
    FEATURES = {
        "CHEEKS": Cheeks,
        "CHIN": Chin,
        "EYEBROWS": Eyebrows,
        "EYESHAPE": EyeShape,
        "FACESHAPE": FaceShape,
        "LIPS": Lips,
        "NOSE": Nose,
    }

    def __init__(self, image):
        self.image = image

        landmarks = Landmarks(image)

        self.characteristics = {}
        for c in Face.FEATURES.keys():
            self.characteristics.update({c: Face.FEATURES.get(c)(landmarks)})

    def similarities(self, other):
        assert (
            type(self) is type(other)
            and self.characteristics.keys() == other.characteristics.keys()
        )

        similarities = {}

        for c, o in self.characteristics.items():
            similarities.update({c: o.similarity(other.characteristics.get(c))})

        return similarities

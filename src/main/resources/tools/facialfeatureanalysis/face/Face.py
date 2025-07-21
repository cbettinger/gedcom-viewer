from face.Cheeks import Cheeks
from face.Chin import Chin
from face.Eyebrows import Eyebrows
from face.EyeShape import EyeShape
from face.FaceShape import FaceShape
from face.Lips import Lips
from face.Nose import Nose
from landmarks.Landmarks import Landmarks


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
        for feature, characteristic in Face.FEATURES.items():
            self.characteristics.update({feature: characteristic(landmarks)})

    def similarities(self, other):
        assert (
            type(self) is type(other)
            and self.characteristics.keys() == other.characteristics.keys()
        )

        result = {}

        for feature, characteristic in self.characteristics.items():
            result.update(
                {feature: characteristic.similarity(other.characteristics.get(feature))}
            )

        return result

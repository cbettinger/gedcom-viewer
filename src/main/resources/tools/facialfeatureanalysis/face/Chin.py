from classifier.xgb_models.XGBModels import XGB_MODELS
from face.OneElementalFacePart import OneElementalFacePart


class Chin(OneElementalFacePart):
    landmarkIndices = [
        335,
        424,
        431,
        395,
        378,
        406,
        418,
        262,
        369,
        400,
        313,
        421,
        428,
        396,
        377,
        18,
        200,
        199,
        175,
        152,
        83,
        201,
        208,
        171,
        148,
        182,
        194,
        32,
        140,
        176,
        106,
        204,
        211,
        170,
        149,
    ]

    def __init__(self, faceLandmarks):
        super().__init__(
            "Kinn",
            faceLandmarks,
            Chin.landmarkIndices,
            18,
            152,
            classifier=XGB_MODELS["CHIN"],
        )

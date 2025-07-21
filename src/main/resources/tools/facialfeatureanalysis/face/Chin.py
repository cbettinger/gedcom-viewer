from classifier.xgb_models.XGBModels import XGB_MODELS
from face.OneElementalCharacteristic import OneElementalCharacteristic


class Chin(OneElementalCharacteristic):

    indices = [
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

    def __init__(self, landmarks):
        super().__init__(
            "chin",
            landmarks,
            Chin.indices,
            18,
            152,
            classifier=XGB_MODELS["CHIN"],
        )

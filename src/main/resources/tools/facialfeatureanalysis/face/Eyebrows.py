from classifier.xgb_models.XGBModels import XGB_MODELS
from face.TwoElementalFacePart import TwoElementalFacePart


class Eyebrows(TwoElementalFacePart):

    right_indices = [
        70,
        63,
        105,
        66,
        107,
        55,
        65,
        52,
        53,
        46,
        71,
        139,
        156,
        124,
        225,
        224,
        223,
        222,
        221,
        68,
        104,
        69,
        108,
        9,
        8,
    ]

    left_indices = [
        336,
        296,
        334,
        293,
        300,
        276,
        283,
        282,
        295,
        285,
        8,
        9,
        337,
        299,
        333,
        298,
        301,
        368,
        383,
        353,
        445,
        444,
        443,
        442,
        441,
    ]

    right_additional_faces = [[108, 107, 9], [46, 124, 225]]

    left_additional_faces = [[337, 9, 336], [445, 353, 276]]

    def __init__(self, landmarks):
        super().__init__(
            "eyebrows",
            landmarks,
            "right eyebrow",
            "left eyebrow",
            Eyebrows.right_indices,
            Eyebrows.left_indices,
            66,
            296,
            65,
            295,
            Eyebrows.right_additional_faces,
            Eyebrows.left_additional_faces,
            XGB_MODELS["EYEBROWS"]["first"],
            XGB_MODELS["EYEBROWS"]["second"],
        )

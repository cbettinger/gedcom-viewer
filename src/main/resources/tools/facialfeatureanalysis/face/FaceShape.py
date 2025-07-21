from classifier.xgb_models.XGBModels import XGB_MODELS
from face.OneElementalCharacteristic import OneElementalCharacteristic
import numpy as np


class FaceShape(OneElementalCharacteristic):
    contour_indices = [
        [
            10,
            338,
            297,
            332,
            284,
            251,
            389,
            356,
            454,
            323,
            361,
            288,
            397,
            365,
            379,
            378,
            400,
            377,
            152,
            148,
            176,
            149,
            150,
            136,
            172,
            58,
            132,
            93,
            234,
            127,
            162,
            21,
            54,
            103,
            67,
            109,
            10,
        ],
        [103, 68, 70, 35, 117, 50, 207, 214, 210, 211, 32, 140, 176],
        [332, 298, 300, 265, 346, 280, 427, 434, 430, 431, 262, 369, 400],
    ]

    indices = [
        10,
        338,
        297,
        332,
        284,
        251,
        389,
        356,
        454,
        323,
        361,
        288,
        397,
        365,
        379,
        378,
        400,
        377,
        152,
        148,
        176,
        149,
        150,
        136,
        172,
        58,
        132,
        93,
        234,
        127,
        162,
        21,
        54,
        103,
        67,
        109,
        68,
        70,
        35,
        117,
        50,
        207,
        214,
        210,
        211,
        32,
        140,
        298,
        300,
        265,
        346,
        280,
        427,
        434,
        430,
        431,
        262,
        369,
    ]

    def __init__(self, landmarks):
        self.contour_indices = self._generateContourLineIndices(
            FaceShape.contour_indices
        )
        super().__init__(
            "face shape",
            landmarks,
            FaceShape.indices,
            10,
            152,
            None,
            XGB_MODELS["FACESHAPE"],
        )

    def _generateContourLineIndices(self, contour_indices):
        if contour_indices is None:
            return None

        result = []

        new_indices = np.arange(478)[self.indices]

        for line in contour_indices:
            l = []

            for i in line:
                l.append(np.nonzero(new_indices == i)[0][0])

            result.append(l)

        return result

    def _mesh(self):
        return [None, None]

    def _edges(self):
        result = []

        for line in self.contour_indices:
            for i in range(len(line) - 1):
                result.extend(
                    self.real_landmarks[line[i]] - self.real_landmarks[line[i + 1]]
                )

        return result

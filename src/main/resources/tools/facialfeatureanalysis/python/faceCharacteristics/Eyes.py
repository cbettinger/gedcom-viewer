from classifiers.classifiers import XGB_CLASSIFIERS
from faceCharacteristics.TwoElementalFacePart import TwoElementalFacePart


class Eyes(TwoElementalFacePart):
    rightEyeIndices = [33, 7, 163, 144, 145, 153, 154, 155, 133, 246, 161, 160, 159, 158, 157, 173, 468, 469, 471]
    leftEyeIndices = [263, 249, 390, 373, 374, 380, 381, 382, 362, 398, 384, 385, 386, 387, 388, 466, 473, 476, 474]
    rightEyeLidsIndices = [*rightEyeIndices, 113, 130, 25, 110, 24, 23, 22, 26, 112, 243, 190, 56, 28, 27, 29, 30, 247, 226, 31, 228, 229, 230, 231, 232, 233, 244]
    leftEyeLidsIndices = [*leftEyeIndices, 342, 359, 467, 260, 259, 257, 258, 286, 414, 463, 341, 256, 252, 253, 254, 339, 255, 446, 261, 448, 449, 450, 451, 452, 453, 464]
    
    def __init__(self, faceLandmarks):
        super().__init__("Augen", faceLandmarks, "rechtes Auge", "linkes Auge", Eyes.rightEyeLidsIndices, Eyes.leftEyeLidsIndices, 133, 362, 230, 450, None, None, classifier1=XGB_CLASSIFIERS["EYES"]["first"], classifier2=XGB_CLASSIFIERS["EYES"]["second"])

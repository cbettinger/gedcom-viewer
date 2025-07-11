from classifiers.XGBClassifiers import XGB_CLASSIFIERS
from faceCharacteristics.OneElementalFacePart import OneElementalFacePart


class Nose(OneElementalFacePart):
    landmarkIndices = [241, 458, 331, 326, 328, 99, 97, 107, 9, 336, 285, 8, 55, 193, 168, 417, 188, 174, 236, 198, 209, 49, 
                       48, 64, 219, 98, 235, 115, 131, 218, 237, 220, 134, 45, 51, 3, 196, 122, 6, 197, 195, 5, 4, 1, 19, 274, 
                       275, 281, 248, 419, 351, 412, 399, 456, 363, 440, 438, 344, 360, 420, 429, 279, 278, 294,
                       60, 239, 79, 166, 59, 75, 44, 238, 125, 242, 2, 141, 94, 240, 102, 
                       20, 370, 354, 462, 250, 461, 459, 457, 290, 309, 439, 455, 392, 289, 305, 460, 327, 
                        358, 129]
    additionalFaces = [[417, 351, 412], [193, 188, 122]]
    
    def __init__(self, faceLandmarks):
        super().__init__("Nase", faceLandmarks, Nose.landmarkIndices, 168, 2, Nose.additionalFaces, XGB_CLASSIFIERS["NOSE"])

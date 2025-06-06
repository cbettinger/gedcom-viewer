from faceCharacteristics.TwoElementalFacePart import TwoElementalFacePart
from classifiers.classifiers import XGB_CLASSIFIERS

class Eyebrows(TwoElementalFacePart):
    leftIndices = [336, 296, 334, 293, 300, 276, 283, 282, 295, 285, 8, 9, 337, 299, 333, 298, 301, 368, 383, 353, 445, 444, 443, 442, 441]
    rightIndices = [70, 63, 105, 66, 107, 55, 65, 52, 53, 46, 71, 139, 156, 124, 225, 224, 223, 222, 221, 68, 104, 69, 108, 9, 8]

    leftAdditionalFaces = [[337, 9, 336], [445, 353, 276]]
    rightAdditionalFaces = [[108, 107, 9], [46, 124, 225]]

    def __init__(self, faceLandmarks):
        super().__init__('Augenbrauen', faceLandmarks, 'rechte Augenbraue', 'linke Augenbraue', Eyebrows.rightIndices, Eyebrows.leftIndices, 66, 296, 65, 295, Eyebrows.rightAdditionalFaces, Eyebrows.leftAdditionalFaces, classifier1=XGB_CLASSIFIERS['EYEBROWS']['first'], classifier2=XGB_CLASSIFIERS['EYEBROWS']['second'])
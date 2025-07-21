from face.Characteristic import Characteristic
from face.OneElementalCharacteristic import OneElementalCharacteristic


class TwoElementalCharacteristic(Characteristic):

    def __init__(
        self,
        name,
        landmarks,
        name_right,
        name_left,
        landmark_indices_right,
        landmark_indices_left,
        all_align_index_right,
        all_align_index_left,
        z_align_index_right,
        z_align_index_left,
        additional_faces_right=None,
        additional_faces_left=None,
        classifier_right=None,
        classifier_left=None,
    ):
        super().__init__(name)

        self.firstPart = OneElementalCharacteristic(
            name_right,
            landmarks,
            landmark_indices_right,
            all_align_index_right,
            z_align_index_right,
            additional_faces_right,
            classifier_right,
        )
        self.secondPart = OneElementalCharacteristic(
            name_left,
            landmarks,
            landmark_indices_left,
            all_align_index_left,
            z_align_index_left,
            additional_faces_left,
            classifier_left,
        )

    def similarity(self, other):
        assert type(self) is type(other)
        return (
            self.firstPart.similarity(other.firstPart)
            + self.secondPart.similarity(other.secondPart)
        ) / 2

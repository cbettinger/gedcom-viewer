from face.Characteristic import Characteristic
from face.OneElementalCharacteristic import OneElementalCharacteristic


class TwoElementalCharacteristic(Characteristic):

    def __init__(
        self,
        name,
        landmarks,
        name_1,
        name_2,
        landmark_indices_1,
        landmark_indices_2,
        all_align_index_1,
        all_align_index_2,
        z_align_index_1,
        z_align_index_2,
        additional_faces_1=None,
        additional_faces_2=None,
        classifier_1=None,
        classifier_2=None,
    ):
        super().__init__(name)

        self.element_1 = OneElementalCharacteristic(
            name_1,
            landmarks,
            landmark_indices_1,
            all_align_index_1,
            z_align_index_1,
            additional_faces_1,
            classifier_1,
        )

        self.element_2 = OneElementalCharacteristic(
            name_2,
            landmarks,
            landmark_indices_2,
            all_align_index_2,
            z_align_index_2,
            additional_faces_2,
            classifier_2,
        )

    def similarity(self, other):
        assert type(self) is type(other)
        return (
            self.element_1.similarity(other.element_1)
            + self.element_2.similarity(other.element_2)
        ) / 2

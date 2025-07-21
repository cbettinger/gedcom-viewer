from face.FacePart import FacePart
from face.OneElementalFacePart import OneElementalFacePart


class TwoElementalFacePart(FacePart):

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

        self.firstPart = OneElementalFacePart(
            name_right,
            landmarks,
            landmark_indices_right,
            all_align_index_right,
            z_align_index_right,
            additional_faces_right,
            classifier_right,
        )
        self.secondPart = OneElementalFacePart(
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

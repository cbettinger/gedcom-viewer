from mediapipe.tasks import python
from mediapipe.tasks.python import vision


class LandmarkDetector:
    DETECTOR = vision.FaceLandmarker.create_from_options(
        vision.FaceLandmarkerOptions(
            base_options=python.BaseOptions(
                model_asset_path="landmarkdetection/face.task"
            ),
            output_face_blendshapes=True,
            output_facial_transformation_matrixes=True,
            num_faces=1,
        )
    )

    @classmethod
    def execute(cls, mpImage):
        return cls.DETECTOR.detect(mpImage).face_landmarks[0]

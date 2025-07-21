from mediapipe.tasks import python
from mediapipe.tasks.python import vision


class LandmarkDetector:
    DETECTOR = vision.FaceLandmarker.create_from_options(
        vision.FaceLandmarkerOptions(
            base_options=python.BaseOptions(
                model_asset_path="landmarks/face.task"
            ),
            output_face_blendshapes=True,
            output_facial_transformation_matrixes=True,
            num_faces=1,
        )
    )

    @classmethod
    def execute(cls, image):
        return cls.DETECTOR.detect(image.mp_image).face_landmarks[0]

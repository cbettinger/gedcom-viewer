from mediapipe.tasks import python
from mediapipe.tasks.python import vision


class LandmarkDetector:

    BASE_OPTIONS = python.BaseOptions(model_asset_path="face_landmarker.task")
    OPTIONS = vision.FaceLandmarkerOptions(base_options=BASE_OPTIONS,
                                        output_face_blendshapes=True,
                                        output_facial_transformation_matrixes=True,
                                        num_faces=1)
    DETECTOR = vision.FaceLandmarker.create_from_options(OPTIONS)

    @classmethod
    def detectLandmarks(cls, mpImg):
        return cls.DETECTOR.detect(mpImg).face_landmarks[0]

from landmarks.LandmarkDetector import LandmarkDetector
import math
import numpy as np


class Landmarks:
    def __init__(self, image):
        landmarks = None
        try:
            landmarks = np.asarray(LandmarkDetector.execute(image))
        except:
            raise Exception("Failed to detect a face")

        img = image.mp_image.numpy_view()
        img_width = img.shape[1]
        img_height = img.shape[0]

        dn = max(img_width, img_height)

        real_landmarks = []
        img_landmarks = []
        norm_landmarks = []

        for lm in landmarks:
            real_landmarks.append(
                [
                    lm.x * img_width / dn,
                    (1.0 - lm.y) * img_height / dn,
                    lm.z * img_width / dn,
                ]
            )
            img_landmarks.append(
                [
                    min(math.floor(lm.x * img_width), img_width - 1),
                    min(math.floor(lm.y * img_height), img_height - 1),
                ]
            )
            norm_landmarks.append([lm.x, lm.y, lm.z])

        self.real_landmarks = np.asarray(real_landmarks)
        self.img_landmarks = np.asarray(img_landmarks)
        self.norm_landmarks = np.asarray(norm_landmarks)

        self._rotate_real_landmarks()

    def _rotate_real_landmarks(self):
        face_up = np.subtract(self.real_landmarks[10], self.real_landmarks[152])
        align_up_matrix = Landmarks._get_rotation_matrix(face_up, [0, 0, 1])

        r = []
        for i in range(len(self.real_landmarks)):
            r.append(np.subtract(self.real_landmarks[i], self.real_landmarks[4]))
            r[i] = np.matmul(align_up_matrix, r[i])

        face_side = np.subtract(r[263], r[33])
        align_side_matrix = Landmarks._get_rotation_matrix(
            [face_side[0], face_side[1], 0], [0, -1, 0]
        )

        self.real_landmarks = [np.matmul(align_side_matrix, lm) for lm in r]
        self.real_landmarks = np.asarray(self.real_landmarks)

    @classmethod
    def _get_rotation_matrix(cls, src, dest):
        a, b = (src / np.linalg.norm(src)).reshape(3), (
            dest / np.linalg.norm(dest)
        ).reshape(3)
        v = np.cross(a, b)
        c = np.dot(a, b)
        s = np.linalg.norm(v)
        kmat = np.array([[0, -v[2], v[1]], [v[2], 0, -v[0]], [-v[1], v[0], 0]])
        return np.eye(3) + kmat + kmat.dot(kmat) * ((1 - c) / (s**2))

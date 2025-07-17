from PIL import Image as PILImage
import mediapipe as mp
import numpy as np


class Image:
    def __init__(self, filepath, clip):
        self.filepath = filepath
        self.mp_image = Image._read(filepath, clip)

    @classmethod
    def _read(cls, filepath, clip=None):
        image_data = np.asarray(PILImage.open(filepath))

        if clip:
            x1, y1 = clip[0]
            x2, y2 = clip[1]
            image_data = image_data[y1:y2, x1:x2]

        return mp.Image(image_format=mp.ImageFormat.SRGB, data=np.array(image_data))

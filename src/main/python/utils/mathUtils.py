import numpy as np

def getRotationMatrixToAlignVectors(src, dst):
    a, b = (src / np.linalg.norm(src)).reshape(3), (dst / np.linalg.norm(dst)).reshape(3)
    v = np.cross(a, b)
    c = np.dot(a, b)
    s = np.linalg.norm(v)
    kmat = np.array([[0, -v[2], v[1]], [v[2], 0, -v[0]], [-v[1], v[0], 0]])
    rotationMatrix = np.eye(3) + kmat + kmat.dot(kmat) * ((1 - c) / (s ** 2))
    return rotationMatrix

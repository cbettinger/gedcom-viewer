import numpy as np

def getRotationMatrixToAlignVectors(src, dst):
    a, b = (src / np.linalg.norm(src)).reshape(3), (dst / np.linalg.norm(dst)).reshape(3)
    v = np.cross(a, b)
    s = np.linalg.norm(v)
    k = v / s
    c = np.dot(a, b)
    kmat = np.array([[0, -k[2], k[1]], [k[2], 0, -k[0]], [-k[1], k[0], 0]])
    rotationMatrix = np.eye(3) + s*kmat + (1-c) * kmat.dot(kmat)
    return rotationMatrix

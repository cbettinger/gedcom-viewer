from base.config import FACE_CHARACTERISTICS_OF_INTEREST
from base.FaceAnalyser import FaceAnalyser
from utils import dictUtils

def getAvgPathSimilarity(path, personSimilarities):
    numEntries = len(path)
    pathSim = 0
    for individualID in path:
        s = personSimilarities.get(individualID)
        if s is None:
            numEntries -=1
        else:
            pathSim += s
    return pathSim/numEntries

def getFaceAnalysisResult(targetPerson, maxDepth=None):
    similarityResults = FaceAnalyser.analyse(targetPerson, maxDepth)
    
    if similarityResults is None:
        return {"isError": "y", "messageKey": "NotEnoughUsablePortraits"} #"Die Zielperson konnte nicht analysiert werden. Es müssen von der Person selbst sowie von beiden Elternteilen Portraits vohanden sein, auf denen ein Gesicht durch das System erkannt werden kann, um eine Ähnlichkeitsanalyse durchzuführen."
    paths = targetPerson.getPaths()

    nodes = dictUtils.getDicts(FACE_CHARACTERISTICS_OF_INTEREST)
    pathSimilarities = dictUtils.getDicts(FACE_CHARACTERISTICS_OF_INTEREST)

    for c in FACE_CHARACTERISTICS_OF_INTEREST:
        avgPersonSimilarities = {}

        for id, personResults in similarityResults.items():
            maxSimRes = personResults["max"][c]
            avgSim = personResults["avg"][c]

            avgPersonSimilarities.update({id: avgSim})

            if maxSimRes.value is None:
                nodes[c].update({id: {"maxSimilarity": None, "avgSimilarity": None}})
            else:
                individualResult = {"maxSimilarity": maxSimRes.value, "avgSimilarity": avgSim, "maxMatchImgTarget": maxSimRes.img1.fileName, "maxMatchImgAncestor": maxSimRes.img2.fileName}
                nodes[c].update({id: individualResult})
        
        for path in paths:
            pathSimilarities[c].update({str(path).replace("[", "").replace("]", ""): getAvgPathSimilarity(path, avgPersonSimilarities)})

    return {"pathSimilarities": pathSimilarities, "nodes": nodes}
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
    similarityToTargetResults = FaceAnalyser.getSimilaritiesToTargetPerson(targetPerson, maxDepth)
    similarityToOwnChildResults = FaceAnalyser.getSimilaritiesToOwnChildInLineToTarget(targetPerson, maxDepth)
    
    if similarityToTargetResults is None:
        return {"isError": "y", "messageKey": "NotEnoughUsablePortraits"}
    paths = targetPerson.getComparablePaths(maxDepth)

    nodes = dictUtils.getDicts(FACE_CHARACTERISTICS_OF_INTEREST)
    pathSimilarities = dictUtils.getDicts(FACE_CHARACTERISTICS_OF_INTEREST)
    edgeSimilarities = dictUtils.getDicts(FACE_CHARACTERISTICS_OF_INTEREST)

    for c in FACE_CHARACTERISTICS_OF_INTEREST:
        avgPersonSimilaritiesToOwnChild = {}

        for id, personResults in similarityToTargetResults.items():
            maxSimRes = personResults["max"][c]
            avgSim = personResults["avg"][c]

            if id in similarityToOwnChildResults.keys():
                simToChild = similarityToOwnChildResults[id][c]
                avgPersonSimilaritiesToOwnChild.update({id: simToChild})
                edgeSimilarities[c].update({id: str(simToChild)})

            if maxSimRes is None:
                nodes[c].update({id: ""})
            else:
                individualResult = {"maxSimilarity": str(maxSimRes.value), "avgSimilarity": str(avgSim), "maxMatchImgTarget": maxSimRes.img1.fileName, "maxMatchImgAncestor": maxSimRes.img2.fileName}
                nodes[c].update({id: individualResult})
        
        for path in paths:
            pathSimilarities[c].update({str(path).replace("[", "").replace("]", "").replace("'", ""): str(getAvgPathSimilarity(path, avgPersonSimilaritiesToOwnChild))})

    return {"pathSimilarities": pathSimilarities, "nodes": nodes, "edgeSimilarities": edgeSimilarities}
from base.MaxSimilarityResult import MaxSimilarityResult
from base.config import MAX_COMPARISON_DEPTH, FACE_CHARACTERISTICS_OF_INTEREST
from utils import dictUtils

class FaceAnalyser:

    @classmethod
    def analyse(cls, rootPerson, maxDepth=MAX_COMPARISON_DEPTH):
        if not rootPerson.hasFaces() or not FaceAnalyser._isNextGenerationComplete(rootPerson):
            return None, None, None
        
        directSimilaritiesToTarget = FaceAnalyser.getSimilaritiesToTargetPerson(rootPerson, maxDepth)
        
        depth = 0
        similaritiesToChildren = {}
        correctedSimilaritiesToTarget = {}
        individualsToCheck = [rootPerson]

        while depth < maxDepth:
            depth += 1
            nextIndividualsToCheck = []

            for i in range(len(individualsToCheck)):
                p = individualsToCheck[i]
                if FaceAnalyser._isNextGenerationComplete(p):
                    fatherId = p.parent1.value
                    _, sAvgFather = FaceAnalyser._getSimilaritiesToIndividual(p, p.parent1)   
                    similaritiesToChildren.update({fatherId: sAvgFather})
                    motherId = p.parent2.value
                    _, sAvgMother = FaceAnalyser._getSimilaritiesToIndividual(p, p.parent2)   
                    similaritiesToChildren.update({motherId: sAvgMother})

                    if depth == 1:
                        correctedSimilaritiesToTarget.update({fatherId: sAvgFather})
                        correctedSimilaritiesToTarget.update({motherId: sAvgMother})
                    else:
                        correctedSimFather = FaceAnalyser._getCorrectedSimilarity(directSimilaritiesToTarget, similaritiesToChildren, fatherId, p.value, depth, correctedSimilaritiesToTarget)
                        correctedSimilaritiesToTarget.update({fatherId: correctedSimFather})
                        correctedSimMother = FaceAnalyser._getCorrectedSimilarity(directSimilaritiesToTarget, similaritiesToChildren, motherId, p.value, depth, correctedSimilaritiesToTarget)
                        correctedSimilaritiesToTarget.update({motherId: correctedSimMother})

                    nextIndividualsToCheck.extend([p.parent1, p.parent2])

            individualsToCheck = nextIndividualsToCheck

        return directSimilaritiesToTarget, similaritiesToChildren, correctedSimilaritiesToTarget
    
    @classmethod
    def _isNextGenerationComplete(cls, rootPerson):
        return rootPerson.parent1 and rootPerson.parent1.hasFaces() and rootPerson.parent2 and rootPerson.parent2.hasFaces()

    @classmethod 
    def getSimilaritiesToTargetPerson(cls, targetPerson, maxDepth=MAX_COMPARISON_DEPTH):
        if not targetPerson.hasFaces() or not cls._isComparable(targetPerson.parent1) or not cls._isComparable(targetPerson.parent2):
            return None
        
        depth = 0
        similarities = {}
        individualsToCheck = [targetPerson.parent1, targetPerson.parent2]

        while depth < maxDepth:
            depth += 1
            nextIndividualsToCheck = []

            for i in range(len(individualsToCheck)):
                p = individualsToCheck[i]
                sMax, sAvg = cls._getSimilaritiesToIndividual(targetPerson, p)   
                similarities.update({p.value: {"max": sMax, "avg": sAvg}})
                
                if p.parent1:
                    nextIndividualsToCheck.append(p.parent1)
                if p.parent2:
                    nextIndividualsToCheck.append(p.parent2)

            individualsToCheck = nextIndividualsToCheck

        return similarities

    @classmethod
    def _isComparable(cls, other):
        return other is not None and other.hasFaces()
    
    @classmethod
    def _getSimilaritiesToIndividual(cls, targetPerson, other):
        maxSimilarities = dictUtils.getZeros(FACE_CHARACTERISTICS_OF_INTEREST)
        mostSimilarFaces = {}
        maxResult = {}

        avgResult = {}
        threeMaxSimilarities = dictUtils.getLists(FACE_CHARACTERISTICS_OF_INTEREST)

        if cls._isComparable(other):
            for ownFace in targetPerson.faces:
                for otherFace in other.faces:
                    similarities = ownFace.getSimilaritiesTo(otherFace)
                    for c in ownFace.characteristics.keys():
                        s = similarities.get(c)
                        if s > maxSimilarities.get(c):
                            maxSimilarities.update({c: s})
                            mostSimilarFaces.update({c: [ownFace, otherFace]})
                        if len(threeMaxSimilarities[c]) < 3:
                            threeMaxSimilarities[c].append(s)
                            threeMaxSimilarities[c].sort(reverse=True)
                        elif s > threeMaxSimilarities[c][2]:
                            threeMaxSimilarities[c][2] = s
                            threeMaxSimilarities[c].sort(reverse=True)

        for c, s in maxSimilarities.items():
            faces = mostSimilarFaces.get(c)
            if faces is None:
                maxResult.update({c: None})
                avgResult.update({c: None})
            else:
                f1, f2 = faces
                bbox1 = f1.characteristics.get(c).getImageBoundingBox()
                bbox2 = f2.characteristics.get(c).getImageBoundingBox()
                maxResult.update({c: MaxSimilarityResult(s, f1.srcImg, f2.srcImg, bbox1, bbox2)})
                avgResult.update({c: sum(threeMaxSimilarities[c])/len(threeMaxSimilarities[c])})

        return maxResult, avgResult
    
    @classmethod
    def _getCorrectedSimilarity(cls, simsToTarget, simsToChildren, currentId, childId, depth, correctedSims):
        simToTarget = simsToTarget[currentId]
        simToChild = simsToChildren[currentId]
        correctedChildSim = correctedSims[childId]
        correctedSim = dictUtils.getZeros(FACE_CHARACTERISTICS_OF_INTEREST)
        correctionFactor = 1/2**(depth)
        for c in FACE_CHARACTERISTICS_OF_INTEREST:
            corrected = (correctedChildSim[c] * simToChild[c] + correctionFactor * simToTarget["avg"][c]) / (1 + correctionFactor)
            correctedSim[c] = corrected
        return correctedSim
    
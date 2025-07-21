from base import DictUtils
from base.Individual import Individual
from base.Similarity import Similarity
from face.Face import Face
import json
import sys


def _avg_line_similarity(line, similarities):
    num_entries = len(line)

    line_similarity = 0

    for id in line:
        s = similarities.get(id)
        if s is None:
            num_entries -= 1
        else:
            line_similarity += s

    return line_similarity / num_entries


def _results(results, proband, max_depth):
    similarities = DictUtils.dicts(Face.FEATURES)
    line_similarities = DictUtils.dicts(Face.FEATURES)

    paths = proband.paths(max_depth)

    for feature in Face.FEATURES:
        avg_similarities = {}

        for id, result in results.items():
            avg_similarity = result["avg"][feature]
            max_similarity = result["max"][feature]

            avg_similarities.update({id: avg_similarity})

            if max_similarity.value is None:
                similarities[feature].update({id: ""})
            else:
                similarities[feature].update(
                    {
                        id: {
                            "avg_similarity": str(avg_similarity),
                            "max_similarity": str(max_similarity.value),
                            "max_similar_proband_portrait_filepath": max_similarity.image1.filepath,
                            "max_similar_ancestor_portrait_filepath": max_similarity.image2.filepath,
                        }
                    }
                )

        for path in paths:
            line_similarities[feature].update(
                {
                    str(path)
                    .replace("[", "")
                    .replace("]", "")
                    .replace("'", ""): str(_avg_line_similarity(path, avg_similarities))
                }
            )

    return {"similarities": similarities, "line_similarities": line_similarities}


def _is_comparable(other):
    return other is not None and other.hasFaces()


def _similarities(proband, other):
    max_result = {}
    avg_result = {}

    done = []

    max_similarities = DictUtils.zeros(Face.FEATURES)
    avg_similarities = DictUtils.zeros(Face.FEATURES)

    most_similar_faces = {}

    avg_max_similarities = DictUtils.zeros(Face.FEATURES)

    if _is_comparable(other):
        for proband_face in proband.faces:
            max_sims = DictUtils.zeros(Face.FEATURES)

            for other_face in other.faces:
                if proband_face is other_face or {proband_face, other_face} in done:
                    continue

                similarities = proband_face.similarities(other_face)

                for c in proband_face.characteristics.keys():
                    s = similarities.get(c)

                    avg_similarities[c] += s

                    if s > max_similarities.get(c):
                        max_similarities.update({c: s})
                        most_similar_faces.update({c: [proband_face, other_face]})

                    if s > max_sims[c]:
                        max_sims[c] = s

                done.append({proband_face, other_face})

            for c in avg_max_similarities.keys():
                avg_max_similarities[c] += max_sims[c]

    for c, s in max_similarities.items():
        faces = most_similar_faces.get(c)

        if faces is None:
            max_result.update({c: Similarity(None, None, None)})
            avg_result.update({c: None})
        else:
            f1, f2 = faces
            max_result.update({c: Similarity(f1.image, f2.image, s)})
            avg_result.update({c: avg_similarities[c] / len(done)})

    return {"max": max_result, "avg": avg_result}


def _analyse(proband, max_depth):
    if (
        not proband.hasFaces()
        or not _is_comparable(proband.father)
        or not _is_comparable(proband.mother)
    ):
        return {"error": True, "message": "Insufficient number of portraits"}

    result = {}
    depth = 0
    queue = [proband.father, proband.mother]

    while depth < max_depth:
        next_level_queue = []

        for i in range(len(queue)):
            other = queue[i]
            result.update({other.value: _similarities(proband, other)})

            if other.father:
                next_level_queue.append(other.father)
            if other.mother:
                next_level_queue.append(other.mother)

        queue = next_level_queue
        depth += 1

    return _results(result, proband, max_depth)


def _on_success(filepath):
    print(json.dumps({"success": True, "filepath": filepath}))
    sys.exit(0)


def _on_error(message):
    print(json.dumps({"error": True, "message": message}))
    sys.exit(1)


if len(sys.argv) < 4:
    _on_error("Invalid number of arguments")
else:
    proband = Individual.parse(sys.argv[1], int(sys.argv[2]))

    if len(sys.argv) > 4:
        id = sys.argv[4]
        try:
            proband = Individual.LIST[id]
        except:
            _on_error("Proband {} not found".format(id))

    depth = int(sys.argv[3])

    results = _analyse(proband, depth)
    if "error" in results and "message" in results:
        _on_error(results.message)

    filepath = sys.argv[1].replace(".json", "-result.json")
    with open(filepath, "w", encoding="utf-8") as file:
        json.dump(results, file, ensure_ascii=False, indent=4)

    _on_success(filepath)

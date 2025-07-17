from base.faceComparison import getFaceAnalysisResult
from base.Person import Person
import json
import sys


if len(sys.argv) < 4:
    print(json.dumps({"error": True, "message": "Invalid arguments"}))
else:
    rootPerson = Person.parse(sys.argv[1], int(sys.argv[2]))
    if len(sys.argv) > 4:
        try:
            rootPerson = Person.PERSONS[sys.argv[4]]
        except:
            sys.exit("No individual {} found".format(sys.argv[4]))

    result = getFaceAnalysisResult(rootPerson, int(sys.argv[3]))

    if "error" in result:
        print(json.dumps(result))
    else:
        filepath = sys.argv[1].replace(".json", "-result.json")
        with open(filepath, "w", encoding="utf-8") as file:
            json.dump(result, file, ensure_ascii=False, indent=4)

        print(json.dumps({"success": True, "filepath": filepath}))

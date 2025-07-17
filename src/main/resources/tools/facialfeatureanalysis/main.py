from base.faceComparison import getFaceAnalysisResult
from base.Individual import Individual
import json
import sys


if len(sys.argv) < 4:
    print(json.dumps({"error": True, "message": "Invalid arguments"}))
    sys.exit(1)
else:
    proband = Individual.parse(sys.argv[1], int(sys.argv[2]))
    if len(sys.argv) > 4:
        try:
            proband = Individual.LIST[sys.argv[4]]
        except:
            print(
                json.dumps(
                    {
                        "error": True,
                        "message": "Proband {} not found".format(sys.argv[4]),
                    }
                )
            )
            sys.exit(1)

    result = getFaceAnalysisResult(proband, int(sys.argv[3]))

    if "error" in result:
        print(json.dumps(result))
        sys.exit(1)
    else:
        filepath = sys.argv[1].replace(".json", "-result.json")
        with open(filepath, "w", encoding="utf-8") as file:
            json.dump(result, file, ensure_ascii=False, indent=4)

        print(json.dumps({"success": True, "filepath": filepath}))
        sys.exit(0)

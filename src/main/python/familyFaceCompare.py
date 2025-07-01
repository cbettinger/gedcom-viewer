import sys
import utils.parseJSON as parseJSON
from base.faceComparison import getFaceAnalysisResult
from base.Person import Person
import json

if len(sys.argv) < 4:
    print(json.dumps({"isError": "y", "messageKey": "NotEnoughParameters"}))
else:
    rootPerson = parseJSON.parseFile(sys.argv[1], int(sys.argv[2]))
    if len(sys.argv) > 4:
        try:
            rootPerson = Person.PERSONS[sys.argv[4]]
        except:
            sys.exit("Es existiert kein Individuum mit ID {}.".format(sys.argv[4]))

    filename = sys.argv[1].replace(".json", "-result.json")
    result = getFaceAnalysisResult(rootPerson, int(sys.argv[3]))
    if "isError" in result:
        print(json.dumps(result))
    else:
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(result, f, ensure_ascii=False, indent=4)

        print(json.dumps({"success": "y", "filename": filename}))

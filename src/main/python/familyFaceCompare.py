import sys
import os
import utils.parseJSON as parseJSON
from base.faceComparison import getFaceAnalysisResult
from base.Person import Person
import json

if len(sys.argv) < 4:
    print(json.dumps({"isError": "y", "messageKey": "NotEnoughParameters"})) #sys.exit("Es wurden nicht alle benötigten Parameter mitgegeben. Es muss ein zu verwendender Stammbaum-JSON-String angegeben werden. Zusätzlich muss die maximale Anzahl Portraits pro Person sowie die maximale Anzahl Generationen für die Analyse spezifiziert werden. Optional kann zusätzlich die ID der zu analysierenden Person mitgegeben werden.")

rootPerson = parseJSON.parseFile(sys.argv[1], int(sys.argv[2]))
if len(sys.argv) > 4:
    try:
        rootPerson = Person.PERSONS[sys.argv[4]]
    except:
        sys.exit("Es existiert kein Individuum mit ID {}.".format(sys.argv[4]))

filename = sys.argv[1].replace(".json", "-result.json")

outputFile = open(filename, "w", encoding="utf-8")
outputFile.write(json.dumps(str(getFaceAnalysisResult(rootPerson, int(sys.argv[3])))))
outputFile.close()
print(json.dumps({"success": "y", "filename": filename}))

# python src/familyfacecompare.py testfiles/sobisiak-miriam-minimal.json > testResults/sobisiak-miriam-minimal-newBase-avg.txt
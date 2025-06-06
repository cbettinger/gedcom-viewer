import sys
import os
import utils.parseJSON as parseJSON
from base.faceComparison import getFaceAnalysisResult
from base.Person import Person

if len(sys.argv) < 4:
    print({'isError': True, 'messageKey': 'NotEnoughParameters'}) #sys.exit('Es wurden nicht alle benötigten Parameter mitgegeben. Es müssen ein zu verwendender Stammbaum-JSON-String und der relative Pfad vom Python-Skript zu den Bilddateien angegeben werden. Zusätzlich muss die maximale Anzahl Portraits pro Person sowie die maximale Anzahl Generationen für die Analyse spezifiziert werden. Optional kann zusätzlich die ID der zu analysierenden Person mitgegeben werden.')

rootPerson = parseJSON.parseString(sys.argv[1], int(sys.argv[3]))
if len(sys.argv) > 4:
    try:
        rootPerson = Person.PERSONS[sys.argv[4]]
    except:
        sys.exit('Es existiert kein Individuum mit ID {}.'.format(sys.argv[4]))

print(getFaceAnalysisResult(rootPerson, int(sys.argv[3])))

# python src/familyfacecompare.py testfiles/sobisiak-miriam-minimal.json > testResults/sobisiak-miriam-minimal-newBase-avg.txt
def getZeros(keys):
    d = {}
    for k in keys:
        d.update({k: 0})
    return d

def getLists(keys):
    d = {}
    for k in keys:
        d.update({k: []})
    return d

def getDicts(keys):
    d = {}
    for k in keys:
        d.update({k: {}})
    return d
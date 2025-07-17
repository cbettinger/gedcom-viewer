def zeros(keys):
    d = {}

    for k in keys:
        d.update({k: 0})

    return d


def dicts(keys):
    d = {}

    for k in keys:
        d.update({k: {}})

    return d

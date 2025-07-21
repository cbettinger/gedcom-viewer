def zeros(dict):
    result = {}

    for key in dict.keys():
        result.update({key: 0})

    return result


def dicts(dict):
    result = {}

    for key in dict.keys():
        result.update({key: {}})

    return result

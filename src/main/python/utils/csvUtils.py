import pandas as pd

def loadClassificationDataFromCSV(filePath):
    df = pd.read_csv(filePath, sep='\t', header=None)
    y = df.iloc[:, 0]
    x = df.iloc[:, 1:]
    return x.to_numpy(), y.to_numpy()
import pandas as pd


def load_classification_data(filepath):
    df = pd.read_csv(filepath, sep="\t", header=None)
    y = df.iloc[:, 0]
    x = df.iloc[:, 1:]
    return x.to_numpy(), y.to_numpy()

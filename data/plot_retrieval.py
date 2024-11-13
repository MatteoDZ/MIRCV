import numpy as np
import matplotlib.pyplot as plt

# Configurazioni
configurations = ['With Compression \nWith Preprocessing', 'Without Compression \nWith Preprocessing']

# Metodi
methods = ['BM25_DAAT', 'BM25_DP', 'TFIDF_DAAT', 'TFIDF_DP']

# Valori P@10
P_at_10 = {
    'BM25_DAAT': [0.5488, 0.5488],
    'BM25_DP': [0.5488, 0.5488],
    'TFIDF_DAAT': [0.5233, 0.5233],
    'TFIDF_DP': [0.5233, 0.5233]
}

# Valori R@10
R_at_10 = {
    'BM25_DAAT': [0.1103 , 0.1103],
    'BM25_DP': [0.1103, 0.1103],
    'TFIDF_DAAT': [0.0966, 0.0966],
    'TFIDF_DP': [0.0966, 0.0966]
}

# Valori NDCG@10
NDCG_at_10 = {
    'BM25_DAAT': [0.4194, 0.4194],
    'BM25_DP': [0.4194, 0.4194],
    'TFIDF_DAAT': [0.4207, 0.4207],
    'TFIDF_DP': [0.4207, 0.4207]
}

# Valori MAP@10
MAP_at_10 = {
    'BM25_DAAT': [0.094, 0.0940],
    'BM25_DP': [0.094, 0.0940],
    'TFIDF_DAAT': [0.0847, 0.0847],
    'TFIDF_DP': [0.0847, 0.0847]
}

# Plot
fig, axs = plt.subplots(2, 2, figsize=(12, 10))

for ax, values, title in zip(axs.flat, [P_at_10, R_at_10, NDCG_at_10, MAP_at_10], ['P@10', 'R@10', 'NDCG@10', 'MAP@10']):
    x = np.arange(len(configurations))
    width = 0.15  # Reduced width of the bars

    for i, method in enumerate(methods):
        ax.bar(x + i*width, [values[method][0], values[method][1]], width=width, label=method)

    ax.set_xticks(x + width * (len(methods) - 1) / 2)
    ax.set_xticklabels(configurations)
    ax.set_title(title)

# Posizionamento della legenda
fig.legend(methods, loc='upper right', bbox_to_anchor=(1, 1.001))  # Adjusted legend position

plt.tight_layout()
plt.show()







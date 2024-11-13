import matplotlib.pyplot as plt
import numpy as np

# Definizione delle configurazioni
configurations = [
    "No Compression,\nNo Preprocessing",
    "With Compression,\nNo Preprocessing",
    "No Compression,\nWith Preprocessing",
    "With Compression,\nWith Preprocessing"
]

# Dati per i tempi di esecuzione in minuti
spimi_times = [49*60 + 46, 50*60 + 23, 17*60 + 26, 17*60 + 12]  # in secondi
merge_times = [47*60 + 39, 50*60 + 36, 21*60 + 40, 21*60 + 22]  # in secondi

# Conversione in minuti
spimi_times = np.array(spimi_times) / 60
merge_times = np.array(merge_times) / 60

# Dimensioni dei file in MB
file_sizes = {
    'docIds': [2748, 1614, 1304, 762.7],  # in MB
    'freqs': [675.5, 62.6, 320.3, 28.5],
    'lexicon': [66.2, 66.2, 55.3, 55.3],
    'docTerms': [35.4, 35.4, 35.4, 35.4],
    'skipping': [92.5, 92.5, 67 , 67]
}

# Creazione della figura con due sottotrame
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 8))

# Grafico a sinistra: Tempi di esecuzione
bar_width = 0.35
index = np.arange(len(configurations))

# Barre per SPIMI e Merge Time
spimi_bars = ax1.bar(index - bar_width/2, spimi_times, bar_width, label='SPIMI Execution', color='blue')
merge_bars = ax1.bar(index + bar_width/2, merge_times, bar_width, label='Merge Time', color='green')

# Etichette e titolo per il grafico dei tempi
ax1.set_xlabel('Configuration')
ax1.set_ylabel('Time (minutes)')
ax1.set_title('SPIMI and Merger Execution Times')
ax1.set_xticks(index)
ax1.set_xticklabels(configurations)
ax1.legend()

# Grafico a destra: Dimensioni dei file
bar_width = 0.15  # Barre più strette per ciascun file
for i, (label, data) in enumerate(file_sizes.items()):
    ax2.bar(index + i * bar_width, data, bar_width, label=label)

# Etichette e titolo per il grafico delle dimensioni
ax2.set_xlabel('Configuration')
ax2.set_ylabel('Size (MB)')
ax2.set_title('File Sizes Across Configurations')
ax2.set_xticks(index + 2 * bar_width)  # Centro delle barre
ax2.set_xticklabels(configurations)
ax2.legend()

# Layout per migliorare la leggibilità
plt.tight_layout()
plt.show()

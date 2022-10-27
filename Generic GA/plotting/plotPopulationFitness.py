import matplotlib.pyplot as plt
import csv

# Installation instructions for matplotlib
# https://matplotlib.org/stable/users/installing.html

# Longer tutorial here
# https://docs.python.org/3/library/csv.html

max_color='blue'
min_color='orange'
avg_color='red'

seed = 27

data = f'./data/Seed{seed}_PopulationFitness.csv'
generations=[]
max=[]
avg=[]
min=[]

with open(data, newline='') as csvfile:
    # Using the csv reader automatically places all values 
    # in columns within a row in a dictionary with a 
    # key based on the header (top line of the file)
    reader = csv.DictReader(csvfile)
    for row in reader:
        generations.append( int(row["Generation"]) )
        max.append( float(row["Max Fitness"] ) )
        min.append( float(row["Min Fitness"] ) )
        avg.append( float(row["Avg Fitness"] ) )

        

plt.plot(generations,max,label=f"Max Fitness", color=max_color)
plt.plot(generations,avg,label=f"Avg Fitness", color=avg_color)
plt.plot(generations,min,label=f"Min Fitness", color=min_color)

plt.yscale("linear")

plt.legend()
plt.xlabel('generations')
plt.ylabel('fitness')


plt.savefig(f'./data/Seed{seed}_PopulationFitness.png')
plt.show()

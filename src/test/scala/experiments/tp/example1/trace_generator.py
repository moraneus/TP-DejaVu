import random
from random import choices

population = [
    1, 2, 3, 4,
    5, 6, 7, 8,
    9, 10, 11, 12,
    13, 14, 15, 16,
    17, 18, 19, 20
]

weights = [
    0.01, 0.01, 0.01, 0.01,
    0.02, 0.02, 0.02, 0.05,
    0.05, 0.05, 0.05, 0.05,
    0.05, 0.05, 0.05, 0.1,
    0.1, 0.1, 0.1, 0.1
]

user_input = input("Select the size of the trace: ")

with open("log.csv", "w") as f:
    for i in range(1, int(user_input) + 1):
        rand = choices(population, weights)[0]
        if rand in (3, 7, 11, 17):
            f.write(f"q,{rand},{random. randint(1, 10000)}\n")
        else:
            f.write(f"p,{rand}\n")
            



        
        
    


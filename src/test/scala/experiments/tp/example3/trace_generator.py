import random

user_input = input("Select the size of the trace: ")

with open("log.csv", "w") as f:
    for i in range(1, int(user_input) + 1):
        if i % 100 == 0:
            f.write(f"p,{random.randint(10000000, 20000000)}\n")
        else:
            f.write(f"q,{random.randint(0, 10000000)}\n")


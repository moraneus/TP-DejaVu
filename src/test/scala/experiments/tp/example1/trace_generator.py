import random

user_input = input("Select the size of the trace: ")

with open("log.csv", "w") as f:
    for i in range(1, int(user_input) + 1):
        if i in (3, 7, 11, 17, 20, 22, 30, 35, 41):
            f.write(f"q,{random.randint(-1000000000, 1000000000)},{random. randint(1, 1000000000)}\n")
        else:
            f.write(f"p,{random.randint(-1000000000, 1000000000)}\n")
            



        
        
    


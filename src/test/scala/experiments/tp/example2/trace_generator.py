import random

user_input = int(input("Select the size of the trace: "))

res = []

for i in range(1, user_input):
    rand_x = random. randint(1, 1000000)
    rand_y = random.randint(1, 1000000)
    if rand_x % 15 == 0 or rand_x % 17 == 0:
        res.append(f"p,{rand_x}\n")
        res.append(f"q,{rand_y}\n")
        res.append(f"r,{rand_x},{rand_y}\n")
    else:
        res.append(f"q,{rand_y}\n")
        res.append(f"p,{rand_x}\n")
        res.append(f"r,{random.randint(1, 1000000)},{random.randint(1, 1000000)}\n")

with open("log.csv", "w") as f:
    random.shuffle(res)
    for l in res[:user_input]:
        f.write(l)
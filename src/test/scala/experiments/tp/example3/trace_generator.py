user_input = input("Select the size of the trace: ")

with open("log.csv", "w") as f:
    for i in range(1, int(user_input) + 1):
        if i % 1000 != 0:
            f.write(f"p,{i}\n")
        else:
            f.write(f"p,{0}\n")


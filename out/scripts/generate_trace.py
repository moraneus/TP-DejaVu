import random


def spec_one_trace_generator(size: int, ordered: bool = False) -> list:
    if ordered:
        trace = [f"a,{i}\nb,{i}\nc,{i}\nd,{i}\ne,{i}\ng,{i}\n" for i in range(1, size + 1)]
    else:
        trace = []
        for index in range(1, size + 1):
            trace.append(f"a,{random.randint(1, 1000)}\n")
            trace.append(f"b,{random.randint(1, 1000)}\n")
            trace.append(f"c,{random.randint(1, 1000)}\n")
            trace.append(f"d,{random.randint(1, 1000)}\n")
            trace.append(f"e,{random.randint(1, 1000)}\n")
            trace.append(f"g,{random.randint(1, 1000)}\n")
        random.shuffle(trace)

    return trace


def spec_two_trace_generator(size: int, ordered: bool = False) -> list:
    if ordered:
        trace = [f"p,{i}\ng,{i}\n" for i in range(1, size + 1)]
    else:
        trace = []
        for index in range(1, size + 1):
            trace.append(f"p,{random.randint(1, 1000)}\n")
            trace.append(f"g,{random.randint(1, 1000)}\n")
        random.shuffle(trace)

    return trace


def spec_three_trace_generator(size: int, ordered: bool = False) -> list:
    if ordered:
        trace = [f"p,{i}\nq,{i}\ng,{i}\n" for i in range(1, size + 1)]
    else:
        trace = []
        for index in range(1, size + 1):
            trace.append(f"p,{random.randint(1, 1000)}\n")
            trace.append(f"q,{random.randint(1, 1000)}\n")
            trace.append(f"g,{random.randint(1, 1000)}\n")
        random.shuffle(trace)

    return trace


def spec_four_trace_generator(size: int, ordered: bool = False) -> list:
    if ordered:
        trace = [f"open,f{i}\nwrite,f{i},{i}\nclose,f{i}\n" for i in range(1, size + 1)]
    else:
        trace = []
        for index in range(1, size + 1):
            filename = random.randint(1, 1000)
            data = random.randint(1, 1000)
            trace.append(f"open,f{filename}\n")
            trace.append(f"write,f{filename},{data}\n")
            trace.append(f"close,f{filename}\n")
        random.shuffle(trace)

    return trace


def spec_five_trace_generator(size: int, ordered: bool = False) -> list:
    trace = []
    if ordered:
        for i in range(1, size + 1):
            trace.append(f"q,{i}\n")
        for i in range(1, size - 4):
            trace.append(f"r,{i}\n")
    else:
        for index in range(1, size + 1):
            trace.append(f"q,{random.randint(1, 1000)}\n")
            trace.append(f"r,{random.randint(1, 1000)}\n")
        random.shuffle(trace)

    return trace


def spec_six_trace_generator(size: int, ordered: bool = False) -> list:
    if ordered:
        trace = [f"q,{i}\n" for i in range(1, size + 1)]
    else:
        trace = []
        for index in range(1, size + 1):
            trace.append(f"q,{random.randint(1, 1000)}\n")
        random.shuffle(trace)

    return trace


with open("trace_spec_5_random_3.csv", "w") as f:
    result = spec_five_trace_generator(250, False)
    f.write("".join(result))

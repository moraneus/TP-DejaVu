
## Example 1

In this example we have events that include two data items: a car vendor 
and speed; a true verdict is returned each time that a new vendor has broken the speed
record for first time. Notice that the verdict for the inspected traces can change from
true to false and back to true any number of times.


#### TP-DejaVu
```
initiate
    MaxSpeed: int := 0
on recorded(vendor: str , speed: int )
    NewRecord: bool := @MaxSpeed <speed
    MaxSpeed: int := ite (NewRecord == true, speed, @MaxSpeed)
output fast(vendor, NewRecord)
```

#### DejaVu

```
prop p1: exists x . ( fast(x, "true") & ! @ P fast(x, "true"))
```

#### Experiments

<table style="font-size: smaller; width: 100%; text-align: center;">
    <thead>
        <tr>
            <th># Objects</th>
            <th>Trace 10K</th>
            <th>Trace 100K</th>
            <th>Trace 500K</th>
            <th>Trace 1M</th>
            <th>Trace 5M</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>10 cars</td>
            <td>0.53s<br>105.68MB</td>
            <td>0.92s<br>157.34MB</td>
            <td>1.55s<br>229.31MB</td>
            <td>2.35s<br>292.72MB</td>
            <td>5.74s<br>303.02MB</td>
        </tr>
</table>

#### How to Execute the Example

1. Ensure you have the following files in your local environment:
- `dejavu`
- `tpdejavu.jar`

2. Clone the [experiment directory](https://github.com/moraneus/TP-DejaVu/blob/master/out/examples/tp/spec1) and place the above files inside it.

3. Specify the length of the trace by modyfing the variable `SIMULATION_STEPS` (line 19) in `generate.go`:

4. Generate a trace:

```
go run generate.go
```

It will prompt you to enter the number of cars.

5. After the trace file is produced, run the following command:

```bash
./dejavu --specfile=spec1.pqtl --logfile=log.csv --bits=20 --prefile=spec1.pqtl
```

where `log.csv` is the name of the produced trace log file. 


## Example 2

The following example records from time to time the temperature in one
of a collection of running cars. It calculates the temperature increase between the two
successive events. If the increase in temperature is more than t (say Celsius) degrees,
then it indicates an overheated car. The monitor reports if some car is overheated more than twice.
(The first-order property also checks the alternating sequencing of the start and end of the measurements.)


#### TP-DejaVu
```
on StartMeasure(Car: str, temp: float)
output startMeasure(Car)

on EndMeasure(Car: str, temp: float)
    Warming: bool := (temp - @temp > 5)
output warmAlert(Car, Warming)
```

#### DejaVu

```
prop p2 : exists x . (warmAlert(x, "true") & @(startMeasure(x) & P (warmAlert(x, "true") & @P (warmAlert(x, "true")))))
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
            <td>0.67s<br>106.88MB</td>
            <td>0.92s<br>150.04MB</td>
            <td>1.52s<br>263.37MB</td>
            <td>2.41s<br>288.28MB</td>
            <td>11.12s<br>291.95MB</td>
        </tr>
</table>

#### How to Execute the Example

1. Ensure you have the following files in your local environment:
- `dejavu`
- `tpdejavu.jar`

2. Clone the [experiment directory](https://github.com/moraneus/TP-DejaVu/blob/master/out/examples/tp/spec2) and place the above files inside it.

3. Specify the length of the trace by modyfing the variable `SIMULATION_STEPS` (line 14) in `generate.go`. Please note that each simulation step generates 2 events (`StartMeasure` & `EndMeasure`) for each car. So if you want to generate a log file of size `n`, you have to define `SIMULATION_STEPS` as `n/2`.

4. Generate a trace (Make sure golang installed in your PC - It tested on go version go1.21.0):

```
go run generate.go
```

It will prompt you to enter the number of cars.

5. After the trace file is produced, run the following command:

```bash
./dejavu --specfile=spec2.pqtl --logfile=log.csv --bits=20 --prefile=spec2.pqtl
```

where `log.csv` is the name of the produced trace log file. 

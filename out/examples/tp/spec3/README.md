
## Example 3

This property deals with commands sent to air conditions (ac). In order
to send a command, we need to turn the air condition on, and then we can send it
commands until we turn it off. However, if the command is out of temperature bounds,
namely below 17C or above 26C, then it is ignored as a faulty command, so even if the
air conditioner is not turned on, there is no problem.


#### TP-DejaVu
```
on set(ac: str, temp: float)
    WithinBound: bool := (temp >= 17 && temp <= 26)
output set(ac, temp, WithinBound)
```

#### DejaVu

```
prop p3: forall ac . ( ( exists temp .  set(ac, temp, "true") ) -> ( !turn_off(ac) S turn_on(ac) )  )
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
            <td>10 ACs</td>
            <td>0.58s<br>108.30MB</td>
            <td>0.81s<br>165.85MB</td>
            <td>1.68s<br>240.72MB</td>
            <td>2.28s<br>297.82MB</td>
            <td>7.22s<br>300.19MB</td>
        </tr>
</table>

#### How to Execute the Example

1. Ensure you have the following files in your local environment:
- `dejavu`
- `tpdejavu.jar`

2. Clone the [experiment directory](https://github.com/moraneus/TP-DejaVu/blob/master/out/examples/tp/spec3) and place the above files inside it.

3. Specify the number simulation steps by modifying the variable `SIMULATION_STEPS` (line 13) in `generate.go`. Please note that each simulation step can generate at most 3 events (`turn_on`, `set` and `turn_off`) for each air conditioner (AC). These three events for a specific AC can be produced with some probability. Additionally, a `set` or `turn_off` event can be produced only if the corresponding AC has been turned on previously. 

4. Generate a trace (Make sure golang installed in your PC - It tested on go version go1.21.0):

```
go run generate.go
```

It will prompt you to enter the number of air conditioning machines.

5. After the trace file is produced, run the following command:

```bash
./dejavu --specfile=spec3.pqtl --logfile=log.csv --bits=20 --prefile=spec3.pqtl
```

where `log.csv` is the name of the produced trace log file. 

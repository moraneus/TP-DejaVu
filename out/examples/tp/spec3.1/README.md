
## Example 3.1 - Prediction of obstacle behaviour in an Automated Valet Parking system

An automated valet parking (AVP) system is an L4 (level four) autonomous driving
system. A user owning a vehicle equipped with the AVP functionality can stop the car
in front of the parking lot entry area. Whenever the user triggers the AVP function, the
vehicle will communicate with the infrastructure and park the car at designated regions.
The system is expected to operate under mixed traffic, i.e., the parking lot will have
other road users including walking pedestrians and other vehicles.
The AVP system implements object detection capabilities for sensing the surround-
ing objects and localisation functions for inferring the system’s location on the map. It
also features mission and path planning functions, as well as trajectory tracking and a
prediction module that is based on the available information that predicts positions of
traffic participants (i.e., obstacle list) in the future. The position is given in x, y coor-
dinates. The prediction error is computed as the distance between an obstacle’s actual
position at cycle t and the predicted position for it at cycle t − 1. The prediction error
reported for an obstacle at any computation cycle is bounded within a certain value Epo.
Also, a maximum accumulated error Emax exists for the system.


#### TP-DejaVu
```
Initiate
    LErr: double := 0
    SysErr: double := 0
    NewPred: bool := false
    Emax: int := 200
    Epo: int := 20

on entry(ru: str)
output entry(ru)

on mk_prediction(ru: str, x: int, y: int)
    LErr: double := 0
    NewPred: bool := true
output predicted(ru)

on obstacle(ru: str, x: int, y: int)
    LErr: double := ite(NewPred == true, (((x - @x)^^2) + ((y - @y)^^2))^^0.5, 0)
    SysErr: double := @SysErr + LErr
    error: bool := (LErr > Epo) || (SysErr > Emax)
    NewPred: bool := false
output valid(ru, error, LErr)

on exit(ru: str, l_err: double)
    SysErr: double := @SysErr - l_err
output exit(ru)
```

#### DejaVu

```
prop p3_1: forall ru .  ( exit(ru) | ( ( @ predicted(ru) -> exists L . valid(ru, "false", L) ) S entry(ru) ) )
```

This specification states that a prediction can be made for the future position of any road
user (ru) that has entered the parking lot area (event entry) and has not yet exited (event
exit). Upon having a new prediction (NewPred), the prediction error (LErr) measured
when ru’s actual position is known (event obstacle) does not exceed Epo. Moreover, the
accumulated error (SysErr) for all road users that are still moving in the parking lot area
does not exceed Emax. When a ru leaves the parking space, the last reported prediction
error (p err) ceases to be considered in the accumulated error (SysErr).


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
            <td>10 road users</td>
            <td>0.72s<br>114.17MB</td>
            <td>1.54s<br>169.08MB</td>
            <td>1.95s<br>292.89MB</td>
            <td>3.23s<br>296.25MB</td>
            <td>8.81s<br>315.48MB</td>
        </tr>
</table>

#### How to Execute the Example

1. Ensure you have the following files in your local environment:
- `dejavu`
- `tpdejavu.jar`

2. Clone the [experiment directory](https://github.com/moraneus/TP-DejaVu/blob/master/out/examples/tp/spec3.1) and place the above files inside it.

3. Specify the number simulation steps by modifying the variable `SIMULATION_STEPS` (line 13) in `generate.go`. Please note that each simulation step can generate up to 5 events (`entry`, `mkprediction`, `obstacle` and `exit`) for each road user (RU).  Each of these events can be produced with certain probabilities for each road user and follows these rules:

* `entry(ru)` : May be randomly generated if the given road user is not already within the parking lot area.
* `mkprediction(ru,x,y)` : May be randomly generated if the given road user has entered the parking lot, has not exited yet and no prediction has been made for this road user so far. If a `mkprediction(ru,x,y)` occurs, an `obstacle(ru,x,y)` event follows for that road user.
* `obstacle(ru,x,y)` : May be randomly generated for road users that are within the parking lot area.
* `exit(ru)`: May be randomly generated for road users that are within the parking lot area.


4. Generate a trace:

```
go run generate.go
```

It will prompt you to enter the number of road users.

5. After the trace file is produced, run the following command:

```bash
./dejavu --specfile=spec3.1.pqtl --logfile=log.csv --bits=20 --prefile=spec3.1.pqtl
```

where `log.csv` is the name of the produced trace log file. 

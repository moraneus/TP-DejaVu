
## Example 4.2 - Monitoring the perception function of an AVP system

The perception monitor takes as input from the Perception subsystem the computed free
space area (event new fspace), obstacles (ru) and their localisation (event location). The
dimensions of the free space are given as a rectangle defined by its diagonally opposite
corners. The dimensions of localised rus are given through reporting their detected 2D
bounding boxes. The perception monitor detects and alerts about inconsistencies in the
perception process. Specifically, the output of free space detection and localisation of
rus should never overlap, to ensure consistency of the overall perception function.


#### TP-DejaVu
```
on new_fspace(fs_x1: int, fs_y1: int , fs_x2: int , fs_y2: int )
output new_fspace(fs_x1, fs_y1, fs_x2, fs_y2)

on location(ru: str , bb_xa: int , bb_ya: int , bb_xb: int , bb_yb: int )
    overlap:bool := (bb_xb >= fs_x1) && (bb_xa <= fs_x2) && (bb_yb <= fs_y1) && (bb_ya >= fs_y2)
output error(ru,overlap)
```

#### DejaVu

```
prop p4:  exists x1 . exists y1 . exists x2 . exists y2 .   exit_fspace(x1,y1,x2,y2) |
  (forall ru . ! error(ru, "true") & (!exit_fspace(x1,y1,x2,y2)  S new_fspace(x1, y1, x2, y2)))
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
            <td>10 road users</td>
            <td>0.64s<br>134.00MB</td>
            <td>1.30s<br>189.15MB</td>
            <td>3.37s<br>340.44MB</td>
            <td>6.10s<br>349.80MB</td>
            <td>22.72s<br>1.11GB</td>
        </tr>
</table>

#### How to Execute the Example

1. Ensure you have the following files in your local environment:
- `dejavu`
- `tpdejavu.jar`

2. Clone the [experiment directory](https://github.com/moraneus/TP-DejaVu/blob/master/out/examples/tp/spec4.2) and place the above files inside it.

3. Specify the number simulation steps by modifying the variable `SIMULATION_STEPS` (line 14) in `generate.go`. Please note that each simulation step can generate up to `n + 2` events ( `new_fspace`, `exit_fspace` and `location`) where `n` the number of road users. In each simulation step, a `new_fspace(x1,y1,x2,y2)` event may be randomly generated with the top-left (`x1`,`y1`) and bottom-right (`x2`,`y2`) points of that free space. If this event is generated, an `exit_fspace(x1,y1,x2,y2)` event is produced first, indicating the coordinates of the previous free space. In each simulation step, whether or not a new free space is generated, `n` `location(ru,x1,y1,x2,y2)` events are generated, each with the ID of the road user and their corresponding coordinates.


4. Generate a trace (Make sure golang installed in your PC - It tested on go version go1.21.0):

```
go run generate.go
```

It will prompt you to enter the number of road users.

5. After the trace file is produced, run the following command:

```bash
./dejavu --specfile=spec4.2.pqtl --logfile=log.csv --bits=20 --prefile=spec4.2.pqtl
```

where `log.csv` is the name of the produced trace log file. 

package main

import (
	"fmt"
	"math"
	"math/rand"
	"os"
	"strconv"
	"sync"
)

var (
	NCARS            int
	SIMULATION_STEPS = 50
	Cars             []*Car
	Distances 		 []*Distance
	Differences 	 []int
	filename         = "log.csv"
	file             *os.File
	fileMutex        sync.Mutex
	
)



type Car struct {
	Name string
	X    int
	Y    int
}

type Distance struct {
	CarA *Car
	CarB *Car
	Dist int 
	Monitoring bool
}

func dices() bool {
	randInt := rand.Intn(51)
	return randInt%5 == 0
}

func openFile() error {
	var err error
	file, err = os.OpenFile(filename, os.O_WRONLY|os.O_CREATE|os.O_APPEND, 0644)
	return err
}

func closeFile() {
	file.Close()
}

func appendLine(line string) {

	fileMutex.Lock()
	defer fileMutex.Unlock()

	if _, err := file.WriteString(line + "\n"); err != nil {
		fmt.Println("Error: ", err)
	}

}

func compute_distance(carA *Car, carB *Car) int {

	return int (math.Sqrt(math.Pow(float64(carA.X-carB.X), 2) + math.Pow(float64(carB.Y-carA.Y), 2)))

}

func location(carA *Car, carB *Car) {
	//dist := math.Sqrt(math.Pow(float64(carA.X-carB.X), 2) + math.Pow(float64(carB.Y-carA.Y), 2))
	appendLine("location," + carA.Name + "," + carB.Name + "," + strconv.Itoa(carA.X)+ "," + strconv.Itoa(carA.Y)+  "," + strconv.Itoa(carB.X)+ "," + strconv.Itoa(carB.Y))
}

func start_measure(carA *Car, carB *Car){
	appendLine("startMeasure," + carA.Name + "," + carB.Name + "," + strconv.Itoa(carA.X)+ "," + strconv.Itoa(carA.Y)+  "," + strconv.Itoa(carB.X)+ "," + strconv.Itoa(carB.Y))
}

func move(car *Car) {
	car.X += rand.Intn(9) - 4
	car.Y += rand.Intn(9) - 4
}

func get_minimum_distance() (int, int) {

	var minimumDistance int  = 10000000
	var index int = -1

	for ind, dist := range Distances {
		if dist.Dist < minimumDistance {
			minimumDistance = dist.Dist
			index = ind
		}
	}

	return index, minimumDistance
}

func start_monitoring(d *Distance) {

	d.Monitoring = true;
	appendLine("startMonitoring" + "," + d.CarA.Name + "," + d.CarB.Name)

}

func stop_monitoring(d *Distance) {

	d.Monitoring = false;
	appendLine("stopMonitoring" + "," + d.CarA.Name + "," + d.CarB.Name)

}

func monitor_check() {

	for i:=0; i < len(Differences); i++ {
		if Differences[i] < 0 && Distances[i].Monitoring == false {
			start_monitoring(Distances[i])
		} else if Differences[i] >=0 && Distances[i].Monitoring == true {
			stop_monitoring(Distances[i])
		}
	}

}

func simulate() {

	
	var index int
	
	for step := 0; step < SIMULATION_STEPS; step++ {

		/*for _, car := range Cars {
			move(car)
		}*/
		
		index, _ = get_minimum_distance();

		d := Distances[index]
		
		start_measure(d.CarA, d.CarB)

		for _, car := range Cars {
			move(car)
		}
		update_distances_between_cars()

		for newInd, _ := get_minimum_distance(); newInd == index; {

			
			location(d.CarA, d.CarB)
			for _, car := range Cars {
				move(car)
			}
			update_distances_between_cars()
		}

		 
	
	}
}

func update_distances_between_cars() {


	k := 0

	for i := 0; i < len(Cars)-1; i++ {

		for j := i + 1; j < len(Cars); j++ {

			d := compute_distance(Cars[i], Cars[j])

			Differences[k] =  d - Distances[k].Dist
			Distances[k].Dist = d
			k+=1
		

		}
	}

	monitor_check()
	
}

func initialize() {

	for i := 0; i < NCARS; i++ {
		Cars = append(Cars, &Car{
			Name: "car" + strconv.Itoa(i),
			X:    rand.Intn(40) - 20,
			Y:    rand.Intn(40) - 20,
		})
	}

	for i := 0; i < len(Cars)-1; i++ {
		for j := i + 1; j < len(Cars); j++ {

			Distances = append(Distances, &Distance { 
				CarA: Cars[i] , 
				CarB: Cars[j], 
				Dist: compute_distance(Cars[i], Cars[j]),
				Monitoring: false,
			})

			Differences = append(Differences, 0)
				
		}
	}

}

func main() {

	if err := openFile(); err != nil {
		fmt.Println("Error: ", err)
		return
	}

	fmt.Print("Number of cars: ")
	fmt.Scan(&NCARS)

	initialize()
	simulate()

}

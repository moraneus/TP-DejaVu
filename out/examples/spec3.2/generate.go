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
	filename         = "log.csv"
	file             *os.File
	fileMutex        sync.Mutex
)

type Car struct {
	Name string
	X    int
	Y    int
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

func distance(carA *Car, carB *Car) {
	dist := math.Sqrt(math.Pow(float64(carA.X-carB.X), 2) + math.Pow(float64(carB.Y-carA.Y), 2))
	appendLine("distance," + carA.Name + "," + carB.Name + "," + strconv.Itoa(int(dist)))
}

func move(car *Car) {
	car.X += rand.Intn(9) - 4
	car.Y += rand.Intn(9) - 4
}

func simulate() {

	for step := 0; step < SIMULATION_STEPS; step++ {
		for i := 0; i < len(Cars)-1; i++ {
			for j := i + 1; j < len(Cars); j++ {
				distance(Cars[i], Cars[j])
			}
		}

		for _, car := range Cars {
			move(car)
		}
	}
}

func initialize() {

	for i := 0; i < NCARS; i++ {
		Cars = append(Cars, &Car{
			Name: "car" + strconv.Itoa(i),
			X:    rand.Intn(40) - 20,
			Y:    rand.Intn(40) - 20,
		})
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

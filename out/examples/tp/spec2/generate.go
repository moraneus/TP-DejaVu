package main

import (
	"fmt"
	"math/rand"
	"os"
	"strconv"
	"sync"
	"time"
)

var (
	NCARS            int
	SIMULATION_STEPS = 250000
	Cars             []*Car
	filename         = "log.csv"
	file             *os.File
	fileMutex        sync.Mutex
)

type Car struct {
	Id          string
	Temperature int
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

func StartMeasure(car *Car) {
	car.Temperature += rand.Intn(10) - 3
	appendLine("StartMeasure," + car.Id + "," + strconv.Itoa(car.Temperature))
}

func EndMeasure(car *Car) {
	car.Temperature += rand.Intn(10) - 3
	appendLine("EndMeasure," + car.Id + "," + strconv.Itoa(car.Temperature))

	if car.Temperature > 120 {
		car.Temperature = 90
	}

}

func simulate() {

	for i := 0; i < SIMULATION_STEPS; i++ {
		for _, car := range Cars {
			StartMeasure(car)
			EndMeasure(car)
		}
	}
}

func initialize() {

	for i := 0; i < NCARS; i++ {
		Cars = append(Cars, &Car{
			Id:          "car" + strconv.Itoa(i),
			Temperature: 90,
		})
	}
}

func main() {

	rand.Seed(time.Now().UnixNano())

	if err := openFile(); err != nil {
		fmt.Println("Error: ", err)
		return
	}

	fmt.Print("Number of cars: ")
	fmt.Scan(&NCARS)

	initialize()
	simulate()

}

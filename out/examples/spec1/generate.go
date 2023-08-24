package main

import (
	"fmt"
	"math"
	"math/rand"
	"os"
	"strconv"
	"sync"
)

var Manifacturers = []string{"bmw", "audi", "alfaromeo",
	"mercedes", "opel", "toyota", "nissan", "citroen", "seat",
	"volkswagen", "scoda", "honda", "renault", "ford", "hyundai", "tesla", "fiat",
	"volvo", "chevrolet", "peugeot"}

var (
	NCARS            int
	SIMULATION_STEPS = 50
	Cars             []*Car
	filename         = "log.csv"
	file             *os.File
	fileMutex        sync.Mutex
)

type Car struct {
	Maker string
	Speed int
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

func recorded(car *Car) {
	car.Speed += rand.Intn(20) - 10
	car.Speed = int(math.Max(float64(car.Speed), 0))
	appendLine("recorded," + car.Maker + "," + strconv.Itoa(car.Speed))
}

func simulate() {

	for i := 0; i < SIMULATION_STEPS; i++ {
		for _, car := range Cars {
			recorded(car)
		}
	}
}

func initialize() {

	for i := 0; i < NCARS; i++ {
		Cars = append(Cars, &Car{
			Maker: Manifacturers[rand.Intn(len(Manifacturers))],
			Speed: 0,
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

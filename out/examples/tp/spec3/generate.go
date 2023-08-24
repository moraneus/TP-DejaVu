package main

import (
	"fmt"
	"math/rand"
	"os"
	"strconv"
	"sync"
)

var (
	NACS             int
	SIMULATION_STEPS = 50
	Airconditions    []*AC
	filename         = "log.csv"
	file             *os.File
	fileMutex        sync.Mutex
)

type AC struct {
	Name   string
	Status bool
	Temp   int
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

func set(ac *AC) {

	if ac.Status == true && dices() == true {
		ac.Temp = rand.Intn(15) + 14
		appendLine("set," + ac.Name + "," + strconv.Itoa(ac.Temp))
	}

}

func turn_on(ac *AC) {

	if dices() == true && ac.Status == false {
		ac.Status = true
		appendLine("turn_on," + ac.Name)
	}
}

func turn_off(ac *AC) {

	if dices() == true && ac.Status == true {
		ac.Status = false
		appendLine("turn_off," + ac.Name)
	}
}

func simulate() {

	for i := 0; i < SIMULATION_STEPS; i++ {
		for _, ac := range Airconditions {
			turn_on(ac)
			set(ac)
			turn_off(ac)
		}
	}
}

func initialize() {

	for i := 0; i < NACS; i++ {
		Airconditions = append(Airconditions, &AC{
			Name:   "ac" + strconv.Itoa(i),
			Status: false,
			Temp:   0,
		})
	}
}

func main() {

	if err := openFile(); err != nil {
		fmt.Println("Error: ", err)
		return
	}

	fmt.Print("Number of ACs: ")
	fmt.Scan(&NACS)

	initialize()
	simulate()

}

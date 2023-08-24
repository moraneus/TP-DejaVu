package main

import (
	"fmt"
	"math"
	"math/rand"
	"os"
	"strconv"
	"sync"
	"time"
)

var (
	RAW_USERS        int
	SIMULATION_STEPS = 20000
	Obstacles        map[string]*Info
	Syserr           int
	filename         = "log.csv"
	file             *os.File
	fileMutex        sync.Mutex
)

type Info struct {
	X         int
	Y         int
	Z         int
	Lerr      int
	Entry     bool
	Exit      bool
	Predicted bool
}

func openFile() error {
	var err error
	file, err = os.OpenFile(filename, os.O_WRONLY|os.O_CREATE|os.O_APPEND, 0644)
	return err
}

func appendLine(line string) {
	fileMutex.Lock()
	defer fileMutex.Unlock()

	_, err := file.WriteString(line + "\n")

	if err != nil {
		fmt.Println("Error:", err)
	}
}

func closeFile() {
	file.Close()
}

func dices() bool {
	randInt := rand.Intn(51)
	return randInt%5 == 0
}

func predict(ru string) (int, int, int) {
	randX, randY, randZ := rand.Intn(41)-20, rand.Intn(41)-20, rand.Intn(41)-20

	ruObj := Obstacles[ru]

	return ruObj.X + randX, ruObj.Y + randY, ruObj.Z + randZ

}

func obstacle(ru string, x int, y int, z int) {
	ruObj := Obstacles[ru]
	ruObj.Lerr = int(math.Abs(float64(ruObj.X-x)) + math.Abs(float64(ruObj.Y-y)) + math.Abs(float64(ruObj.Z-z)))
	Syserr += ruObj.Lerr
	appendLine("obstacle," + ru + "," + strconv.Itoa(ruObj.X) + "," + strconv.Itoa(ruObj.Y) + "," + strconv.Itoa(ruObj.Z))
}

func obstacle_report(ru string) {

	ruObj := Obstacles[ru]

	if ruObj.Entry == true && ruObj.Exit == false && dices() == true {
		appendLine("obstacle," + ru + "," + strconv.Itoa(ruObj.X) + "," + strconv.Itoa(ruObj.Y) + "," + strconv.Itoa(ruObj.Z))
	}
}

func mk_prediction(ru string) {
	ruObj := Obstacles[ru]

	if ruObj.Entry == true && ruObj.Exit == false && ruObj.Predicted == false && dices() == true {
		x, y, z := predict(ru)
		appendLine("mk_prediction," + ru + "," + strconv.Itoa(x) + "," + strconv.Itoa(y) + "," + strconv.Itoa(z))
		obstacle(ru, x, y, z)
		ruObj.Predicted = true
	}

}

func entry(ru string) {
	ruObj := Obstacles[ru]

	if ruObj.Entry != true && dices() == true {
		appendLine("entry," + ru)
		ruObj.Entry = true
		ruObj.Exit = false
	}
}

func exit(ru string) {

	ruObj := Obstacles[ru]
	if ruObj.Exit != true && ruObj.Entry == true && /*ruObj.Predicted == true &&*/ dices() == true {
		appendLine("exit," + ru + "," + strconv.Itoa(ruObj.Lerr))
		ruObj.Exit = true
		ruObj.Entry = false
		ruObj.Predicted = false
		Syserr -= ruObj.Lerr
		ruObj.Lerr = 0

	}

}

func simulate() {

	for i := 0; i < SIMULATION_STEPS; i++ {

		for key, _ := range Obstacles {
			entry(key)
			mk_prediction(key)
			obstacle_report(key)
			exit(key)
		}

	}

}

func initialize() {

	for i := 0; i < RAW_USERS; i++ {
		Obstacles["ru"+strconv.Itoa(i)] = &Info{X: rand.Intn(30), Y: rand.Intn(30), Z: rand.Intn(30), Entry: false, Exit: false, Lerr: 0}
	}
}

func main() {

	rand.Seed(time.Now().UnixNano())

	if err := openFile(); err != nil {
		fmt.Println("Error: ", err)
		return
	}

	defer closeFile()

	Obstacles = make(map[string]*Info)

	fmt.Print("Number of raw users: ")
	fmt.Scan(&RAW_USERS)
	fmt.Println(RAW_USERS)

	initialize()
	simulate()

}

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
	ROAD_USERS        int
	SIMULATION_STEPS = 50000
	RoadUsers        map[string]*BoundingBox
	FreeSpace		 *BoundingBox
	Syserr           float64
	filename         = "log.csv"
	file             *os.File
	fileMutex        sync.Mutex
)

type Point struct {
	X 	int
	Y	int
}

type Movable interface {
	Move()
}

type BoundingBox struct {
	TopLeft		*Point
	BottomRight	*Point
}

func (bbox *BoundingBox) Move() {

	randX, randY := rand.Intn(21)-10, rand.Intn(21)-10

	//ruObj := RoadUsers[ru]
	bbox.TopLeft.X += randX
	bbox.BottomRight.X += randX
	bbox.TopLeft.Y += randY
	bbox.BottomRight.Y += randY

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


func free_space() {

	if dices() == true {
		newFreeSpace()
	}
}


func location(ru string) {

	ruObj := RoadUsers[ru]

	appendLine("location," + ru + "," + strconv.Itoa(ruObj.TopLeft.X) + "," + strconv.Itoa(ruObj.TopLeft.Y) + "," + strconv.Itoa(ruObj.BottomRight.X) + "," + strconv.Itoa(ruObj.BottomRight.Y))
	
}


func simulate() {

	for i := 0; i < SIMULATION_STEPS; i++ {

		free_space()

		for key, _ := range RoadUsers {
			location(key)
		}
		
		for _, rubbox := range RoadUsers {
			rubbox.Move()
		}

	}

}

func computeBoundingBox(height int, width int) (*BoundingBox) {
	xMin := rand.Intn(500)
	yMin := rand.Intn(500)
	xMax :=  xMin + width //xMin + rand.Intn(width - xMin)
	yMax :=  yMin + height //yMin + rand.Intn(height - yMin)

	topLeftPoint := Point{X: xMin, Y: yMax}
	bottomRightPoint := Point{X: xMax, Y: yMin}

	return &BoundingBox{TopLeft: &topLeftPoint, BottomRight: &bottomRightPoint}

}

func newFreeSpace() {

	appendLine("exit_fspace," + strconv.Itoa(FreeSpace.TopLeft.X) + "," + strconv.Itoa(FreeSpace.TopLeft.Y) + "," + strconv.Itoa(FreeSpace.BottomRight.X) + "," + strconv.Itoa(FreeSpace.BottomRight.Y))

	FreeSpace = computeBoundingBox(100, 50)

	appendLine("new_fspace," + strconv.Itoa(FreeSpace.TopLeft.X) + "," + strconv.Itoa(FreeSpace.TopLeft.Y) + "," + strconv.Itoa(FreeSpace.BottomRight.X) + "," + strconv.Itoa(FreeSpace.BottomRight.Y))

}

func initialize() {

	FreeSpace = computeBoundingBox(100, 50)
	appendLine("new_fspace," + strconv.Itoa(FreeSpace.TopLeft.X) + "," + strconv.Itoa(FreeSpace.TopLeft.Y) + "," + strconv.Itoa(FreeSpace.BottomRight.X) + "," + strconv.Itoa(FreeSpace.BottomRight.Y))

	for i := 0; i < ROAD_USERS; i++ {
	 
		RoadUsers["ru"+strconv.Itoa(i)] = computeBoundingBox( 10 + rand.Intn(20),  5 + rand.Intn(10))
	}
}

func main() {

	rand.Seed(time.Now().UnixNano())

	if err := openFile(); err != nil {
		fmt.Println("Error: ", err)
		return
	}

	defer closeFile()

	
	RoadUsers = make(map[string]*BoundingBox)

	fmt.Print("Number of road users: ")
	fmt.Scan(&ROAD_USERS)
	fmt.Println(ROAD_USERS)

	initialize()
	simulate()

}

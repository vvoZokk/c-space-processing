package cspacegen

import (
	"fmt"
	"testing"
)

var testResult *CSpace

func BenchmarkGenerateCSpace(b *testing.B) {
	var r *CSpace
	// benchmark your seed here
	seed := int64(20183804)
	for f := 0; f <= MaxFullness; f++ {
		r, _ = GenerateCSpace(DefaultSize, DefaultSize, DefaultSize, f, seed)
	}
	testResult = r
}

func ExampleGenerateCSpace() {
	size := 10.0
	fullness := 0
	seed := int64(99)
	space, _ := GenerateCSpace(size, size, size, fullness, seed)
	result, _ := space.Serialize()
	fmt.Println(result)
	// Output:
	//{"Description":"c-space 10.00 x 10.00 x 10.00, empty, seed 99","Border":[{"X":0,"Y":0,"Z":0},{"X":10,"Y":0,"Z":0},{"X":10,"Y":10,"Z":0},{"X":0,"Y":10,"Z":0},{"X":0,"Y":10,"Z":10},{"X":0,"Y":0,"Z":10},{"X":10,"Y":0,"Z":10},{"X":10,"Y":10,"Z":10}],"Start":{"X":0,"Y":0,"Z":0},"Finish":{"X":10,"Y":10,"Z":10},"Obstacle":[]}
}

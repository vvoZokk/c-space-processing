package main

import (
	"bufio"
	"c-space-processing/generator/cspacegen"
	"flag"
	"fmt"
	"os"
	"time"
)

func main() {
	// set flags
	outputFlag := flag.String("o", "", "set file name to write JSON")
	dimensionFlag := flag.Float64("d", cspacegen.DefaultSize, "set c-space dimension")
	seedFlag := flag.Int64("s", 0, "set seed for random generator, zero to use system time")
	fullnessFlag := flag.Int("f", cspacegen.MaxFullness/2, "set c-space fullness from 0 to "+fmt.Sprint(cspacegen.MaxFullness))
	flag.Parse()

	// output
	outFile := os.Stdout
	defer outFile.Close()
	if *outputFlag != "" {
		if file, err := os.Create(*outputFlag); err != nil {
			fmt.Println(">", err)
			os.Exit(1)
		} else {
			outFile = file
		}
	}
	// sizes
	size := *dimensionFlag
	if size <= 0 {
		size = 10
		fmt.Println("> incorrect size, used default value (10.0)")
	}
	// fullnes
	fullness := *fullnessFlag
	if fullness < 0 || fullness > cspacegen.MaxFullness {
		fullness = cspacegen.MaxFullness / 2
		fmt.Println("> incorrect fullness, used value " + fmt.Sprint(cspacegen.MaxFullness/2))
	}
	// seed for random number generator
	seed := *seedFlag
	if seed == 0 {
		seed = time.Now().UnixNano()
	}

	// generating
	if *outputFlag != "" {
		fmt.Printf("> start c-space generation with size %.2f x %.2f x %.2f\n", size, size, size)
	}
	if cSpace, err := cspacegen.GenerateCSpace(size, size, size, fullness, seed); err != nil {
		fmt.Println(">", err)
		os.Exit(1)
	} else {
		if *outputFlag != "" {
			fmt.Println("> generation done, write output JSON to file", *outputFlag)
		}
		if json, err := cSpace.Serialize(); err != nil {
			fmt.Println(">", err)
			os.Exit(1)
		} else {
			writer := bufio.NewWriter(outFile)
			writer.WriteString(json)
			if err := writer.Flush(); err != nil {
				fmt.Println(">", err)
				os.Exit(1)
			}
			if *outputFlag != "" {
				fmt.Println("> writing done")
			}
		}
	}
}

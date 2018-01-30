// Package cspacegen provides types and functions for generating and JSON serializing
// 3D configuration spaces with obstacles.
package cspacegen

import (
	"errors"
	"fmt"
	"math/rand"
)

// Indexes of coordinates.
const (
	X = 0 // x coordinate
	Y = 1 // y coordinate
	Z = 2 // z coordinate
)

// Default vales of parameters.
const (
	DefaultSize = 10 // size
	MaxFullness = 9  // maximum of fullness
)

const (
	insideOffset = 0.33
)

// Point3D stores three coordinates x, y, z as slice.
type Point3D []float64

// Obstacle contains origin point, three sizes and offsets for its boundary and center points.
type Obstacle struct {
	origin      Point3D
	size        Point3D
	offset      []Point3D
	centerPoint map[int]Point3D
}

// CSpace contains description, three sizes, start and finish points, slice of obstacles.
type CSpace struct {
	description   string
	dimension     Point3D
	start, finish Point3D
	obstacles     []Obstacle
}

// NewPoint returns new Point3D by three float numbers.
func NewPoint(x, y, z float64) *Point3D {
	p := make(Point3D, 3, 3)
	p[X] = x
	p[Y] = y
	p[Z] = z
	return &p
}

// String returns point coordinates as string.
func (p *Point3D) String() string {
	return fmt.Sprintf("%.2f %.2f %.2f", (*p)[X], (*p)[Y], (*p)[Z])
}

func (p *Point3D) shift(s Point3D) *Point3D {
	result := NewPoint(s[X], s[Y], s[Z])
	(*result)[X] += (*p)[X]
	(*result)[Y] += (*p)[Y]
	(*result)[Z] += (*p)[Z]
	return result
}

func (p *Point3D) scale(s Point3D) *Point3D {
	result := NewPoint(s[X], s[Y], s[Z])
	(*result)[X] *= (*p)[X]
	(*result)[Y] *= (*p)[Y]
	(*result)[Z] *= (*p)[Z]
	return result
}

func zeroBoundaryOffsetSlice() []Point3D {
	result := make([]Point3D, 8, 8)
	for i := range result {
		switch i {
		case 0:
			result[i] = *NewPoint(0, 0, 0)
		case 1:
			result[i] = *NewPoint(1, 0, 0)
		case 2:
			result[i] = *NewPoint(1, 1, 0)
		case 3:
			result[i] = *NewPoint(0, 1, 0)
		case 4:
			result[i] = *NewPoint(0, 1, 1)
		case 5:
			result[i] = *NewPoint(0, 0, 1)
		case 6:
			result[i] = *NewPoint(1, 0, 1)
		case 7:
			result[i] = *NewPoint(1, 1, 1)
		}
	}
	return result
}

func zeroCenterOffset(i int) *Point3D {
	var result *Point3D
	switch i {
	case 8:
		result = NewPoint(0.5, 0.5, 0)
	case 9:
		result = NewPoint(0.5, 0, 0.5)
	case 10:
		result = NewPoint(1, 0.5, 0.5)
	case 11:
		result = NewPoint(0.5, 1, 0.5)
	case 12:
		result = NewPoint(0, 0.5, 0.5)
	case 13:
		result = NewPoint(0.5, 0.5, 1)
	}
	return result
}

// NewObstacle creates new obstacle with zero offsets.
func NewObstacle() *Obstacle {
	origin := *NewPoint(0, 0, 0)
	size := *NewPoint(1, 1, 1)
	offset := zeroBoundaryOffsetSlice()
	return &Obstacle{origin, size, offset, make(map[int]Point3D, 0)}
}

// NewObstacleByPoints creates new obstacle with zero offsets by origin point and sizes.
func NewObstacleByPoints(o, s *Point3D) *Obstacle {
	return &Obstacle{*o, *s, zeroBoundaryOffsetSlice(), make(map[int]Point3D, 0)}
}

// volume returns value of obstacle volume.
func (o *Obstacle) volume() float64 {
	return o.size[X] * o.size[Y] * o.size[Z]
}

// centerPointIndex calculates and returns index of center point by number.
func (o *Obstacle) centerPointIndex(i int) int {
	index := 7
	for j := 8; j < 13; j++ {
		if _, ok := o.centerPoint[j]; ok {
			index++
		}
		if index == i {
			index = j
			break
		}
	}
	return index
}

// point returns obstacle edge or center point by number from 0 to 13.
func (o *Obstacle) point(i int) *Point3D {
	if i < 8 {
		return o.origin.shift(*o.size.scale(o.offset[i]))
	}
	index := o.centerPointIndex(i)
	return o.origin.shift(*o.size.scale(o.centerPoint[index]))
}

// points returns obstacle edges as slice of 3D points.
func (o *Obstacle) points() []*Point3D {
	size := 8 + len(o.centerPoint)
	result := make([]*Point3D, size, size)
	for i := range result {
		result[i] = o.point(i)
	}
	return result
}

// Function checkInterfering returns true if obstacle interferes with a space around box,
// which has origin point p and sizes x, y, z.
func (o *Obstacle) checkInterfering(p Point3D, x, y, z, offset float64) bool {
	checkX := p[X]+x+offset < o.origin[X] || p[X] > o.origin[X]+o.size[X]+offset
	checkY := p[Y]+y+offset < o.origin[Y] || p[Y] > o.origin[Y]+o.size[Y]+offset
	checkZ := p[Z]+z+offset < o.origin[Z] || p[Z] > o.origin[Z]+o.size[Z]+offset
	return !(checkX || checkY || checkZ)
}

// Function checkPointInterfering returns true if obstacle interferes with a cubic space around point p.
func (o *Obstacle) checkPointInterfering(p Point3D, offset float64) bool {
	return o.checkInterfering(p, 0, 0, 0, offset)
}

func (o *Obstacle) copy() *Obstacle {
	result := NewObstacle()
	for i := 0; i < 3; i++ {
		result.origin[i] = o.origin[i]
		result.size[i] = o.size[i]
	}
	for i := range o.offset {
		result.offset[i] = o.offset[i]
	}
	return result
}

// GenerateCSpace returns new generated c-space by x y z sizes, fullness and seed for random generator.
func GenerateCSpace(x, y, z float64, f int, seed int64) (*CSpace, error) {
	if x <= 0 || y <= 0 || z <= 0 {
		return nil, errors.New("incorrect c-space dimensions")
	}
	if f < 0 || f > MaxFullness {
		return nil, errors.New("incorrect c-space fullness")
	}

	// c-space dimensions
	dimension := *NewPoint(x, y, z)
	r := rand.New(rand.NewSource(seed))
	edgeSize := x
	if y <= x && y <= z {
		edgeSize = y
	}
	if z <= x && z <= y {
		edgeSize = z
	}
	// c-space fullness
	fullness := fmt.Sprintf("fullness value %d", f)
	quantity := 0
	rate := 1.0 - float64(f)/(MaxFullness/0.9)
	switch {
	case f > 0 && f <= MaxFullness/3:
		// minimum fullness
		quantity = f/(MaxFullness/0.9) + r.Intn(2+f)
	case f > MaxFullness/3 && f <= 2*MaxFullness/3:
		// medium fullness
		quantity = 1 + f/(MaxFullness/0.9) + r.Intn(1+f)
	case f > 2*MaxFullness/3 && f <= MaxFullness:
		// maximum fullness
		quantity = 2 + f/(MaxFullness/0.9) + r.Intn(f)
	default:
		fullness = "empty"
		rate = 1
	}
	//fmt.Println("Debug: rate", rate)
	zeroPoint := *NewPoint(0, 0, 0)
	edgePoint := *NewPoint(x, y, z)

	var obstacles []Obstacle
	// minimum obstacle size
	minSize := 0.1 * edgeSize
	minDistance := rate * minSize

	//start and finish points
	size := minSize - minDistance
	start := *NewPoint(size*r.Float64(), size*r.Float64(), size*r.Float64())
	finish := *NewPoint(x-size*r.Float64(), y-size*r.Float64(), z-size*r.Float64())

	volume := x * y * z
	freeVolume := volume
	scaleRatio := 0.1 // means size increasing rate
	iteration := 0
	for freeVolume > rate*volume {
		if iteration == 0 {
			obstacles = make([]Obstacle, 0, quantity)
			for len(obstacles) < quantity {
				for true {
					p := NewPoint((x-minSize)*r.Float64(), (y-minSize)*r.Float64(), (z-minSize)*r.Float64())
					// new obstacle with origin point p
					nextObstacle := NewObstacleByPoints(p, NewPoint(minSize, minSize, minSize))
					if nextObstacle.checkPointInterfering(zeroPoint, minSize) {
						continue
					}
					if nextObstacle.checkPointInterfering(edgePoint, minSize) {
						continue
					}
					unsuitablePoint := false
					for _, o := range obstacles {
						if o.checkInterfering(*p, minSize, minSize, minSize, minSize) {
							unsuitablePoint = true
							break
						}
					}
					if unsuitablePoint {
						continue
					} else {
						obstacles = append(obstacles, *nextObstacle)
						break
					}
				} // endless cycle
			} // quantity cycle
		} // iteration check
		if rate > 0 && len(obstacles) == 0 {
			return nil, errors.New("initiation failed")
		}
		number := r.Intn(len(obstacles))
		c := obstacles[number].copy()
		dimension := r.Intn(3)
		if r.Intn(2) == 0 { // probability value is 1/2
			scale := 1 + scaleRatio*r.Float64()
			c.size[dimension] *= scale
			if c.origin[dimension]+c.size[dimension] > edgePoint[dimension] {
				c.size[dimension] = edgePoint[dimension] - c.origin[dimension]
			}
		} else {
			scale := (1 - scaleRatio) + scaleRatio*r.Float64()
			size := c.origin[dimension] * scale
			c.size[dimension] += c.origin[dimension] - size
			c.origin[dimension] = size
		}
		unsuitableChange := false
		if c.checkPointInterfering(zeroPoint, minSize) || c.checkPointInterfering(edgePoint, minSize) {
			unsuitableChange = true
		} else {
			for n, o := range obstacles {
				if n != number && o.checkInterfering(c.origin, c.size[X], c.size[Y], c.size[Z], minDistance) {
					unsuitableChange = true
					break
				}
			}
		}
		iteration++
		if !unsuitableChange {
			// applying obstacle changes
			obstacles[number] = *c
			freeVolume = volume
			for _, o := range obstacles {
				freeVolume -= o.volume()
			}
		} else {
			if iteration%5000 == 0 {
				//fmt.Println("Debug: Generation iteration failed, start new initiation")
				iteration = 0
			}
		}
	} // freeVolume cycle

	random := func() float64 {
		return 0.5 + 0.5*r.Float64()
	}
	// set point offsets
	for _, o := range obstacles {
		// boundary points offsets
		for i, point := range o.offset {
			for index := range point {
				if r.Intn(5) < 4 { // probability value is 4/5
					if o.offset[i][index] < 0.5 {
						if r.Intn(2) == 0 { // probability value is 1/2
							o.offset[i][index] = random() * insideOffset
						} else {
							o.offset[i][index] = -random() * minDistance / (2 * o.size[index])
							if o.origin[index]+o.size[index]*o.offset[i][index] < zeroPoint[index] {
								o.offset[i][index] = (zeroPoint[index] - o.origin[index]) / o.size[index]
							}
						}
					} else {
						if r.Intn(2) == 0 { // probability value is 1/2
							o.offset[i][index] = 1 - random()*insideOffset
						} else {
							o.offset[i][index] = 1 + random()*minDistance/(2*o.size[index])
							if o.origin[index]+o.size[index]*o.offset[i][index] > edgePoint[index] {
								o.offset[i][index] = (edgePoint[index] - o.origin[index]) / o.size[index]
							}
						}
					}
				} // random check
			} // point cycle
		} // offset cycle
		// central points offsets
		for i := 8; i < 13; i++ {
			if r.Intn(5) < 3 { // probability value is 3/5
				p := *zeroCenterOffset(i)
				for index := 0; index < 3; index++ {
					if r.Intn(3) < 2 { // probability value is 2/3
						switch {
						case p[index] == 0:
							if r.Intn(2) == 0 { // probability value is 1/2
								p[index] = random() * insideOffset
							} else {
								p[index] = -random() * minDistance / (2 * o.size[index])
								if o.origin[index]+o.size[index]*p[index] < zeroPoint[index] {
									p[index] = (zeroPoint[index] - o.origin[index]) / o.size[index]
								}
							}
						case p[index] == 1:
							if r.Intn(2) == 0 { // probability value is 1/2
								p[index] = 1 + random()*minDistance/(2*o.size[index])
								if o.origin[index]+o.size[index]*p[index] > edgePoint[index] {
									p[index] = (edgePoint[index] - o.origin[index]) / o.size[index]
								}
							} else {
								p[index] = 1 - random()*insideOffset
							}
						default:
							p[index] = (1-insideOffset)/2 + random()*insideOffset
						} // offset check
					} // random check
				} // index cycle
				o.centerPoint[i] = p
			} // random check
		}
	}
	description := fmt.Sprintf("c-space %.2f x %.2f x %.2f, %s, seed %d", x, y, z, fullness, seed)
	//fmt.Println("Debug: quantity of obstacles", len(obstacles))
	return &CSpace{description, dimension, start, finish, obstacles}, nil
}

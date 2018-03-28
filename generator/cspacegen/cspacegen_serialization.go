package cspacegen

import (
	"encoding/json"
)

// point struct to serialize.
type point struct {
	X, Y, Z float64
}

// triangle struct to serialize.
type triangle struct {
	First, Second, Third int
}

// obstacle struct to serialize.
type obstacle struct {
	Vertex []point
	Facet  []triangle
}

// cspace struct to serialize.
type cspace struct {
	Description string
	Vertex      []point
	Start       point
	Finish      point
	Obstacle    []obstacle
}

// Serialize returns serialized JSON or error.
func (f *CSpace) Serialize() (string, error) {
	vertex := make([]point, 0, 8)
	vertex = append(vertex, point{0, 0, 0})
	vertex = append(vertex, point{f.dimension[X], 0, 0})
	vertex = append(vertex, point{f.dimension[X], f.dimension[Y], 0})
	vertex = append(vertex, point{0, f.dimension[Y], 0})
	vertex = append(vertex, point{0, f.dimension[Y], f.dimension[Z]})
	vertex = append(vertex, point{0, 0, f.dimension[Z]})
	vertex = append(vertex, point{f.dimension[X], 0, f.dimension[Z]})
	vertex = append(vertex, point{f.dimension[X], f.dimension[Y], f.dimension[Z]})
	obstacles := make([]obstacle, 0)
	for _, o := range f.obstacles {
		obstacles = append(obstacles, o.toStruct())
	}
	space := cspace{f.description, vertex, f.start.toStruct(), f.finish.toStruct(), obstacles}
	result, err := json.Marshal(space)
	if err != nil {
		return "", err
	}
	return string(result), nil
}

// toStruct converts Point3D to serializable struct.
func (p *Point3D) toStruct() point {
	return point{(*p)[X], (*p)[Y], (*p)[Z]}
}

// toStruct converts Obstacle to serializable struct.
func (o *Obstacle) toStruct() obstacle {
	edge := make([]point, 0)
	for _, p := range o.points() {
		edge = append(edge, p.toStruct())
	}
	facet := make([]triangle, 0, 0)
	index := 7
	if _, ok := o.centerPoint[8]; ok {
		index++
		facet = append(facet, triangle{0, 3, index})
		facet = append(facet, triangle{0, index, 1})
		facet = append(facet, triangle{1, index, 2})
		facet = append(facet, triangle{2, index, 3})
	} else {
		facet = append(facet, triangle{0, 3, 2})
		facet = append(facet, triangle{0, 2, 1})
	}

	if _, ok := o.centerPoint[9]; ok {
		index++
		facet = append(facet, triangle{6, 5, index})
		facet = append(facet, triangle{6, index, 1})
		facet = append(facet, triangle{1, index, 0})
		facet = append(facet, triangle{0, index, 5})
	} else {
		facet = append(facet, triangle{6, 5, 0})
		facet = append(facet, triangle{6, 0, 1})
	}

	if _, ok := o.centerPoint[10]; ok {
		index++
		facet = append(facet, triangle{1, 2, index})
		facet = append(facet, triangle{1, index, 6})
		facet = append(facet, triangle{6, index, 7})
		facet = append(facet, triangle{7, index, 2})
	} else {
		facet = append(facet, triangle{1, 2, 7})
		facet = append(facet, triangle{1, 7, 6})
	}

	if _, ok := o.centerPoint[11]; ok {
		index++
		facet = append(facet, triangle{2, 3, index})
		facet = append(facet, triangle{2, index, 7})
		facet = append(facet, triangle{7, index, 4})
		facet = append(facet, triangle{4, index, 3})
	} else {
		facet = append(facet, triangle{2, 3, 4})
		facet = append(facet, triangle{2, 4, 7})
	}

	if _, ok := o.centerPoint[12]; ok {
		index++
		facet = append(facet, triangle{5, 4, index})
		facet = append(facet, triangle{5, index, 0})
		facet = append(facet, triangle{0, index, 3})
		facet = append(facet, triangle{3, index, 4})
	} else {
		facet = append(facet, triangle{5, 4, 3})
		facet = append(facet, triangle{5, 3, 0})
	}

	if _, ok := o.centerPoint[13]; ok {
		index++
		facet = append(facet, triangle{4, 5, index})
		facet = append(facet, triangle{4, index, 7})
		facet = append(facet, triangle{7, index, 6})
		facet = append(facet, triangle{6, index, 5})
	} else {
		facet = append(facet, triangle{4, 5, 6})
		facet = append(facet, triangle{4, 6, 7})
	}

	return obstacle{edge, facet}
}

package cspacegen

import (
	"encoding/json"
	"fmt"
	"math"
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

// vectorSubtraction returns result of a - b.
func vectorSubtraction(a, b point) point {
	result := a
	result.X -= b.X
	result.Y -= b.Y
	result.Z -= b.Z
	return result
}

// serializationPointsToFacet returns STL facet as string.
func serializationPointsToFacet(a, b, c point) string {
	ab := vectorSubtraction(b, a)
	bc := vectorSubtraction(c, b)
	normal := b
	normal.X = ab.Y*bc.Z - ab.Z*bc.Y
	normal.Y = ab.Z*bc.X - ab.X*bc.Z
	normal.Z = ab.X*bc.Y - ab.Y*bc.X
	length := math.Sqrt(normal.X*normal.X + normal.Y*normal.Y + normal.Z*normal.Z)
	normal.X /= length
	normal.Y /= length
	normal.Z /= length
	result := fmt.Sprintf(" facet normal %e %e %e\n  outer loop\n", normal.X, normal.Y, normal.Z)
	result += fmt.Sprintf("   vertex %e %e %e\n", a.X, a.Y, a.Z)
	result += fmt.Sprintf("   vertex %e %e %e\n", b.X, b.Y, b.Z)
	result += fmt.Sprintf("   vertex %e %e %e\n", c.X, c.Y, c.Z)
	result += "  endloop\n endfacet\n"
	return result
}

// SerializeToJSON returns serialized JSON or error.
func (c *CSpace) SerializeToJSON() (string, error) {
	vertex := make([]point, 0, 8)
	vertex = append(vertex, point{0, 0, 0})
	vertex = append(vertex, point{c.dimension[X], 0, 0})
	vertex = append(vertex, point{c.dimension[X], c.dimension[Y], 0})
	vertex = append(vertex, point{0, c.dimension[Y], 0})
	vertex = append(vertex, point{0, c.dimension[Y], c.dimension[Z]})
	vertex = append(vertex, point{0, 0, c.dimension[Z]})
	vertex = append(vertex, point{c.dimension[X], 0, c.dimension[Z]})
	vertex = append(vertex, point{c.dimension[X], c.dimension[Y], c.dimension[Z]})
	obstacles := make([]obstacle, 0)
	for _, o := range c.obstacles {
		obstacles = append(obstacles, o.toStruct())
	}
	space := cspace{c.description, vertex, c.start.toStruct(), c.finish.toStruct(), obstacles}
	result, err := json.Marshal(space)
	if err != nil {
		return "", err
	}
	return string(result), nil
}

// SerializeToSTL returns serialized ASCII STL or error.
func (c *CSpace) SerializeToSTL() (string, error) {
	vertex := make([]point, 0, 8)
	vertex = append(vertex, point{0, 0, 0})
	vertex = append(vertex, point{c.dimension[X], 0, 0})
	vertex = append(vertex, point{c.dimension[X], c.dimension[Y], 0})
	vertex = append(vertex, point{0, c.dimension[Y], 0})
	vertex = append(vertex, point{0, c.dimension[Y], c.dimension[Z]})
	vertex = append(vertex, point{0, 0, c.dimension[Z]})
	vertex = append(vertex, point{c.dimension[X], 0, c.dimension[Z]})
	vertex = append(vertex, point{c.dimension[X], c.dimension[Y], c.dimension[Z]})

	result := "solid " + c.description + "\n"
	result += serializationPointsToFacet(vertex[0], vertex[1], vertex[2])
	result += serializationPointsToFacet(vertex[2], vertex[3], vertex[0])
	result += serializationPointsToFacet(vertex[3], vertex[4], vertex[5])
	result += serializationPointsToFacet(vertex[5], vertex[0], vertex[3])
	result += serializationPointsToFacet(vertex[0], vertex[5], vertex[6])
	result += serializationPointsToFacet(vertex[6], vertex[1], vertex[0])
	result += serializationPointsToFacet(vertex[1], vertex[6], vertex[7])
	result += serializationPointsToFacet(vertex[7], vertex[2], vertex[1])
	result += serializationPointsToFacet(vertex[2], vertex[7], vertex[4])
	result += serializationPointsToFacet(vertex[4], vertex[3], vertex[2])
	result += serializationPointsToFacet(vertex[4], vertex[7], vertex[6])
	result += serializationPointsToFacet(vertex[6], vertex[5], vertex[4])
	for _, obstacle := range c.obstacles {
		o := obstacle.toStruct()
		for _, f := range o.Facet {
			result += serializationPointsToFacet(o.Vertex[f.First], o.Vertex[f.Second], o.Vertex[f.Third])
		}
	}
	result += "endsolid "
	return result, nil
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

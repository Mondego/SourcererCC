/* The following code example is taken from the book
 * "Object-Oriented Programming in C++"
 * by Nicolai M. Josuttis, Wiley, 2002
 *
 * (C) Copyright Nicolai M. Josuttis 2002.
 * Permission to copy, use, modify, sell and distribute this software
 * is granted provided this copyright notice appears in all copies.
 * This software is provided "as is" without express or implied
 * warranty, and with no claim as to its suitability for any purpose.
 */
// header file for I/O
#include <iostream>

// header files for used classes
#include "line.hpp"
#include "circle.hpp"
#include "geogroup.hpp"

int main()
{
    Geo::Line l1(Geo::Coord(1,2), Geo::Coord(3,4));
    Geo::Line l2(Geo::Coord(7,7), Geo::Coord(0,0));
    Geo::Circle c(Geo::Coord(3,3), 11);

    Geo::GeoGroup g;

    g.add(l1);            // GeoGroup contains: - line l1
    g.add(c);             //     - circle c
    g.add(l2);            //     - line l2

    g.draw();             // draw GeoGroup
    std::cout << std::endl;

    g.move(Geo::Coord(3,-3));  // move offset of GeoGroup
    g.draw();                  // draw GeoGroup again
    std::cout << std::endl;

    g.remove(l1);         // GeoGroup now only contains c and l2
    g.draw();             // draw GeoGroup again
}

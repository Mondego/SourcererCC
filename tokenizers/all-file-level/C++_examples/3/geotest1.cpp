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
// header files for used classes
#include "line.hpp"
#include "circle.hpp"
#include "geoobj.hpp"

// forward declaration
void printGeoObj(const Geo::GeoObj&);

int main()
{
    Geo::Line l1(Geo::Coord(1,2), Geo::Coord(3,4));
    Geo::Line l2(Geo::Coord(7,7), Geo::Coord(0,0));
    Geo::Circle c(Geo::Coord(3,3), 11);

    // array as an inhomogenous collection of geometric objects:
    Geo::GeoObj* coll[10];

    coll[0] = &l1;     // collection contains: - line l1
    coll[1] = &c;      //     - circle c
    coll[2] = &l2;     //     - line l2

    /* move and draw elements in the collection
     * - the correct function is called automatically
     */
    for (int i=0; i<3; i++) {
        coll[i]->draw();
        coll[i]->move(Geo::Coord(3,-3));
    }

    // output individual objects via auxiliary function
    printGeoObj(l1);
    printGeoObj(c);
    printGeoObj(l2);
}

void printGeoObj(const Geo::GeoObj& obj)
{
    /* the correct function is called automatically
     */
    obj.draw();
}

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
#ifndef CIRCLE_HPP
#define CIRCLE_HPP

// header file for I/O
#include <iostream>

// header file of the base class
#include "geoobj.hpp"

namespace Geo {

/* class Circle
 * - derived from GeoObj
 * - a circle consists of:
 *     - a center point (reference point, inherited)
 *     - a radius (new)
 */
class Circle : public GeoObj {
  protected:
    unsigned radius;    // radius

  public:
    // constructor for center point and radius
    Circle(const Coord& m, unsigned r)
         : GeoObj(m), radius(r) {
    }

    // draw geometric object (now implemented)
    virtual void draw() const;

    // virtual destructor
    virtual ~Circle() {
    }
};

/* drawing
 * - defined inline
 */
inline void Circle::draw() const
{
    std::cout << "Circle around center point " << refpoint
              << " with radius " << radius << std::endl;
}

}  // namespace Geo

#endif // CIRCLE_HPP

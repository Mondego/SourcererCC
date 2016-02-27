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
#ifndef GEOOBJ_HPP
#define GEOOBJ_HPP

// header file for coordinates
#include "coord.hpp"

namespace Geo {

/* abstract base class GeoObj
 * - common base class for geometric objects
 * - provided for inheritance
 */
class GeoObj {
  protected:
    // every GeoObj has a reference point
    Coord refpoint;

    /* constructor for an initial reference point
     * - not public
     * - there is no default constructor available
     */
    GeoObj(const Coord& p) : refpoint(p) {
    }

  public:
    // move geometric object according to passed relative offset
    virtual void move(const Coord& offset) {
        refpoint += offset;
    }

    /* draw geometric object
     * - pure virtual function
     */
    virtual void draw() const = 0;

    // virtual destructor
    virtual ~GeoObj() {
    }
};

}  // namespace Geo

#endif  // GEOOBJ_HPP

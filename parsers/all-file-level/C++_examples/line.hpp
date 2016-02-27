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
#ifndef LINE_HPP
#define LINE_HPP

// header file for I/O
#include <iostream>

// header file of the base class
#include "geoobj.hpp"

namespace Geo {

/* class Line
 * - derived from GeoObj
 * - a line consists of:
 *     - a start point (refernce point, inherited)
 *     - an end point (new)
 */
class Line : public GeoObj {
  protected:
    Coord p2;    // second point, end point

  public:
    // constructor for start and end points
    Line(const Coord& a, const Coord& b)
         : GeoObj(a), p2(b) {
    }

    // draw geometric object (now implemented)
    virtual void draw() const;

    // move geometric object (reimplemented)
    virtual void move(const Coord&);

    // virtual destructor
    virtual ~Line() {
    }
};

/* output
 * - defined inline
 */
inline void Line::draw() const
{
    std::cout << "Line from " << refpoint
              << " to " << p2 << std::endl;
}

/* move
 * - reimplemented, inline
 */
inline void Line::move(const Coord& offset)
{
    refpoint += offset;    // represents GeoObj::move(offset);
    p2 += offset;
}

}  // namespace Geo

#endif // LINE_HPP

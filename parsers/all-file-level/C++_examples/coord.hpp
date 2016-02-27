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
#ifndef COORD_HPP
#define COORD_HPP

// header file for I/O
#include <iostream>

namespace Geo {

/* class Coord
 * - auxiliary class for geometric objects
 * - not suitable for inheritance
 */
class Coord {
  private:
    int x;     // X coordinate
    int y;     // Y coordinate

  public:
    // default constructor, and two-parameter constructor
    Coord() : x(0), y(0) {     // default values: 0
    }
    Coord(int newx, int newy) : x(newx), y(newy) {
    }

    Coord operator + (const Coord&) const;    // addition
    Coord operator - () const;                // negation
    void  operator += (const Coord&);         // +=
    void  printOn(std::ostream& strm) const;  // output
};

/* operator +
 * - add X and Y coordinates
 */
inline Coord Coord::operator + (const Coord& p) const
{
    return Coord(x+p.x,y+p.y);
}

/* unary operator -
 * - negate X and Y coordinates
 */
inline Coord Coord::operator - () const
{
    return Coord(-x,-y);
}

/* operator +=
 * - add offset to X and Y coordinates
 */
inline void Coord::operator += (const Coord& p)
{
    x += p.x;
    y += p.y;
}

/* printOn()
 * - output coordinates as a pair of values
 */
inline void Coord::printOn(std::ostream& strm) const
{
    strm << '(' << x << ',' << y << ')';
}

/* operator <<
 * - conversion for standard output operator
 */
inline std::ostream& operator<< (std::ostream& strm, const Coord& p)
{
    p.printOn(strm);
    return strm;
}

}  // namespace Geo

#endif // COORD_HPP

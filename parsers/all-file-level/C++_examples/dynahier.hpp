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
#include "coord.hpp"

// common abstract base class GeoObj for geometric objects
class GeoObj {
  public:
    // draw geometric object:
    virtual void draw() const = 0;
    // return position of geometric object:
    virtual Coord position() const = 0;
    //...
};

// concrete geometric object class Circle
// - derived from GeoObj
class Circle : public GeoObj {
  public:
    virtual void draw() const;
    virtual Coord position() const;
    //...
};

// concrete geometric object class Line
// - derived from GeoObj
class Line : public GeoObj {
  public:
    virtual void draw() const;
    virtual Coord position() const;
    //...
};
//...

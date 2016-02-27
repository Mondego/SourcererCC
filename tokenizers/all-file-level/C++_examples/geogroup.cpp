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
#include "geogroup.hpp"
#include <algorithm>

namespace Geo {

/* add
 * - insert element
 */
void GeoGroup::add(GeoObj& obj)
{
    // keep address of the passed geometric object
    elems.push_back(&obj);
}

/* draw
 * - draw all elements, taking the reference points into account
 */
void GeoGroup::draw() const
{
    for (unsigned i=0; i<elems.size(); ++i) {
        elems[i]->move(refpoint);   // add offset for the reference point
        elems[i]->draw();           // draw element
        elems[i]->move(-refpoint);  // subtract offset
    }
}

/* remove
 * - delete element
 */
bool GeoGroup::remove(GeoObj& obj)
{
    // find first element with this address and remove it
    // return whether an object was found and removed
    std::vector<GeoObj*>::iterator pos;
    pos = std::find(elems.begin(),elems.end(),&obj);
    if (pos != elems.end()) {
        elems.erase(pos);
        return true;
    }
    else {
        return false;
    }
}

}  // namespace Geo

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
// draw any geometric object
void myDraw (const GeoObj& obj)
{
    obj.draw();
}

// process distance between two geometric objects
Coord distance (const GeoObj& x1, const GeoObj& x2)
{
    Coord a = x1.position() - x2.position();
    return a.abs();
}

// draw inhomogenous collection of geometric objects
void drawElems (const std::vector<GeoObj*>& elems)
{
    for (unsigned i=0; i<elems.size(); ++i) {
        elems[i]->draw();
    }
}

int main()
{
    Line l;
    Circle c, c1, c2;

    myDraw(l);              // myDraw(GeoObj&) => Line::draw()
    myDraw(c);              // myDraw(GeoObj&) => Circle::draw()

    distance(c1,c2);        // distance(GeoObj&,GeoObj&)
    distance(l,c);          // distance(GeoObj&,GeoObj&)

    std::vector<GeoObj*> coll;  // inhomogenous collection
    coll.push_back(&l);         // insert line
    coll.push_back(&c);         // insert circle
    drawElems(coll);            // draw collection
}

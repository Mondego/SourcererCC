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
#include <iostream>
#include <vector>
#include <list>
#include <algorithm>

int main()
{
    std::list<int>   coll1;
    std::vector<int> coll2;

    // insert elements 1 to 6 in the first collection
    for (int i=1; i<=6; i++) {
        coll1.push_back(i);
    }

    /* RUNTIME ERROR:
     * - copy elements into the second collection
     */
    std::copy(coll1.begin(), coll1.end(),    // source range
              coll2.begin());                // destination range
}

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
#include <deque>
#include <string>

int main()
{
    std::deque<std::string> coll;    // deque container for strings

    // insert elements at the front
    coll.push_front("often");
    coll.push_front("always");
    coll.push_front("but");
    coll.push_front("always");
    coll.push_front("not");

    // output all elements followed by a space
    for (unsigned i=0; i<coll.size(); ++i) {
        std::cout << coll[i] << ' ';
    }

    // finally output a newline
    std::cout << std::endl;
}

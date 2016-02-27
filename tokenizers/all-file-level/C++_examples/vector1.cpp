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

int main()
{
    std::vector<int> coll;    // vector container for ints

    // insert elements with the values 1 to 6
    for (int i=1; i<=6; ++i) {
        coll.push_back(i);
    }

    // output all elements followed by a space
    for (unsigned i=0; i<coll.size(); ++i) {
        std::cout << coll[i] << ' ';
    }

    // finally output a newline
    std::cout << std::endl;
}

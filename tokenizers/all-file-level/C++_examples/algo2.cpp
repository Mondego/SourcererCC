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
#include <algorithm>
#include <string>

int main()
{
    std::vector<std::string> coll;            // container for strings
    std::vector<std::string>::iterator pos;   // iterator

    // insert various city names
    coll.push_back("Hamburg");
    coll.push_back("Munich");
    coll.push_back("Berlin");
    coll.push_back("Braunschweig");
    coll.push_back("Duisburg");
    coll.push_back("Leipzig");

    // sort all elements
    std::sort(coll.begin(), coll.end());

    /* insert `Hannover' in front of `Hamburg'
     * - search for position of `Hamburg'
     * - insert `Hannover' before it
     */
    pos = find(coll.begin(), coll.end(),    // range
               "Hamburg");                  // search criteria
    if (pos != coll.end()) {
        coll.insert(pos,"Hanover");
    }
    else {
        std::cerr << "oops, Hamburg is not available" << std::endl;
        coll.push_back("Hanover");
    }

    // output all elements
    for (pos=coll.begin(); pos!=coll.end(); ++pos) {
        std::cout << *pos << ' ';
    }
    std::cout << std::endl;
}

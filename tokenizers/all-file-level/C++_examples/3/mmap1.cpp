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
#include <map>
#include <string>

int main()
{
    // datatype of the collection
    typedef std::multimap<int,std::string> IntStringMMap;

    IntStringMMap coll;    // multimap container for int/string value pairs

    // insert some unordered elements
    // - two elements have the key 5
    coll.insert(std::make_pair(5,"heavy"));
    coll.insert(std::make_pair(2,"best"));
    coll.insert(std::make_pair(1,"The"));
    coll.insert(std::make_pair(4,"are:"));
    coll.insert(std::make_pair(5,"long"));
    coll.insert(std::make_pair(3,"parties"));

    /* output the values of all elements
     * - an iterator iterates over all elements
     * - using second, the value of the element is accessed
     */
    IntStringMMap::iterator pos;
    for (pos = coll.begin(); pos != coll.end(); ++pos) {
        std::cout << pos->second << ' ';
    }
    std::cout << std::endl;
}

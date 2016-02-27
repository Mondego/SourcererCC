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
#include <set>
#include <vector>
#include <algorithm>
#include <iterator>
#include <iostream>

// include the function object Add
#include "add.hpp"

int main()
{
    std::set<int>    coll1;
    std::vector<int> coll2;

    // insert the elements with the values 1 to 9 in coll1
    for (int i=1; i<=9; ++i) {
        coll1.insert(i);
    }

    // output elements in coll1
    copy(coll1.begin(), coll1.end(),                  // source: coll1
         std::ostream_iterator<int>(std::cout," "));  // target: cout
    std::cout << std::endl;

    // transform every element in coll1 to coll2
    // - add value of first element to each element
    transform(coll1.begin(),coll1.end(),    // source
              std::back_inserter(coll2),    // target (inserting)
              Add(*coll1.begin()));         // operation

    // output elements in coll2
    copy(coll2.begin(), coll2.end(),                  // source: coll1
         std::ostream_iterator<int>(std::cout," "));  // target: cout
    std::cout << std::endl;
}

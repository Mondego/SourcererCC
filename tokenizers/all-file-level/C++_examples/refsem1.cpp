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
#include <list>
#include <deque>
#include <algorithm>
#include "countptr.hpp"
using namespace std;

void printCountedPtr(CountedPtr<int> elem)
{
    cout << *elem << ' ';
}

int main()
{
    // type for smart pointer for this purpose
    typedef CountedPtr<int> IntPtr;

    // two different sets
    deque<IntPtr> coll1;
    list<IntPtr> coll2;

    // array of initial ints
    static int values[] = { 3, 5, 9, 1, 6, 4 };

    /* insert newly created ints in the sets with reference semantics
     * - same sequence in coll1
     * - reversed sequence in coll2
     */
    for (unsigned i=0; i<sizeof(values)/sizeof(values[0]); ++i) {
        IntPtr ptr(new int(values[i]));
        coll1.push_back(ptr);
        coll2.push_front(ptr);
    }

    // output content of both sets
    for_each(coll1.begin(), coll1.end(), printCountedPtr);
    cout << endl;
    for_each(coll2.begin(), coll2.end(), printCountedPtr);
    cout << endl << endl;

    /* modify elements of the sets in different places
     * - square third value in coll1
     * - negate first value in coll1
     * - set first value in coll2 to 0
     */
    *coll1[2] *= *coll1[2];
    (**coll1.begin()) *= -1;
    (**coll2.begin()) = 0;

    // output content of both sets again
    for_each(coll1.begin(), coll1.end(), printCountedPtr);
    cout << endl;
    for_each(coll2.begin(), coll2.end(), printCountedPtr);
    cout << endl;
}

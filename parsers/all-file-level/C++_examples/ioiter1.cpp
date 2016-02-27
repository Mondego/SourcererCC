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
#include <iterator>

int main()
{
    using namespace std;    // all symbols in std are global

    vector<string> coll;    // vector container for strings

    /* read strings from the standard input up until the end of the data
     * - copy from the `input collection' cin, inserting into coll
     */
    copy(istream_iterator<string>(cin),    // start of source range
         istream_iterator<string>(),       // end of source range
         back_inserter(coll));             // destination range

    // sort elements in coll
    sort(coll.begin(), coll.end());

    /* output all elements
     * - copy from coll to the `output collection' cout
     * - every string on its own line (separated by "\n")
     */
    copy(coll.begin(), coll.end(),              // source range
         ostream_iterator<string>(cout,"\n"));  // destination range
}

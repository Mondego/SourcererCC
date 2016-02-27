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
#include <limits>

inline
std::istream& ignoreLine(std::istream& strm)
{
    char c;

    // ignore all characters up to the end of the line
    strm.ignore(std::numeric_limits<int>::max(),'\n');

    // return strm for chaining
    return strm;
}

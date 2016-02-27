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
// header file of this class
#include "colstring.hpp"

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* output to stream
 */
void ColString::printOn(std::ostream& strm) const
{
    // output character sequence with colour in brackets
    String::printOn(strm);
    strm << " (in " << col << ')';
}

/* reading of a ColString from an input stream
 */
void ColString::scanFrom(std::istream& strm)
{
    // read character sequence and colour, one after the other
    String::scanFrom(strm);
    col.scanFrom(strm);
}

} // **** END namespace CPPBook ********************************


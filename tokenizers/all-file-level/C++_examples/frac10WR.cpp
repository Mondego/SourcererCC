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
// include header file of the class
#include "frac.hpp"

// include standard header files
#include <cstdlib>
#include <iostream>

// **** BEGIN Namespace CPPBook ********************************
namespace CPPBook {

#include "frac10.cpp"


/* printOn
 * - output fraction on stream strm
 */
void Fraction::printOn (std::ostream& strm) const
{
    strm << numer << '/' << denom;
}


} // **** END Namespace CPPBook ********************************

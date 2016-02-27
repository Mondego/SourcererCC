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
//...
/* operator <
 * - new: global friend function
 */
bool operator < (const Fraction& a, const Fraction& b)
{
    // because the denominator can not be negative, the following is sufficient:
    return a.numer * b.denom < b.numer * a.denom;
}
//...

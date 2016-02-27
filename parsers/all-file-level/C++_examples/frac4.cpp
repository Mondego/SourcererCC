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

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* default constructor, and one- and two-paramter constructor
 * - default for n: 0
 * - default for d: 1
 */
Fraction::Fraction(int n, int d)
{
    /* initialize numerator and denominator as passed
     * - 0 is not allowed as denominator
     * - move any negative sign for the denominator to the numerator
     */
    if (d == 0) {
        // exit program with error message
        std::cerr << "error: denominator is 0" << std::endl;
        std::exit(EXIT_FAILURE);
    }
    if (d < 0) {
        numer = -n;
        denom = -d;
    }
    else {
        numer = n;
        denom = d;
    }
}

/* operator *=
 */
const Fraction& Fraction::operator *= (const Fraction& f)
{
    // `x *= y'  =>  `x = x * y'
    *this = *this * f;

    // object (first operand) is returned
    return *this;
}

/* operator <
 */
bool Fraction::operator < (const Fraction& f) const
{
    // because the denominator cannot be negative, the following is sufficient:
    return numer * f.denom < f.numer * denom;
}

} // **** END namespace CPPBook ********************************

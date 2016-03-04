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
// include header file with the class declaration
#include "frac.hpp"

// include standard header files
#include <iostream>
#include <cstdlib>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* default constructor
 */
Fraction::Fraction()
 : numer(0), denom(1)    // initialize fraction with 0
{
    // no further instructions
}

/* constructor for integer
 */
Fraction::Fraction(int n)
 : numer(n), denom(1)    // initialize fraction with n
{
    // no further instructions
}

/* constructor for numerator and denominator
 */
Fraction::Fraction(int n, int d)
{
    // 0 is not allowed as a denomiator
    if (d == 0) {
        // exit program with error message
        std::cerr << "error: denominator is 0" << std::endl;
        std::exit(EXIT_FAILURE);
    }

    /* new: move a negative sign of the denominator to the numerator
     * this prevents, among other things, special treatment with the operator <
     */
    if (d < 0) {
        numer = -n;
        denom = -d;
    }
    else {
        numer = n;
        denom = d;
    }
}

/* print
 */
void Fraction::print()
{
    std::cout << numer << '/' << denom << std::endl;
}

/* new: operator family*
 */
Fraction Fraction::operator * (Fraction f)
{
    /* simply multiply numerator and denominator
     * - this is quicker
     */
    return Fraction(numer * f.numer, denom * f.denom);
}

/* new: operator family*=
 */
Fraction Fraction::operator *= (Fraction f)
{
    // `x *= y'  =>  `x = x * y'
    *this = *this * f;

    // return object for which the operation was called (first operand)
    return *this;
}

/* new: operator <
 */
bool Fraction::operator< (Fraction f)
{
    /* simply multiply inequality by denumerator
     * - because the denominator cannot be negative, the comparison cannot be reversed
     */
    return numer * f.denom < f.numer * denom;
}

} // **** END namespace CPPBook ********************************

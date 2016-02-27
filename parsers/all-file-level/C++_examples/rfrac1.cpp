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
// header file for min() and abs()
#include <algorithm>
#include <cstdlib>

// include header file of the separate class
#include "rfrac.hpp"

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* gcd()
 * - greatest common divisor of numerator and denominator
 */
unsigned RFraction::gcd() const
{
    if (numer == 0) {
        return denom;
    }

    /* determine the greatest number that divides, without remainder,
     * the denominator and the numerator
     */
    unsigned divisor = std::min(std::abs(numer),denom);
    while (numer % divisor != 0  ||  denom % divisor != 0) {
        --divisor;
    }
    return divisor;
}

/* reduce()
 */
void RFraction::reduce()
{
    // if reducible, divide numerator and denominator by CGD
    if (reducible) {
        int divisor = gcd();

        numer /= divisor;
        denom /= divisor;

        reducible = false;       // no longer reducible
    }
}

/* operator *=
 * - reimplemented
 */
const RFraction& RFraction::operator*= (const RFraction& f)
{
    // as with the base class:
    numer *= f.numer;
    denom *= f.denom;

    // still reduced?
    if (!reducible) {
        reducible = (gcd() > 1);
    }

    return  *this;
}

/* scanFrom()
 */
void RFraction::scanFrom(std::istream& strm)
{
    Fraction::scanFrom(strm);   // call scanFrom() of the base class

    reducible = (gcd() > 1);   // test reducibility
}

} // **** END namespace CPPBook ********************************

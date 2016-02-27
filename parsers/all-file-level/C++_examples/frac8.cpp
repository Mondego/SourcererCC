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
// include header file of the classn
#include "frac.hpp"

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* default constructor, and one- and two-paramter constructor
 * - default for n: 0
 * - default for d: 1
 */
Fraction::Fraction(int n, int d)
{
    /* initialize numerator and denominator as passed
     * - 0 is not allowed as a denominator
     * - move negative sign from the denominator to the numerator
     */
    if (d == 0) {
        // new: throw exception with error object for 0 as denominator
        throw DenomIsZero();
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
 * - global friend function
 */
bool operator < (const Fraction& a, const Fraction& b)
{
    // because the denominator cannot be negative, the following is sufficient:
    return a.numer * b.denom < b.numer * a.denom;
}


/* printOn
 * - output fraction on stream strm
 */
void Fraction::printOn(std::ostream& strm) const
{
    strm << numer << '/' << denom;
}

/* scanFrom
 * - read fraction from stream strm
 */
void Fraction::scanFrom(std::istream& strm)
{
    int n, d;

    // read numerator
    strm >> n;

    // read optional separator `/' and denominator
    if (strm.peek() == '/') {
        strm.get();
        strm >> d;
    }
    else {
        d = 1;
    }

    // read error?
    if (! strm) {
        return;
    }

    // denominator equals zero?
    if (d == 0) {
        // new: throw exception with error object for 0 as denominator
        throw DenomIsZero();
    }

    /* OK, assign read values
     * - move negative sign from the denominator to the numerator
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

// type conversion to double
double Fraction::toDouble() const
{
    // return quotient form numerator and denominator
    return double(numer)/double(denom);
}

} // **** END namespace CPPBook ********************************

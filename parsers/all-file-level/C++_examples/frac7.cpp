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

// **** BEGIN Namespace CPPBook ********************************
namespace CPPBook {

/* default constructor, constructor from integer,
 * constructor from numerator and denominator
 * - Default for n: 0
 * - Default for d: 1
 */
Fraction::Fraction (int n, int d)
{
    /* initialize numerator and denominator as passed
     * - 0 is not allowed as a denominator
     * - move a negative sign of the denominator to the numerator
     */
    if (d == 0) {
        std::cerr << "Error: numerator is 0" << std::endl;
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
    // "x *= y"  ==>  "x = x * y"
    *this = *this * f;

    // object (first operand) is returned
    return *this;
}

/* operator <
 * - global friend function
 */
bool operator < (const Fraction& a, const Fraction& b)
{
    // because the denominator can not be negative the following is sufficient:
    return a.numer * b.denom < b.numer * a.denom;
}

/* printOn
 * - output fraction on stream strm
 */
void Fraction::printOn (std::ostream& strm) const
{
    strm << numer << '/' << denom;
}

/* scanFrom
 * - read fraction from stream strm
 */
void Fraction::scanFrom (std::istream& strm)
{
    int n, d;

    // read numerator
    strm >> n;

    // read optional separator '/' and denominator
    if (strm.peek() == '/') {
        strm.get();
        strm >> d;
    }
    else {
        d = 1;
    }

    // read error ?
    if (! strm) {
        return;
    }

    // denominator == 0 ?
    if (d == 0) {
        // set failbit
        strm.clear (strm.rdstate() | std::ios::failbit);
        return;
    }

    /* OK, assign read values
     * - move a negative sign of the denominator to the numerator
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

// type conversion according to double
double Fraction::toDouble () const
{
    // return quotient form numerator and denominator
    return double(numer)/double(denom);
}

} // **** END Namespace CPPBook ********************************

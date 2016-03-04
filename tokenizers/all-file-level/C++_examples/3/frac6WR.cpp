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

/* Default constructor, constructor from integer,
 * constructor from numerator and denominator
 * - Default for n: 0
 * - Default for d: 1
 */
Fraction::Fraction (int n, int d)
{
    /* initialize numerator and denominator as passed
     * - 0 is not allowed as denominator
     * - move a negative sign of the denominator to the numerator
     */
    if (d == 0) {
        // new:
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
    // "x *= y"  ==>  "x = x * y"
    *this = *this * f;

    // Object (first operand) is returned
    return *this;
}

#include "frac6.cpp"

///* operator <
// */
//bool Fraction::operator < (const Fraction& f) const
//{
//    // because the denominator can not be negative, the following is sufficient:
//    return numer * f.denom < f.numer * denom;
//}

/* printOn
 * - output fraction on stream strm
 */
void Fraction::printOn (std::ostream& strm) const
{
    strm << numer << '/' << denom;
}

/* scanFrom
 * - read fraction from Stream strm
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

    // denominator == 0?
    if (d == 0) {
        // set failbit
        strm.clear (strm.rdstate() | std::ios::failbit);
        return;
    }

    /* OK, assign read values
     * - there is a negative sign of the numerator in the denominator
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

} // **** END Namespace CPPBook ********************************

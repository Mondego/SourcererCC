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
#ifndef FRACTION_HPP
#define FRACTION_HPP

// include standard header files
#include <iostream>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

class Fraction {

  protected:
    int numer;
    int denom;

  public:
    /* error class
     */
    class DenomIsZero {
    };

    /* default constructor, one-, and two-parameter constructor
     */
    Fraction(int = 0, int = 1);

    /* multiplication
     * - global friend function, so that an automatic
     *     type conversion of the first operand is possible
     */
    friend Fraction operator * (const Fraction&, const Fraction&);

    /* multiplicative assignment
     * - new: virtual
     */
    virtual const Fraction& operator *= (const Fraction&);

    /* comparison
     * - global friend function, so that an automatic
     *     type conversion of the first operand is possible
     */
    friend bool operator < (const Fraction&, const Fraction&);

    /* output to and input from a stream
     * - new: virtual
     */
    virtual void printOn(std::ostream&) const;
    virtual void scanFrom(std::istream&);

    /* type conversion to double
     * - new: virtual
     */
    virtual double toDouble() const;
};

/* operator *
 * - global friend function
 * - inline defined
 */
inline Fraction operator * (const Fraction& a, const Fraction& b)
{
    /* simply multiply numerator and denominator
     * - this saves time
     */
    return Fraction(a.numer * b.numer, a.denom * b.denom);
}

/* standard output operator
 * - overloaded globally and inline defined
 */
inline
std::ostream& operator << (std::ostream& strm, const Fraction& f)
{
    f.printOn(strm);    // call member function for output
    return strm;        // return stream for chaining
}

/* standard input operator
 * - overloaded globally and inline defined
 */
inline
std::istream& operator >> (std::istream& strm, Fraction& f)
{
    f.scanFrom(strm);   // call member function for input
    return strm;        // return stream for chaining
}

} // **** END namespace CPPBook ********************************

#endif  // FRACTION_HPP

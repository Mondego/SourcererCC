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

  private:
    int numer;
    int denom;

  public:
    /* default constructor, and one- and two-parameter constructor
     */
    Fraction(int = 0, int = 1);
    // multiplication
    Fraction operator * (const Fraction&) const;

    // multiplicative assignment
    const Fraction& operator *= (const Fraction&);

    // comparison
    bool operator < (const Fraction&) const;

    // new: output to a stream
    void printOn(std::ostream&) const;

    // new: input from a stream
    void scanFrom(std::istream&);
};

/* operator *
 * - defined inline
 */
inline Fraction Fraction::operator * (const Fraction& f) const
{
    /* simply multiply numerator and denominator
     * - no reducing yet
     */
    return Fraction (numer * f.numer, denom * f.denom);
}


/* new: standard output operator
 * - overload globally and define inline
 */
inline
std::ostream& operator << (std::ostream& strm, const Fraction& f)
{
    f.printOn(strm);    // call member function for output
    return strm;        // return stream for chaining
}

/* new: standard input operator
 * - overload globally and define inline
 */
inline
std::istream& operator >> (std::istream& strm, Fraction& f)
{
    f.scanFrom(strm);   // call member function for input
    return strm;        // return stream for chaining
}

} // **** END namespace CPPBook ********************************

#endif  // FRACTION_HPP

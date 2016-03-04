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

// **** BEGIN Namespace CPPBook ********************************
namespace CPPBook {

class Fraction {

  private:
    int numer;
    int denom;

  public:
    /* default constructor, constructor from numerator, and
     * constructor from numerator and denominator
     */
    Fraction (int = 0, int = 1);

    /* multiplication
     * - new: global friend function so that an automatic
     * type conversion of the first operands is possible
     */
    friend Fraction operator * (const Fraction&, const Fraction&);
    
    // multiplicative assignment
    const Fraction& operator *= (const Fraction&);
    
    /* comparison
     * - new: global friend function so that an automatic
     * type conversion of the first operands is possible
     */
    friend bool operator < (const Fraction&, const Fraction&);

    // output to a stream
    void printOn (std::ostream&) const;

    // input from a stream
    void scanFrom (std::istream&);

    // new for sqrt():
    operator double () const;
};

// new for sqrt():
inline Fraction::operator double () const
{
    // return quotient from numerator and denominators
    return double(numer)/double(denom);
}

/* operator *
 * - new: global friend function
 * - define inline
 */
inline Fraction operator * (const Fraction& a, const Fraction& b)
{
    /* simply multiply numerator and denominator
     * - this saves time
     */
    return Fraction (a.numer * b.numer, a.denom * b.denom);
}

/* standard output operator
 * - overload globally and define inline
 */
inline
std::ostream& operator << (std::ostream& strm, const Fraction& f)
{
    f.printOn(strm);    // call element function for output
    return strm;        // return stream for chaining
}

/* standard input operator
 * - overload globally and define inline
 */
inline
std::istream& operator >> (std::istream& strm, Fraction& f)
{
    f.scanFrom(strm);   // call member function for input
    return strm;        // return stream for chaining
}

} // **** END Namespace CPPBook ********************************

#endif  // FRACTION_HPP

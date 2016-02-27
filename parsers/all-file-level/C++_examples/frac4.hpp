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

    // output (defined inline)
    void print() const {
        std::cout << numer << '/' << denom << std::endl;
    }

    // multiplication
    Fraction operator * (const Fraction&) const;

    // multiplicative assignment
    const Fraction& operator *= (const Fraction&);

    // comparison
    bool operator < (const Fraction&) const;
};


/* operator *
 * - defined inline
 */
inline Fraction Fraction::operator * (const Fraction& f) const
{
    /* simply multiply numerator and denominator
     * - no reducing yet
     */
    return Fraction(numer * f.numer, denom * f.denom);
}

} // **** END namespace CPPBook ********************************

#endif  // FRACTION_HPP

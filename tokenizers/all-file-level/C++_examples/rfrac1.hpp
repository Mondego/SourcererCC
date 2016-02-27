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
#ifndef RFRACTION_HPP
#define RFRACTION_HPP

// header file of the base class
#include "frac.hpp"

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* class RFraction
 * - derived from Fraction
 * - access to inherited members limited (public remains public)
 */
class RFraction : public Fraction {
  protected:
    bool reducible;        // true: fraction is reducible

    // auxiliary function: returns the GCD of the numerator and denominator
    unsigned gcd() const;

  public:
    /* default constructor, and one- and two-parameter constructor
     * - parameters are passed to the Fraction constructor
     */
    RFraction(int n = 0, int d = 1) : Fraction(n,d) {
        reducible = (gcd() > 1);
    }

    // multiplicative assignement (reimplemented)
    const RFraction& operator*= (const RFraction&);

    // input from a stream (reimplemented)
    void scanFrom(std::istream&);

    // reduce fraction
    void reduce();

    // test reducililty
    bool isReducible() const {
        return reducible;
    }
};

} // **** END namespace CPPBook ********************************

#endif    // RFRACTION_HPP

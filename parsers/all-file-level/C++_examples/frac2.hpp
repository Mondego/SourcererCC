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

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* fraction class
 */
class Fraction {
  /* private: no access from outside
   */
  private:
    int numer;
    int denom;

  /* public interface
   */
  public:
    // default constructor
    Fraction();

    // constructor from int (denominator)
    Fraction(int);

    // constructor from two ints (numerator and denominator)
    Fraction(int, int);

    // output
    void print();

    // new: multiplication with other fraction
    Fraction operator * (Fraction);

    // new: multiplicative assignment
    Fraction operator *= (Fraction);

    // new: comparison with different fraction
    bool operator < (Fraction);
};

} // **** END namespace CPPBook ********************************

#endif    // FRACTION_HPP

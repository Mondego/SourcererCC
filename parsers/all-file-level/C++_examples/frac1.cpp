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
// include header file with the class declaration
#include "frac.hpp"

// include standard header files
#include <iostream>
#include <cstdlib>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* default constructor
 */
Fraction::Fraction()
 : numer(0), denom(1)    // initialize fraction with 0
{
    // no further statements
}

/* constructor for whole number
 */
Fraction::Fraction(int n)
 : numer(n), denom(1)    // initialize fraction with n
{
    // no further statements
}

/* constructor for numerator and denominator
 */
Fraction::Fraction(int n, int d)
 : numer(n), denom(d)    // initialize numerator and denominator as passed
{
    // 0 as denominator is not allowed
    if (d == 0) {
        // exit program with error message
        std::cerr << "error: denominator is 0" << std::endl;
        std::exit(EXIT_FAILURE);
    }
}

/* print
 */
void Fraction::print()
{
    std::cout << numer << '/' << denom << std::endl;
}

} // **** END namespace CPPBook ********************************

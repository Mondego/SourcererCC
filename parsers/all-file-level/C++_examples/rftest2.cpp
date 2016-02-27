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
// header files
#include <iostream>
#include "rfrac.hpp"
#include "frac.hpp"

int main()
{
    // declare RFraction
    CPPBook::RFraction x(7,3);

    // pointer to fraction refers to x
    CPPBook::Fraction* xp = &x;

    // declare fraction with reciprocal value of x
    CPPBook::Fraction f(3,7);

    *xp *= f;         // PROBLEM: calls Fraction::operator*=()

    // output x
    std::cout << x;
    std::cout << (x.isReducible() ? " (reducible)"
                                  : " (non reducible)") << std::endl;

    std::cout << "enter fraction (numer/denom): ";

    std::cin >> x;    // PROBLEM: indirectly calls Fraction::scanFrom()

    // output x
    std::cout << x;
    std::cout << (x.isReducible() ? " (reducible)"
                                  : " (non reducible)") << std::endl;
}

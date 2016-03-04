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
// include standard header files
#include <iostream>
#include <cstdlib>

// include header file for the classes that are being used
#include "frac.hpp"

int main()
{
    const CPPBook::Fraction a(7,3);      // declare fraction constant a
    CPPBook::Fraction x;                 // declare fraction variable x

    std::cout << a << std::endl;         // output fraction a

    // read fraction x
    std::cout << "enter fraction (numer/denom): ";
    if (! (std::cin >> x)) {
        // input error: exit program with error status
        std::cerr << "Error during input of fraction" << std::endl;
        return EXIT_FAILURE;
    }
    std::cout << "Input was: " << x << std::endl;

    // as long as x is less than 1000
    // new: instead of while (x < CPPBook::Fraction(1000))
    while (x < 1000) {
        // multiply x by a and output result
        x = x * a;
        std::cout << x << std::endl;
    }
}

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

// include header files for the classes that are being used
#include "frac.hpp"

int main()
{
    CPPBook::Fraction x;     // fraction variable

    /* try to read the fraction x, and handle
     * exceptions of the type DenomIsZero
     */
    try {
        int n, d;
        std::cout << "numerator: ";
        if (! (std::cin >> n)) {
            // input error: exit program with error status
            std::cerr << "error during input of numerator"
                      << std::endl;
            return EXIT_FAILURE;
        }
        std::cout << "denominator: ";
        if (! (std::cin >> d)) {
            // input error: exit program with error status
            std::cerr << "error during input of denominator"
                      << std::endl;
            return EXIT_FAILURE;
        }
        x = CPPBook::Fraction(n,d);
        std::cout << "input was: " << x << std::endl;
    }
    catch (const CPPBook::Fraction::DenomIsZero&) {
        /* exit program with an appropriate error message
         */
        std::cerr << "input error: numerator can not be zero"
                  << std::endl;
        return EXIT_FAILURE;
    }

    // this point is only reached if x was read successfully
    //...
}

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
// header file for I/O with streams
#include <iostream>

// general header file for EXIT_FAILURE
#include <cstdlib>

int main()
{
    int x, y;

    // output start string
    std::cout << "Integral division (x/y)\n\n";

    // read x
    std::cout << "x: ";
    if (! (std::cin >> x)) {
        /* error when reading
         * => exit program with error message and error status
         */
        std::cerr << "Error when reading an integer"
                  << std::endl;
        return EXIT_FAILURE;
    }

    // read y
    std::cout << "y: ";
    if (! (std::cin >> y)) {
        /* error when reading
         * => exit program with error message and error status
         */
        std::cerr << "Error when reading an integer"
                  << std::endl;
        return EXIT_FAILURE;
    }

    // error if y is zero
    if (y == 0) {
        /* division by zero
         * => exit program with error message and error status
         */
        std::cerr << "Error: division by 0" << std::endl;
        return EXIT_FAILURE;
    }

    // output operands and result
    std::cout << x << " divided by " << y << " gives "
              << x / y << std::endl;
}

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
#include <iostream>
#include "swap.hpp"

int main()
{
    int x = 7;
    int y = 13;

    std::cout << "x: " << x                  // x: 7,  y: 13
              << "  y: " << y << std::endl;

    swap (x, y);                             // swaps values of x and y

    std::cout << "x: " << x                  // x: 13,  y: 7
              << "  y: " << y << std::endl;
}

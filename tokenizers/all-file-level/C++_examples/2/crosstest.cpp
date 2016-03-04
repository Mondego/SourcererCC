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
// include header file with the declaration of crosssum()
#include "cross.hpp"

// include header file for I/O
#include <iostream>

// forward declaration of printCrosssum()
void printCrosssum(long);

// implementation of main()
int main()
{
    printCrosssum(12345678);
    printCrosssum(0);
    printCrosssum(13*77);
}

// implementation of printCrosssum()
void printCrosssum(long number)
{
    std::cout << "the cross sum of " << number
              << " is " << crosssum(number) << std::endl;
}

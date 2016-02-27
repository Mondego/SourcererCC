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
#include "cross.hpp"

// implementation of the function that calculates the cross sum of an integer
int crosssum(long number)
{
    int cross = 0;

    while (number > 0) {
        cross += number % 10;    // add one to the cross sum
        number = number / 10;    // continue with the remaining digits
    }

    return cross;
}

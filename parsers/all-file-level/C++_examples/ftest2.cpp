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
// include header files for the classes that are being used
#include "frac.hpp"

int main()
{
    CPPBook::Fraction x;             // declare fraction x
    CPPBook::Fraction w(7,3);        // declare fraction w

    // output fraction w
    w.print();

    // x assign the square of w
    x = w * w;

    // as long as x is less than 1000
    while (x < CPPBook::Fraction(1000)) {

        // multiply x by a
        x *= w;

        // and output
        x.print();
    }
}


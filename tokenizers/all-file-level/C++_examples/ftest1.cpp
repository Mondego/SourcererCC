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
#include "frac1.hpp"

int main()
{
    CPPBook::Fraction x;        // initialisation using the default constructor
    CPPBook::Fraction w(7,3);   // initialisation using the int/int constructor

    // output fraction w
    w.print();

    // fraction w is assigned to fraction x
    x = w;

    // convert 1000 to a fraction and assign the result to w
    w = CPPBook::Fraction(1000);

    // output x and w
    x.print();
    w.print();
}

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

int main()
{
    // declare reducible fraction
    CPPBook::RFraction x(91,39);

    // output x
    std::cout << x;
    std::cout << (x.isReducible() ? " (reducible)"
                                  : " (non reducible)") << std::endl;

    // reduce x
    x.reduce();

    // output x
    std::cout << x;
    std::cout << (x.isReducible() ? " (reducible)"
                                  : " (non reducible)") << std::endl;

    // multiply x by 3
    x *= 3;

    // output x
    std::cout << x;
    std::cout << (x.isReducible() ? " (reducible)"
                                  : " (non reducible)") << std::endl;
}

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
#include <string>
#include "max1.hpp"

int main()
{
    int         a, b;  // two variables of the datatype int
    std::string s, t;  // two variables of the type std::string
    //...
    std::cout << max(a,b) << std::endl;    // max() for two ints
    std::cout << ::max(s,t) << std::endl;  // max() for two strings
}

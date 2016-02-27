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
#include <iostream>    // C++ header file for I/O
#include "string.hpp"  // C++ header file for strings

int main()
{
    typedef CPPBook::String string;

    // create two strings
    string firstname = "Jicolai";
    string lastname = "Nosuttis";
    string name;

    // mix up the first characters of the string
    char c = firstname[0];
    firstname[0] = lastname[0];
    lastname[0] = c;

    std::cout << firstname << ' ' << lastname << std::endl;
}

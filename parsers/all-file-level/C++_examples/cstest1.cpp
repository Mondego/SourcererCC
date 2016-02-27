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
// header file for I/O
#include <iostream>

// header file for the class ColString
#include "colstring.hpp"

int main()
{
    CPPBook::ColString c("hello");        // ColString with default colour
    CPPBook::ColString r("red","red");    // ColString with colour red

    std::cout << c << " " << r << std::endl; // output ColStrings

    c.color("green");                        // set colour of f to green

    std::cout << c << " " << r << std::endl; // output ColStrings

    std::cout << "concatenated string: " << c + r << std::endl;

    c[0] = 'H';
    std::cout << "modified string:     " << c << std::endl;
}

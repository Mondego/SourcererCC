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
#include <string>      // C++ header file for strings

int main()
{
    const std::string c = "input: ";  // string constant
    std::string text;                 // string variable
    std::string s;                    // string variable for the input

    // read string s
    if (! (std::cin >> s)) {
        // read error
        //...
    }

    // compare string with empty string
    if (s == "") {
        // assign string literal to string text
        text = "no input";
    }
    else {
        /* assign string constant c, followed by read string, to text
         */
        text = c + s;
    }
    //...
}

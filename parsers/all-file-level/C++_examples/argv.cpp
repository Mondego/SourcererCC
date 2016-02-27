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

int main(int argc, char* argv[])
{
    // output program name and number of parameters
    std::string progname = argv[0];
    if (argc > 1) {
        std::cout << progname << " has " << argc-1 << " parameters: "
                  << std::endl;
    }
    else {
        std::cout << progname << " was called without parameters"
                  << std::endl;
    }

    // output program parameters
    for (int i=1; i<argc; ++i) {
        std::cout << "argv[" << i << "]: " << argv[i] << std::endl;
    }
}

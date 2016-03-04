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
#include <cstdlib>
#include "lexcast.hpp"

int main(int argc, char* argv[])
{
    try {
        if (argc > 1) {
            // evaluate first argument as int
            int value = lexical_cast<int>(argv[1]);

            // use int as string
            std::string msg;
            msg = "The passed value is: "
                  + lexical_cast<std::string>(value);
            std::cout << msg << std::endl;
        }
    }
    catch (const char* msg) {
        std::cerr << "Exception: " << msg << std::endl;
        return EXIT_FAILURE;
    }
}

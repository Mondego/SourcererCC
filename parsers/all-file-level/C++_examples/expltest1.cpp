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
#include "expl.hpp"

int main()
{
    try {
        CPPBook::Stack<int>         intStack;       // stack for integers
        CPPBook::Stack<std::string> stringStack;    // stack for strings

        // manipulate integer stack
        intStack.push(7);
        intStack.push(max(intStack.top(),42));  // max() for ints
        std::cout << intStack.top() << std::endl;
        intStack.pop();

        // manipulate string stack
        std::string s = "hello";
        stringStack.push(s);
        stringStack.pop();
        stringStack.pop();
    }
    catch (const char* msg) {
        std::cerr << "Exception: " << msg << std::endl;
        return EXIT_FAILURE;
    }
}

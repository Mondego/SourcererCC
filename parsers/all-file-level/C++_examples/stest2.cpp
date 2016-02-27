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
#include "stack1.hpp"
#include "stack2.hpp"

int main()
{
    try {
        CPPBook::Stack<int>         intStack;       // Stack for integer
        CPPBook::Stack<std::string> stringStack;    // Stack for strings

        // manipulate integer stack
        intStack.push(7);
        std::cout << intStack.top() << std::endl;
        intStack.pop();

        // manipulate string stack
        std::string s = "hello";
        stringStack.push(s);
        std::cout << stringStack.top() << std::endl; 
        stringStack.pop();
        std::cout << stringStack.top() << std::endl;
        stringStack.pop();
    }
    catch (const char* msg) {
        std::cerr << "Exception: " << msg << std::endl;
        return EXIT_FAILURE;
    }
}

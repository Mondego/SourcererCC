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
#include "stack4.hpp"

int main()
{
    try {
        CPPBook::Stack<int,20>         int20Stack;   // stack for 20 ints
        CPPBook::Stack<int,40>         int40Stack;   // stack for 40 ints
        CPPBook::Stack<std::string,40> stringStack;  // stack for 40 strings

        // manipulate integer stack
        int20Stack.push(7);
        std::cout << int20Stack.top() << std::endl;
        int20Stack.pop();

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

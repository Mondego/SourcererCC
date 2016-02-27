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
#include <vector>
#include <cstdlib>
#include "stack6.hpp"

int main()
{
    try {
        CPPBook::Stack<int>         intStack;       // Stack for integer
        CPPBook::Stack<float>       floatStack;     // Stack for floating point value

        // manipulate floating point value stack
        floatStack.push(7.7);

        // manipulate integer stack
        intStack.push(42);
        intStack.push(7);

        floatStack = intStack;
        floatStack.push(3.1415);
        std::cout << floatStack.top() << std::endl;
        floatStack.pop();
        std::cout << floatStack.top() << std::endl;
        floatStack.pop();
        std::cout << floatStack.top() << std::endl;
        floatStack.pop();

        CPPBook::Stack<int,std::vector<int> > vStack;       // Stack for integer
        vStack.push(42);
        vStack.push(7);
        std::cout << vStack.top() << std::endl;
        vStack.pop();
    }
    catch (const char* msg) {
        std::cerr << "Exception: " << msg << std::endl;
        return EXIT_FAILURE;
    }
}

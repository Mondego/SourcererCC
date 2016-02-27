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
#include <deque>
#include <cstdlib>
#include "stack3.hpp"

int main()
{
    try {
        // stack for integers
        CPPBook::Stack<int> intStack;
        // stack for floating-point values
        CPPBook::Stack<double,std::deque<double> > dblStack;

        // manipulate integer stack
        intStack.push(7);
        std::cout << intStack.top() << std::endl;
        intStack.pop();

        // manipulate floating-point value stack
        dblStack.push(42.42);
        std::cout << dblStack.top() << std::endl; 
        dblStack.pop();
        std::cout << dblStack.top() << std::endl;
        dblStack.pop();
    }
    catch (const char* msg) {
        std::cerr << "Exception: " << msg << std::endl;
        return EXIT_FAILURE;
    }
}

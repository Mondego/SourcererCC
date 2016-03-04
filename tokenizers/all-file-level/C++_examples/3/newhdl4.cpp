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
// header file for the new handler
#include <new>

// header filefor I/O
#include <iostream>

// forward declaration
static void myNewHandler();

// reserved memory
static char* reserveMemory1;
static char* reserveMemory2;

void initNewHandler()
{
    // request reserved memory accoring to needs
    reserveMemory1 = new char[1000000];
    reserveMemory2 = new char[100000];

    // install new handler
    std::set_new_handler(&myNewHandler);
}

static void myNewHandler()
{
    static bool firstKiss = true;

    // - first time:  provide reserve memory
    // - second time: throw exception
    if (firstKiss) {
        // program runs until second call
        firstKiss = false;

        // deallocate first reserved memory for new handler
        delete [] reserveMemory1;

        // output warning on standard error channel
        std::cerr << "Warning: almost out of memory" << std::endl;
    }
    else {
        // deallocate second reserved memory for new handler
        delete [] reserveMemory2;

        // output error message on standard error channel
        std::cerr << "out of memory" << std::endl;

        // throw appropriate exception
        throw std::bad_alloc();
    }
}

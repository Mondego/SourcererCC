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

// header file for I/O
#include <iostream>

// forward declaration
static void myNewHandler();

// reserved memory
static char* reserveMemory;

void initNewHandler()
{
    // allocate memory as might be necessary
    reserveMemory = new char[100000];

    // install new handler
    std::set_new_handler(&myNewHandler);
}

static void myNewHandler()
{
    // deallocate reserved memory
    delete [] reserveMemory;

    // output error message on standard error channel
    std::cerr << "out of memory (use emerengy memory)"
              << std::endl;

    // throw appropriate exception
    throw std::bad_alloc();
}

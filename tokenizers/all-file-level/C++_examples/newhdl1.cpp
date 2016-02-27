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

// standard header files
#include <iostream>
#include <cstdlib>

/* myNewHandler()
 * - outputs error message and exits the program
 */
void myNewHandler()
{
    // output error message on standard error channel
    std::cerr << "out of meemmmoooorrrrrryyyyyyy..." << std::endl;

    // throw appropriate exception
    throw std::bad_alloc();
}

int main()
{
    try {
        // install your own new handler
        std::set_new_handler(&myNewHandler);

        // and test with endless loops that requires memory
        for (;;) {
            new char[1000000];
        }

        // no computer can have infinite memory
        std::cout << "Yikes, magic!" << std::endl;
    }
    catch (const std::bad_alloc& e) {
        std::cerr << "Exception: " << e.what() << std::endl;
        return EXIT_FAILURE;
    }
}

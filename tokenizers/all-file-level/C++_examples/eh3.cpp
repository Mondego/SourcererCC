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
#include <iostream>    // header file for I/O
#include <string>      // header file for strings
#include <cstdlib>     // header file for EXIT_FAILURE
#include <exception>   // header file for exceptions

void processException()
{
    try {
        throw;    // rethrow the exception again so that it
                  // can be handled here
    }
    catch (const std::bad_alloc& e) {
        // special exception: no more memory
        std::cerr << "no more memory" << std::endl;
    }
    catch (const std::exception& e) {
        // other standard exception
        std::cerr << "standard exception: " << e.what() << std::endl;
    }
    catch (...) {
        // all other exceptions
        std::cerr << "other exception" << std::endl;
    }
}

int main()
{
    try {
        // create two strings
        std::string firstname("bjarne");    // may trigger std::bad_alloc
        std::string lastname("stroustrup"); // may trigger std::bad_alloc
        std::string name;

        // manipulate strings
        firstname.at(20) = 'B';             // triggers std::out_of_range
        lastname[30] = 'S';                 // ERROR: undefined behaviour

        // chain strings
        name = firstname + " " + lastname;  // may trigger std::bad_alloc
    }
    catch (...) {
        // deal with all exceptions in auxiliary function
        processException();
        return EXIT_FAILURE;    // exit main() with error status
    }

    std::cout << "OK, everything was alright up until now"
              << std::endl;
}

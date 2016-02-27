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
#ifndef BOAT_HPP
#define BOAT_HPP

// include header file for I/O
#include <iostream>

namespace CPPBook {

/* Boat class
 * - suitable for inheritance
 */
class Boat {
  protected:
    int sm;                      // sea miles traveled

  public:
    // default constructor, and one-parameter constructor
    Boat(int d = 0) : sm(d) {    // initialize distance traveled
    }

    // travel a certain distance
    virtual void travel(int d) {
        sm += d;                 // add additional sea miles
    }

    // output distance traveled
    virtual void printTraveled() {
        std::cout << "The boat has traveled "
                  << sm << " sm " << std::endl;
    }

    virtual ~Boat() {            // virtal destructor
    }
};

}  // namespace CPPBook

#endif    // BOAT_HPP

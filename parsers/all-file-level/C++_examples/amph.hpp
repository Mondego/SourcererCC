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
#ifndef AMPH_HPP
#define AMPH_HPP

// include header files of the base classes
#include "car.hpp"
#include "boat.hpp"

namespace CPPBook {

/* Amph class
 * - derived from Car and Boat
 * - suitable for further derivation
 */
class Amph : public Car, public Boat {
  public:
    /* default constructor, and one- and two-parameter constructor
     * - Car constructor is called with first parameter
     * - Boat constructor is called with second parameter
     */
    Amph(int k = 0, int s = 0) : Car(k), Boat(s) {
        // thus there is nothing more to do
    }

    // output distance traveled
    virtual void printTraveled() {
        std::cout << "The amphibious vehicle has traveled "
                  << km << " km and " << sm << " sm " << std::endl;
    }

    // virtual destructor
    virtual ~Amph() {
    }
};

}  // namespace CPPBook

#endif    // AMPH_HPP

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
#ifndef CAR_HPP
#define CAR_HPP

// include header file for I/O
#include <iostream>

namespace CPPBook {

/* Car class
 * - suitable for inheritance
 */
class Car {
  protected:
    int km;                      // kilometers traveled

  public:
    // default constructor, and one-parameter constructor
    Car(int d = 0) : km(d) {     // initialize distance traveled
    }

    // travel a certain distance
    virtual void travel(int d) {
        km += d;                 // add additional kilometers
    }

    // output distance traveled
    virtual void printTraveled() {
        std::cout << "The car has traveled "
                  << km << " km " << std::endl;
    }

    // virtual destructor
    virtual ~Car() {
    }
};

}  // namespace CPPBook

#endif    // CAR_HPP

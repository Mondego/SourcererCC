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
#ifndef PERSON_HPP
#define PERSON_HPP

// header files for auxiliary classes
#include <string>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

class Person {
  private:
    const std::string lname;      // last name of the Person (new: constant)
    std::string       fname;      // first name of the Person

  public:
    // constructor for last name and optional first name
    Person(const std::string& ln, const std::string& fn = "")
      : lname(ln), fname(fn) {
    }

    // query of properties
    const std::string& lastname() const {      // return last name
        return lname;
    }
    const std::string& firstname() const {     // return last name
        return fname;
    }
    //...
};

} // **** END namespace CPPBook ********************************

#endif  // PERSON_HPP

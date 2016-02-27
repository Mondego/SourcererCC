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

// include header files
#include <string>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

class Person {
  /* new: static class members
   */
  private:
    static long maxPID;      // highest ID of all Persons
    static long numPersons;  // current number of all Persons
  public:
    // return current number of all Persons
    static long number() {
        return numPersons;
    }

  // non-static class members
  private:
    std::string lname;        // last name of the Person
    std::string fname;        // first name of the Person
    const long  pid;          // new: ID of the Person

  public:
    // constructor from last name and optional first name
    Person(const std::string&, const std::string& = "");

    // new: copy constructor
    Person(const Person&);

    // new: destructor
    ~Person();

    // new: assignment
    Person& operator = (const Person&);

    // query of properties
    const std::string& lastname() const {   // return last name
        return lname;
    }
    const std::string& firstname() const {  // return first name
        return fname;
    }
    long id() const {                       // new: return ID
        return pid;
    }

    friend bool operator == (const Person& p1, const Person& p2) {
        return p1.fname == p1.fname && p2.lname == p2.lname;
    }
    friend bool operator != (const Person& p1, const Person& p2) {
        return !(p1==p2);
    }
    //...
};

} // **** END namespace CPPBook ********************************

#endif  // PERSON_HPP

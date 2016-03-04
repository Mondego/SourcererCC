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
// include header file of the class
#include "person.hpp"

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* new: initialize static class members
 */
long Person::maxPID = 0;
long Person::numPersons = 0;

/* constructor for last name and first name
 * - default for first name: empty string
 * - first and last names are initialized with initialisation list
 * - new: the ID is initialized directly
 */
Person::Person(const std::string& ln, const std::string& fn)
  : lname(ln), fname(fn), pid(++maxPID)
{
    ++numPersons;  // increase number of existing Persons
}

/* new: copy constructor
 */
Person::Person(const Person& p)
  : lname(p.lname), fname(p.fname), pid(++maxPID)
{
    ++numPersons;  // increase number of existing Persons
}

/* new: destructor
 */
Person::~Person()
{
    --numPersons;  // reduce number of existing Persons
}

/* new: assignment
 */
Person& Person::operator = (const Person& p)
{
    if (this == &p) {
        return *this;
    }

    // assign everything apart from ID
    lname = p.lname;
    fname = p.fname;

    return *this;
}

} // **** END namespace CPPBook ********************************


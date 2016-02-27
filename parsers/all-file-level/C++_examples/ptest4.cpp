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
#include <iostream>
#include "person.hpp"

int main()
{
    CPPBook::Person nico("Josuttis","Nicolai");

    /* declare variable of the type Salutation of the class CPPBook::Person
     * and initialize it with the value empty of the class CPPBook::Person
     */
    CPPBook::Person::Salutation noSalutation = CPPBook::Person::empty;
    //...
    if (nico.salutation() == noSalutation) {
        std::cout << "salutation of Nico was not set" << std::endl;
    }
}

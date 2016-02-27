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
#include "vehiclehier.hpp"
#include <iostream>

int main()
{
    using std::cout;
    using std::endl;

    CPPBook::Amph a;

    // address of a
    cout << "&a: " << (void*)&a << "\n" << endl;

    // adress of a => as Car and as Boat
    cout << "(CPPBook::Car*) &a: "
         << (void*)(CPPBook::Car*)&a << "\n";
    cout << "(CPPBook::Boat*) &a: "
         << (void*)(CPPBook::Boat*)&a << "\n\n";

    // address of a => as Car => as Vehicle
    cout << "(CPPBook::Vehicle*) (CPPBook::Car*) &a: "
         << (void*)(CPPBook::Vehicle*)(CPPBook::Car*)&a << endl;

    // address of a => as Boat => as Vehicle
    cout << "(CPPBook::Vehicle*) (CPPBook::Boat*) &a: "
         << (void*)(CPPBook::Vehicle*)(CPPBook::Boat*)&a << endl;
}

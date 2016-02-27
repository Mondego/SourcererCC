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

void fVehicle(const CPPBook::Vehicle& a)
{
    std::cout << "    as vehicle: "
              << static_cast<const void*>(&a) << std::endl;
}

void fCar(const CPPBook::Car& a)
{
    std::cout << "&a  as car: "
              << static_cast<const void*>(&a) << std::endl;
    fVehicle(a);
}

void fBoat(const CPPBook::Boat& a)
{
    std::cout << "&a  as boat: "
              << static_cast<const void*>(&a) << std::endl;
    fVehicle(a);
}

int main()
{
    CPPBook::Amph a;

    fCar(a);
    fBoat(a);
}

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
#include <cstddef>

void* MyClass::operator new (std::size_t size)
{
    // output message
    std::cout << "call MyClass::new" << std::endl;

    // call global new for memory of the size size
    return  ::new char[size];
}

void* MyClass::operator new[] (std::size_t size)
{
    // output message
    std::cout << "call MyClass::new[]" << std::endl;

    // call global new for memory of the size size
    return  ::new char[size];
}

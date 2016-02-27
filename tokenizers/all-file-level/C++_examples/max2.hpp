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
#include <cstring>

// maximum of two values of any type
template <typename T>
inline const T& max (const T& a, const T& b)
{
    std::cout << "max<>() for T" << std::endl;
    return  a < b  ?  b : a;
}

// maximum of two pointers
template <typename T>
inline T* const& max (T* const& a, T* const& b)
{
    std::cout << "max<>() for T*" << std::endl;
    return  *a < *b  ?  b : a;
}

// maximum of two C-strings
inline const char* const& max (const char* const& a,
                               const char* const& b)
{ 
    std::cout << "max<>() for char*" << std::endl;
    return  std::strcmp(a,b) < 0  ?  b : a;
}

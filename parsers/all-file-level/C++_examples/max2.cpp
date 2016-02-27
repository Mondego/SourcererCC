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
#include <string>
#include "max2.hpp"

int main()
{
    int a=7;                               // two variables of datatype int
    int b=11;
    std::cout << max(a,b) << std::endl;    // max() for two ints

    std::string s="hello";                 // two strings
    std::string t="holla";
    std::cout << ::max(s,t) << std::endl;  // max() for two strings

    int* p1 = &b;                          // two pointers
    int* p2 = &a;
    std::cout << *max(p1,p2) << std::endl; // max() for two pointers

    const char* s1 = "hello";              // two C-strings
    const char* s2 = "otto";
    std::cout << max(s1,s2) << std::endl;  // max() for two C-strings
}

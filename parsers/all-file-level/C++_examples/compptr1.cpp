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

class MyClass {
  public:
    void func1() {
        std::cout << "call of func1()" << std::endl;
    }
    void func2() {
        std::cout << "call of func2()" << std::endl;
    }
};

// type: pointer to member function of class MyClass
//     without parameter and return value
typedef void (MyClass::*MyClassFunction) ();

int main()
{
    // pointer to member function of class MyClass
    MyClassFunction funcPtr;

    // object of class MyClass
    MyClass x;

    // pointer to member refers to func1()
    funcPtr = & MyClass::func1;

    // call member function, to which the pointer refers, for object x
    (x.*funcPtr)();

    // pointer to member refers to func2()
    funcPtr = & MyClass::func2;

    // call member function, to which the pointer refers for object x
    (x.*funcPtr)();
}

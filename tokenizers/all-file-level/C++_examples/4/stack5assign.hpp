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
template <typename T>
 template <typename T2>
Stack<T>& Stack<T>::operator= (const Stack<T2>& op2)
{
    if ((void*)this == (void*)&op2) {    // assignment to itself?
        return *this;
    }

    Stack<T2> tmp(op2);              // create a copy of the assigned stack

    elems.clear();                   // remove existing elements
    while (!tmp.empty()) {           // copy all elements
        elems.push_front(tmp.top());
        tmp.pop();
    }
    return *this;
}

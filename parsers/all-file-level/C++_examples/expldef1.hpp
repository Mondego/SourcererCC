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
#ifndef EXPLDEF_HPP
#define EXPLDEF_HPP

#include "expl.hpp"
#include <stdexcept>

// definition of the function template max()
template <typename T>
const T& max(const T& a, const T& b)
{
    return (a > b ? a : b);
}

// definition of the functions of the class template Stack<>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

// constructor
template <typename T>
Stack<T>::Stack()
{
    // nothing more to do
}

template <typename T>
void Stack<T>::push(const T& elem)
{
    elems.push_back(elem);    // store copy as new top element
}

template<typename T>
void Stack<T>::pop()
{
    if (elems.empty()) {
        throw std::out_of_range("Stack<>::pop(): empty stack");
    }
    elems.pop_back();         // remove top element
}

template <typename T>
T Stack<T>::top() const
{
    if (elems.empty()) {
        throw std::out_of_range("Stack<>::top(): empty stack");
    }
    return elems.back();      // return top element as copy
}

} // **** END namespace CPPBook ********************************

#endif // EXPLDEF_HPP

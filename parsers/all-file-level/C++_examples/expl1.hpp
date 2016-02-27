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
#ifndef EXPL_HPP
#define EXPL_HPP

// declaration of the function template max()
template <typename T>
const T& max(const T& a, const T& b);

// declaration of the class template Stack<>
#include <vector>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

template <typename T>
class Stack {
  private:
    std::vector<T> elems;  // elements
  public:
    Stack();               // constructor
    void push(const T&);   // store new top element
    void pop();            // remove top element
    T top() const;         // return top element
};

} // **** END namespace CPPBook ********************************

#endif // EXPL_HPP

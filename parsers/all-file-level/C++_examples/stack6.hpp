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
#include <deque>
#include <algorithm>
#include <stdexcept>

namespace CPPBook {  // ******** Begin namespace CPPBook::

template <typename T, typename CONT = std::deque<T> >
class Stack {
  private:
    CONT elems;  // elements

  public:
    Stack();               // constructor
    void push(const T&);   // store new top element
    void pop();            // remove top element
    T top() const;         // return top element
    bool empty() const {   // return whether the stack is empty
        return elems.empty();
    }

    // assign stack for elements of the type T2
    template <typename T2, typename CONT2>
    Stack<T,CONT>& operator= (const Stack<T2,CONT2>&);
};

// constructor
template <typename T, typename CONT>
Stack<T,CONT>::Stack ()
{
    // nothing more to do
}

template <typename T, typename CONT>
void Stack<T,CONT>::push (const T& elem)
{
    elems.push_back(elem);    // store new top element
}

template<typename T, typename CONT>
void Stack<T,CONT>::pop ()
{
    if (elems.empty()) {
        throw std::out_of_range("Stack<>::pop(): empty stack");
    }
    elems.pop_back();         // remove top element
}

template <typename T, typename CONT>
T Stack<T,CONT>::top () const
{
    if (elems.empty()) {
        throw std::out_of_range("Stack<>::top(): empty stack");
    }
    return elems.back();      // return top element as copy
}

template <typename T, typename CONT>
 template <typename T2, typename CONT2>
Stack<T,CONT>& Stack<T,CONT>::operator= (const Stack<T2,CONT2>& op2)
{
    if ((void*)this == (void*)&op2) {    // assignment to itself?
        return *this;
    }

    Stack<T2,CONT2> tmp(op2);        // create a copy of the assigned stack

    elems.clear();                   // remove existing elements
    while (!tmp.empty()) {           // copy all elements
        elems.push_front(tmp.top());
        tmp.pop();
    }
    return *this;
}

}  // ******** End namespace CPPBook::

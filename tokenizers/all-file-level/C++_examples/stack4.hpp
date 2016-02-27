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
#include <stdexcept>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

template <typename T, int MAXSIZE>
class Stack {
  private:
    T elems[MAXSIZE];     // elements
    int numElems;         // current number of elements entered

  public:
    Stack();              // constructor
    void push(const T&);  // store new top element
    void pop();           // remove top element
    T top() const;        // return top element
};

// constructor
template <typename T, int MAXSIZE>
Stack<T,MAXSIZE>::Stack()
  : numElems(0)    // no elements
{
    // nothing more to do
}

template <typename T, int MAXSIZE>
void Stack<T,MAXSIZE>::push(const T& elem)
{
    if (numElems == MAXSIZE) {
        throw std::out_of_range("Stack<>::push(): stack is full");
    }
    elems[numElems] = elem;   // enter element
    ++numElems;               // increase number of elements
}

template<typename T, int MAXSIZE>
void Stack<T,MAXSIZE>::pop()
{
    if (numElems <= 0) {
        throw std::out_of_range("Stack<>::pop(): empty stack");
    }
    --numElems;               // reduce number of elements
}

template <typename T, int MAXSIZE>
T Stack<T,MAXSIZE>::top() const
{
    if (numElems <= 0) {
        throw std::out_of_range("Stack<>::top(): empty stack");
    }
    return elems[numElems-1];  // return top element
}

} // **** END namespace CPPBook ********************************

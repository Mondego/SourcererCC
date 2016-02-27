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
#include <string>
#include <stdexcept>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

template<>
class Stack<std::string> {
  private:
    std::deque<std::string> elems;  // elements

  public:
    Stack() {                       // constructor
    }
    void push(const std::string&);  // store new top element
    void pop();                     // remove top element
    std::string top() const;        // return top element
};

void Stack<std::string>::push(const std::string& elem)
{
    elems.push_back(elem);          // remove top element
}

void Stack<std::string>::pop()
{
    if (elems.empty()) {
        throw std::out_of_range
                    ("Stack<std::string>::pop(): empty stack");
    }
    elems.pop_back();               // remove top element
}

std::string Stack<std::string>::top() const
{
    if (elems.empty()) {
        throw std::out_of_range
                    ("Stack<std::string>::top(): empty stack");
    }
    return elems.back();            // return top element as copy
}

} // **** END namespace CPPBook ********************************

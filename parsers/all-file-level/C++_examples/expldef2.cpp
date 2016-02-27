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
#include <string>
#include "expldef.hpp"

// explicitly instantiate function template
template const int& max(const int&, const int&);

// explicitly instantiate necessary functions of Stack<> for int
template CPPBook::Stack<int>::Stack();
template void CPPBook::Stack<int>::push(const int&);
template int CPPBook::Stack<int>::top() const;
template void CPPBook::Stack<int>::pop();

// explicitly instantiate necessary functions of Stack<> for std::string
// - top() is not required
template CPPBook::Stack<std::string>::Stack();
template void CPPBook::Stack<std::string>::push(const std::string&);
template void CPPBook::Stack<std::string>::pop();

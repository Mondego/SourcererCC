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
#include <sstream>

int main()
{
    // create string that will be read
    std::string s = "Pi: 3.1415";

    // create string stream for formatted reading
    // and initialize it with the string
    std::istringstream is(s);

    // read first string and value
    std::string name;
    double value;
    is >> name >> value;

    // output read data
    std::cout << "Name: " << name << std::endl;
    std::cout << "Value: " << value << std::endl;
}

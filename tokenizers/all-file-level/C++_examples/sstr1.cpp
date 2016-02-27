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
    // create string stream for formatted writing
    std::ostringstream os;

    // write integral value in decimal and hexadecimal forms
    os << "dec: " << 15 << std::hex << " hex: " << 15 << std::endl;

    // output string stream as string
    std::cout << os.str() << std::endl;

    // append floating-point number
    os << "float: " << 4.67 << std::endl;

    // output string stream as string
    std::cout << os.str() << std::endl;

    // overwrite the beginning of the string stream with octal value
    os.seekp(0);
    os << "oct: " << std::oct << 15;

    // output string stream as string
    std::cout << os.str() << std::endl;
}

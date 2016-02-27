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
#include <locale>

int main()
{
    // use the classic language environment in order to
    // read from the standard input
    std::cin.imbue(std::locale::classic());

    // use a German language environment in order to write data
    std::cout.imbue(std::locale("de_DE"));

    // read and output floating-point numbers in a loop
    double value;
    while (std::cin >> value) {
        std::cout << value << std::endl;
    }
}

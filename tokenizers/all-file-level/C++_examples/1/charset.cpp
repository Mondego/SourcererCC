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
/* output character set
 */

#include <iostream>    // declarations for I/O

int main()
{
    // for every character c with a value of 32 to 126
    for (unsigned char c=32; c<127; ++c) {
        // output value as number and as character:
        std::cout << "Value: " << static_cast<int>(c)
                  << " Character: " << c
                  << std::endl;
    }
}

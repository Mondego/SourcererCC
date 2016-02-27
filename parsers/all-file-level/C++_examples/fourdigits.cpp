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
#include <iostream>     // C++ header file for I/O

int main()
{
    int counter = 0;    // current number of found four-digit numbers

    // for every number from 1000 to 9999
    for (int number=1000; number<10000; ++number) {

        // separate the first and last two digits
        int front = number/100;    // the first two digits
        int back  = number%100;    // the last two digits

        // if the sum of the squares produce the original number,
        // output number and increment counter
        if (front*front + back*back == number) {
            std::cout << number << " == "
                      << front << "*" << front << " + "
                      << back << "*" << back << std::endl;
            ++counter;
        }
    }

    // output number of four-digit numbers found
    std::cout << counter << " numbers found" << std::endl;
}

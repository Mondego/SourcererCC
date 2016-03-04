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
// header file for the class Amph
#include "amph.hpp"

int main()
{
    /* create amphibious vehicle and initialize
     * with 7 kilometers and 42 sea miles
     */
    CPPBook::Amph a(7,42);

    // output distance traveled
    a.printTraveled();
}

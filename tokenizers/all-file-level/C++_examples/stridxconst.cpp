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
/* operator [] for constants
 */
char String::operator [] (unsigned idx) const
{
    // index not in permitted range?
    if (idx >= len) {
        throw std::out_of_range("string index out of range");
    }

    return buffer[idx];
}

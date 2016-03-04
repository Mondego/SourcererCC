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
/* operator [] for variables
 */
String::reference String::operator [] (unsigned i)
{
    // index not in permitted range?
    if (i >= len) {
        // throw exception with invalid index
        throw RangeError(i);
    }

    return reference(buffer[i]);
}

/* operator [] for constants
 */
char String::operator [] (unsigned i) const
{
    // index not in permitted range?
    if (i >= len) {
        // throw exception with invalid index
        throw RangeError(i);
    }

    return buffer[i];
}

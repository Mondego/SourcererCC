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
#include <strstream>

int main()
{
    // create dynamic char* stream for writing
    std::ostrstream buffer;

    // write formatted string and append end-of-string character
    buffer << "Pi: " << 3.1415 << std::ends;

    /* output character string
     * - str() freezes the char* stream
     */
    std::cout << buffer.str() << std::endl;

    // cancel the freezing
    buffer.freeze(false);

    // position so that std::ends is overwritten
    buffer.seekp(-1,std::ios::end);

    // write into char* stream further
    buffer << " or also: " << std::scientific << 3.1415
           << std::ends;

    /* output character string
     * - str() freezes the char* stream
     */
    std::cout << buffer.str() << std::endl;

    // cancel the freezing, so that memory is freed by char* stream destructor
    buffer.freeze(false);
}

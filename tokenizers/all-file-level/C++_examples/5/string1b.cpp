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
// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

void String::scanFrom(std::istream& strm)
{
    char c;

    len = 0;            // initially, the string is empty

    strm >> std::ws;    // skip leading whitespace

    /* as long as the input stream strm, after the read
     * of a character c, is fine
     */
    while (strm.get(c)) {       // >> would skip whitespace

        /* if there is a whitespace at the end of the string input,
         * RETURN
         */
        if (std::isspace(c)) {
            return;
        }

        /* if there is not enough memory, enlarge it
         */
        if (len >= size) {
            char* tmp = buffer;           // pointer to old memory
            size = size*2 + 32;           // increase size of memory
            buffer = new char[size];      // allocate new memory
            std::memcpy(buffer,tmp,len);  // copy characters
            delete [] tmp;                // release old memory
        }

        // enter new characters
        buffer[len] = c;
        ++len;
    }

    // end of read because of error or EOF
}

} // **** END namespace CPPBook ********************************

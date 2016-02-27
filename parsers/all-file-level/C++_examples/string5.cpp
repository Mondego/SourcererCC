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
// header file of the separate class
#include "string.hpp"

// C header files for string functions
#include <cstring>
#include <cctype>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* constructor from C-string (const char*)
 * - default for s: empty string
 */
String::String(const char* s)
{
    len = std::strlen(s);       // number of characters
    size = len;                 // number of characters determines size of memory
    buffer = new char[size];    // allocate memory
    std::memcpy(buffer,s,len);  // copy characters into memory
}

/* copy constructor
 */
String::String(const String& s)
{
    len = s.len;                       // copy number of characters
    size = len;                        // number of characters
                                       // determines size of memory
    buffer = new char[size];           // allocate memory
    std::memcpy(buffer,s.buffer,len);  // copy characters
}

/* destructor
 */
String::~String()
{
    // release memory allocated with new[]
    delete [] buffer;
}

/* operator =
 * - assignment
 */
String& String::operator= (const String& s)
{
    // assignment of a string to itself has no effect
    if (this == &s) {
        return *this;        // return string
    }

    len = s.len;             // copy number of characters

    // if there is not enough space, enlarge
    if (size < len) {
        delete [] buffer;         // release old memory
        size = len;               // number of characters determines new size
        buffer = new char[size];  // allocate memory
    }

    std::memcpy(buffer,s.buffer,len);  // copy characters

    return *this;            // return modified string
}

/* operator ==
 * - compares two strings
 * - global friend function, so that an automatic
 *     type conversion of the first operand is possible
 */
bool operator== (const String& s1, const String& s2)
{
    return s1.len == s2.len &&
           std::memcmp(s1.buffer,s2.buffer,s1.len) == 0;
}

/* operator +
 * - appends two strings
 * - global friend function, so that an automatic
 *     type conversion of the first operand is possible
 */
String operator+ (const String& s1, const String& s2)
{
    // allocate buffer for the concatenated string
    char* buffer = new char[s1.len+s2.len];

    // copy characters into the buffer
    std::memcpy (buffer,        s1.buffer, s1.len);
    std::memcpy (buffer+s1.len, s2.buffer, s2.len);

    // create concatenated string from these data and return it
    return String(s1.len+s2.len, buffer);
}

/* constructor for uninitialized string of a certain length
 * - internally for operator +
 */
String::String(unsigned l, char* buf)
{
    len = l;       // copy number of characters
    size = len;    // number of characters determines size of memory
    buffer = buf;  // copy memory
}

/* output to stream
 */
void String::printOn(std::ostream& strm) const
{
    // simply output character string
    strm.write(buffer,len);
}

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

/* operator [] for variables
 */
String::reference String::operator [] (unsigned i)
{
    // index not in permitted range?
    if (i >= len) {
        /* throw exception:
         * - new: pass string itself and invalid index
         */
        throw RangeError(*this,i);
    }

    return reference(buffer[i]);
}

/* operator [] for constants
 */
char String::operator [] (unsigned i) const
{
    // index not in permitted range?
    if (i >= len) {
        /* throw exception:
         * - new: pass string itself and invalid index
         */
        throw RangeError(*this,i);
    }

    return buffer[i];
}

} // **** END namespace CPPBook ********************************

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
#ifndef STRING_HPP
#define STRING_HPP

// header file for I/O
#include <iostream>

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

class String {

  private:
    char*    buffer;    // character sequence as dynamic array
    unsigned len;       // current number of characters
    unsigned size;      // size of buffer

  public:
    // default and C-string constructor
    String(const char* = "");

    // due to dynamic members:
    String(const String&);              // copy constructor
    String& operator= (const String&);  // assignment
    ~String();                          // destructor

    // comparison of strings
    friend bool operator== (const String&, const String&);
    friend bool operator!= (const String&, const String&);

    // concatenating strings
    friend String operator+ (const String&, const String&);

    // output to a stream
    void printOn(std::ostream&) const;

    // input from a stream
    void scanFrom(std::istream&);

    // number of characters
    unsigned length() const {
        return len;
    }

  private:
    /* constructor from length and buffer
     * - internal for operator +
     */
    String(unsigned, char*);
};

// standard output operator
inline std::ostream& operator << (std::ostream& strm, const String& s)
{
    s.printOn(strm);    // output string to stream
    return strm;        // return stream
}

// standard input operator
inline std::istream& operator >> (std::istream& strm, String& s)
{
    s.scanFrom(strm);   // read string from stream
    return strm;        // return stream
}

/* operator !=
 * - implemented as inline conversion to operator ==
 */
inline bool operator!= (const String& s1, const String& s2)
{
    return !(s1==s2);
}

} // **** END namespace CPPBook ********************************

#endif  // STRING_HPP

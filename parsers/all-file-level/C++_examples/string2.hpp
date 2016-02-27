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

// **** BEGIN Namespace CPPBook ********************************
namespace CPPBook {

class String {
  public:
    class reference {
      friend class String;     // String has access to private members
      private:
        char& ch;              // internal reference to a character in the string

        // constructor (can only be called from String class)
        reference(char& c) : ch(c) {    // create reference
        }

        reference(const reference&);     // copying forbidden
      public:

        // assignments of char and other reference are OK
        reference& operator= (char c) {
            ch = c;
            return *this;
        }
        reference& operator= (const reference& r) {
            ch = r.ch;
            return *this;
        }

        // use as char creates a copy
        operator char() {
            return ch;
        }
    };

  private:
    char*    buffer;    // character sequence as a dynamic array
    unsigned len;       // current number of characters
    unsigned size;      // size of buffer

  public:
    // default and C-string constructor
    String (const char* = "");

    // due to dynamic members:
    String (const String&);             // copy constructor
    String& operator= (const String&);  // assignment
    ~String();                          // destructor

    // comparison of strings
    friend bool operator== (const String&, const String&);
    friend bool operator!= (const String&, const String&);

    // concatenating strings
    friend String operator+ (const String&, const String&);

    // output to a stream
    void printOn (std::ostream&) const;

    // input from a stream
    void scanFrom (std::istream&);

    // number of characters
    unsigned length () const {
        return len;
    }

    // access to a character in the string
    reference operator [] (unsigned);
    char      operator [] (unsigned) const;

  private:
    /* constructor from length and buffer
     * - internal for operator +
     */
    String (unsigned, char*);
};

// standard output operator
inline std::ostream& operator << (std::ostream& strm, const String& s)
{
    s.printOn(strm);    // output string on stream
    return strm;        // return stream
}

// standard input eoperator
inline std::istream& operator >> (std::istream& strm, String& s)
{
    s.scanFrom(strm);   // read string from stream
    return strm;        // return stream
}

/* operator !=
 * - implemented inline as conversion to operator ==
 */
inline bool operator!= (const String& s1, const String& s2) 
{
    return !(s1==s2);
}

} // **** END Namespace CPPBook ********************************

#endif  // STRING_HPP

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
  public:
    class reference {
      friend class String;  // String has access to private members
      private:
        char& ch;           // internal reference to a character in the string
        reference(char& c) : ch(c) {     // constructor
        }
        reference(const reference&);     // copying forbidden
      public:
        reference& operator= (char c) {  // assignments
            ch = c;
            return *this;
        }
        reference& operator= (const reference& r) {
            ch = r.ch;
            return *this;
        }
        operator char() {                // use as char creates a copy
            return ch;
        }
    };

    // error class:
    // - forward declared because it contains a String
    class RangeError;

  protected:
    char*    buffer;    // character string as dynamic array
    unsigned len;       // current number of characters
    unsigned size;      // size of memory of buffer

  public:
    // default and char* constructor
    String(const char* = "");

    // due to dynamic members:
    String(const String&);              // copy constructor
    String& operator= (const String&);  // assignment
    virtual ~String();                  // destructor (new: virtual)

    // comparison of strings
    friend bool operator== (const String&, const String&);
    friend bool operator!= (const String&, const String&);

    // appending strings one after the other
    friend String operator+ (const String&, const String&);

    // output to a stream
    virtual void printOn(std::ostream&) const;

    // input from a stream
    virtual void scanFrom(std::istream&);

    // number of characters
    // note: cannot be overlooked during derivation
    unsigned length() const {
        return len;
    }

    // operator [] for variables and constants
    reference operator [] (unsigned);
    char      operator [] (unsigned) const;

  private:
    /* constructor from length and buffer
     * - internally for operator +
     */
    String(unsigned, char*);
};

class String::RangeError {
  public:
    int    index;    // invalid index
    String value;    // string for this purpose

    // constructor (initializes index and value)
    RangeError (const String& s, int i) : index(i), value(s) {
    }
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
 * - implemented inline as conversion to operator ==
 */
inline bool operator!= (const String& s1, const String& s2) {
    return !(s1==s2);
}

} // **** END namespace CPPBook ********************************

#endif  // STRING_HPP

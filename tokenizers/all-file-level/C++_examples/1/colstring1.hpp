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
#ifndef COLSTRING_HPP
#define COLSTRING_HPP

// header file of the base class
#include "string.hpp"

// **** BEGIN namespace CPPBook ********************************
namespace CPPBook {

/* class ColString
 * - derived from String
 */
class ColString : public String {
  protected:
    String col;    // colour of the string

  public:
    // default, String and String/String constructor
    ColString(const String& s = "", const String& c = "black")
        : String(s), col(c) {
    }

    // query and set colour
    const String& color() {
        return col;
    }
    void color(const String& newColor) {
        col = newColor;
    }

    // output to and input from a stream
    virtual void printOn(std::ostream&) const;
    virtual void scanFrom(std::istream&);

    // comparison of ColStrings
    friend bool operator== (const ColString& s1,
                            const ColString& s2) {
        return static_cast<const String&>(s1)
                 == static_cast<const String&>(s2)
               && s1.col == s2.col;
    }
    friend bool operator!= (const ColString& s1,
                            const ColString& s2) {
        return !(s1==s2);
    }
};

} // **** END namespace CPPBook ********************************

#endif // COLSTRING_HPP

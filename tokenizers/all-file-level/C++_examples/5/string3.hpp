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
namespace CPPBook {
  class String {
    public:
      class reference {
          //...
      };

      // new: error class
      class RangeError {
        public:
          int index;                      // invalid index
          RangeError(int i) : index(i) {  // constructor (initializes index)
          }
      };

    public:
      //...
      // operator [] for variables and constants
      reference operator [] (unsigned);
      char      operator [] (unsigned) const;
  };
}

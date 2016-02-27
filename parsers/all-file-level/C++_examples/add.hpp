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
// function object that adds the passed value
class Add {
  private:
    int val;    // value to add
  public:
    // constructor (initializes the value to add)
    Add(int w) : val(w) {
    }

    // `function call' (adds the value)
    int operator() (int elem) const
    {
        return elem + val;
    }
};

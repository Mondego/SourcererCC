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
class Fraction {
  private:
    int numer;
    int denom;

  public:
    /* error classes:
     * - new: common-case class with two derived classes
     */
    class FractionError {
    };
    class DenomIsZero: public FractionError {
    };
    class ReadError : public FractionError {
    };

    /* default constructor, and one- and two-parameter constructor
     */
    Fraction(int = 0, int = 1);

    /* output to and input from a stream
     */
    void printOn(std::ostream&) const;
    void scanFrom(std::istream&);
    //...
};

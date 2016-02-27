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
//...

/* new: scanFrom()
 * - read fraction from stream strm
 */
void Fraction::scanFrom(std::istream& strm)
{
    int n, d;

    // read numerator
    strm >> n;

    // read optional separator '/' and denominator
    if (strm.peek() == '/') {
        strm.get();
        strm >> d;
    }
    else {
        d = 1;
    }

    // read error?
    if (! strm) {
        return;
    }

    // denominator equals zero?
    if (d == 0) {
        // set failbit
        strm.clear (strm.rdstate() | std::ios::failbit);
        return;
    }

    /* OK, assign read values
     * - move negative sign of the denominator to the numerator
     */
    if (d < 0) {
        numer = -n;
        denom = -d;
    }
    else {
        numer = n;
        denom = d;
    }
}

} // **** END namespace CPPBook ********************************

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
CPPBook::Fraction readFraction()
{
    CPPBook::Fraction x;        // fraction variable
    bool              error;    // error occurred?

    do {
        error = false;   // no error yet

        /* try to read the fraction x and catch
         * errors of the type DenomIsZero
         */
        try {
            std::cout << "enter fraction (numer/denom): ";
            std::cin >> x;
            std::cout << "input was: " << x << std::endl;
        }
        catch (const CPPBook::Fraction::DenomIsZero&) {
            /* output error message and continue the loop
             */
            std::cout << "input error: numerator can not be zero"
                      << std::endl;
            error = true;
        }
    } while (error);

    return x;             // return read fraction
}

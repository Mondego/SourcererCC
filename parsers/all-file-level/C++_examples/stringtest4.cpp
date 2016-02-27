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
int main()
{
    try {
        //...
    }
    catch (const CPPBook::String::RangeError& error) {
        // exit main() with error message and error status
        std::cerr << "ERROR: invalid index " << error.index
                  << " when accessing string \"" << error.value
                  << "\"" << std::endl;
        return EXIT_FAILURE;
    }
}

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
/* constructor for last name and first name
 * - default for first name: empty string
 */
Person::Person(const std::string& ln, const std::string& fn)
  : lname(ln), fname(fn)    // initialize first and last names with passed parameters
{
    // nothing else to do
}

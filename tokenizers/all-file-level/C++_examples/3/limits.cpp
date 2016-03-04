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
#include <iostream>
#include <limits>
#include <string>

int main()
{
   using namespace std;

   // use textual representation for bool
   cout << boolalpha;

   // output maxima of integral types
   cout << "max(short): " << numeric_limits<short>::max() << endl;
   cout << "max(int):   " << numeric_limits<int>::max() << endl;
   cout << "max(long):  " << numeric_limits<long>::max() << endl;
   cout << endl;

   // output maxima of floating-point types
   cout << "max(float):       "
        << numeric_limits<float>::max() << endl;
   cout << "max(double):      "
        << numeric_limits<double>::max() << endl;
   cout << "max(long double): "
        << numeric_limits<long double>::max() << endl;
   cout << endl;

   // is char a signed interal type?
   cout << "is_signed(char): "
        << numeric_limits<char>::is_signed << endl;
   cout << endl;

   // are there numeric limits for the type string?
   cout << "is_specialized(string): "
        << numeric_limits<string>::is_specialized << endl;
}

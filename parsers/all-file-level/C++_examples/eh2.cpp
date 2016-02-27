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
char f1 (const std::string s, int idx)
{
    std::string tmp = s;  // local object that is destroyed
    //...                    // if there is an exception
    char c = s.at(idx);   // could trigger an exception
    //...
    return c;
}

void foo()
{
    try {
        std::string s("hello");  // is destroyed if there is an exception
        f1(s,11);    // triggers an exception
        f2();        // is not called if there is an exception in f1()
    }
    catch (...) {
        std::cerr << "Exception, but we will go on" << std::endl;
    }

    // program continues here after the exception in f1()
    //...
}

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
#include <fstream>
#include <string>

void redirect(std::ostream&,const std::string&);

int main()
{
    std::cout << "first line" << std::endl;

    redirect(std::cout, "redirect.txt"); // redirect cout to redirect.txt

    std::cout << "last line" << std::endl;
}

void redirect(std::ostream& strm, const std::string& filename)
{
    // open file (with buffer) for writing
    std::ofstream file(filename.c_str());

    // save output buffer of the passed stream
    std::streambuf* strm_puffer = strm.rdbuf();

    // redirect output to the file
    strm.rdbuf(file.rdbuf());

    file << "line is directly written to the file" << std::endl;
    strm  << "line is written to the redirected stream"
          << std::endl;

    // restore old output buffer of the passed stream
    strm.rdbuf(strm_puffer);

}  // closes the file and the redirected buffer

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
#include <string>       // for strings
#include <iostream>     // for I/O
#include <fstream>      // for file I/O
#include <iomanip>      // for setw()
#include <cstdlib>      // for EXIT_FAILURE

// forward declarations
void writeCharsetInFile(const std::string& filename);
void printFile(const std::string& filename);

int main()
{
    try {
        writeCharsetInFile("charset.out");
        printFile ("charset.out");
    }
    catch (const std::string& msg) {
        std::cerr << "Exception: " << msg << std::endl;
        return EXIT_FAILURE;
    }
}

void writeCharsetInFile(const std::string& filename)
{
    // open file for writing
    std::ofstream file(filename.c_str());

    // was the file really opened?
    if (! file) {
        // NO, throw exception
        throw "cannot open file \"" + filename
              + "\" for writing";
    }

    // write character set into file
    for (int i=32; i<127; ++i) {
        // output value as number and character:
        file << "value: " << std::setw(3) << i << "   "
             << "character: " << static_cast<char>(i) << std::endl;
    }

}   // closes file automatically

void printFile(const std::string& filename)
{
    // open file for reading
    std::ifstream file(filename.c_str());

    // was file really opened?
    if (! file) {
        // NO, throw exception
        throw "cannot open file \"" + filename
              + "\" for reading";
    }

    // copy all characters of the file to std::cout
    char c;
    while (file.get(c)) {
        std::cout.put(c);
    }

}   // closes the file automatically

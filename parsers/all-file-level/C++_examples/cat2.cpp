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
// header files
#include <iostream>
#include <fstream>
#include <string>

void printFileTwice(const std::string& filename)
{
    char c;

    // open file for reading
    std::ifstream file(filename.c_str());

    // output content of the file for the first time
    while (file.get(c)) {
        std::cout.put(c);
    }

    // clear eofbit and failbit (were set because of EOF)
    file.clear();

    // set read position to the start of the file
    file.seekg(0);

    // output content of the file for the second time
    while (file.get(c)) {
        std::cout.put(c);
    }
}

int main(int argc, char* argv[])
{
    // output all passed files in the command line twice
    for (int i=1; i<argc; ++i) {
        printFileTwice(argv[i]);
    }
}

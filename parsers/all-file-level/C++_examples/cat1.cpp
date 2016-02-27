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
#include <fstream>
#include <iostream>
using namespace std;

/* for all passed filenames in the command line
 * - open file, output content and close file
 */
int main (int argc, char* argv[])
{
    // create file stream for reading (without opening a file)
    std::ifstream file;

    // for all arguments from the command line
    for (int i=1; i<argc; ++i) {
        // open file
        file.open(argv[i]);

        // output content of file
        char c;
        while (file.get(c)) {
            std::cout.put(c);
        }

        // clear eofbit and failbit (were set because of EOF)
        file.clear();

        // close file
        file.close();
    }
}

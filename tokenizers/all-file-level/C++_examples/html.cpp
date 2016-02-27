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
#include <iostream>    // C++ header file for I/O
#include <string>      // C++ header file for strings

int main()
{
    const std::string start("http:");           // start of an HTML link
    const std::string separator (" \"\t\n<>");  // characters that end the link
    std::string line;                           // current line
    std::string link;                           // current HTML link
    std::string::size_type begIdx, endIdx;      // indices

    // for every line read successfully
    while (getline(std::cin,line)) {
        // search for first occurence of "http:"
        begIdx = line.find(start);

        // as long as "http:" was found in the line,
        while (begIdx != std::string::npos) {
            // find the end of the link
            endIdx = line.find_first_of(separator,begIdx);

            // extract the link
            if (endIdx != std::string::npos) {
                // extract from the start to the end
                link = line.substr(begIdx,endIdx-begIdx);
            }
            else {
                // no end found: use remainder of the line
                link = line.substr(begIdx);
            }

            // output link
            // - ignore "http:" without further characters
            if (link != "http:") {
                link = std::string("Link: ") + link;
                std::cout << link << std::endl;
            }

            // search for another link in the line
            if (endIdx != std::string::npos) {
                // search for an additional occurence of "http:" from the found end
                begIdx = line.find(start,endIdx);
            }
            else {
                // end of link was the line end: no new start index
                begIdx = std::string::npos;
            }
        }
    }
}

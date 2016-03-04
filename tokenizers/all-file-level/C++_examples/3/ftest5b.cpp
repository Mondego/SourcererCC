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
while (! (std::cin >> x)) {
    char c;

    if (std::cin.bad()) {
        // fatal input error: exit program
        std::cerr << "fatal error during intput of fraction"
                  << std::endl;
        std::exit(EXIT_FAILURE);
    }
    if (std::cin.eof()) {
        // end of file: exit program
        std::cerr << "EOF with input of fraction" << std::endl;
        std::exit(EXIT_FAILURE);
    }
    /* non-fatal error:
     * - reset failbit
     * - read everything up to the end of the line and try again (loops!)
     */
    std::cin.clear();
    while (std::cin.get(c) && c != '\n') {
    }
    std::cerr << "Error during input of fraction, try again: "
              << std::endl;
}

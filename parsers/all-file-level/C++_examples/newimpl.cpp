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
// possible implementation of operator new
void* operator new (std::size_t size)
{
    void* p;        // pointer for new memory

    // as long as we do not get new memory,
    // call new handler or throw exceptions
    while ((p = getMemory(size)) == 0) {
        // no sufficient memory available
        if (MyNewHandler != 0) {
              // call new handler
              (*myNewHandler)();
        }
        else {
              // throw exeption
              throw std::bad_alloc();
        }
    }

    // OK, return new memory
    return p;
}

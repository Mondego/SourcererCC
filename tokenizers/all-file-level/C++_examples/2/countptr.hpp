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
#ifndef COUNTED_PTR_HPP
#define COUNTED_PTR_HPP

/* class template for smart pointer with reference semantics
 * - destroys the object that is referred to when the last CountedPtr
 *     that refers to it is destroyed
 */
template <typename T>
class CountedPtr {
  private:
    T* ptr;        // pointer to the actual object
    long* count;   // reference to the number of pointers that refer to it

  public:
    // initialize with ordinary pointer
    // - p has to be a value returned by new
    explicit CountedPtr (T* p=0)
     : ptr(p), count(new long(1)) {
    }

    // copy constructor
    CountedPtr(const CountedPtr<T>& p) throw()
     : ptr(p.ptr), count(p.count) {  // copy object and counter
        ++*count;                    // increment number of references
    }

    // destructor
    ~CountedPtr () throw() {
        release();             // release reference to the object
    }

    // assignment
    CountedPtr<T>& operator= (const CountedPtr<T>& p) throw() {
        if (this != &p) {       // if not a reference to itself
            release();          // release reference to old object
            ptr = p.ptr;        // copy new object
            count = p.count;    // copy counter
            ++*count;           // increment number of references
        }
        return *this;
    }

    // access to the object
    T& operator*() const throw() {
        return *ptr;
    }

    // access to a member of the object
    T* operator->() const throw() {
        return ptr;
    }

  private:
    void release() {
        ++*count;               // decrement number of references
        if (*count == 0) {      // if last reference
             delete count;      // destroy counter
             delete ptr;        // destroy object
        }
    }
};

#endif /*COUNTED_PTR_HPP*/

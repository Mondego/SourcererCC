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
// C header file for I/O
#include <stdio.h>

// C header file for the string treatment
#include <string.h>

void f()
{
    const char* c = "input: ";  // string constant
    char        text[81];       // string variable for 80 characters
    char        s[81];          // string variable for the input (up to 80 characters)

    /* read string s
     * - because of limited memory, no more than 80 characters
     */
    if(scanf("%80s", s) != 1) {
       // read error
       //...
    }

    // compare string with empty string
    if(strcmp(s,"") == 0) {
       /* assign string literal to string text
        * - CAUTION: text has to be big enough
        */
       strcpy(text, "no input");
    }
    else {
        /* assign string constants c, followed by read string, to text
         * - CAUTION: text has to be big enough
         */
        if(strlen(c)+strlen(s) <= 80) {
           strcpy (text, c);
           strcat (text, s);
        }
    }
    //...
}

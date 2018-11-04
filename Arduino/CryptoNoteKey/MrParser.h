/*
    MrParser.h
    Author: Jakub W�jcik

    Library designed to parse strings as commands
    Command structure examples:
    <command:property1:property2 ... propertyN>
    <command>

    What this library do:
    1. Takes char array as input
    2. Removes start & stop delimiters
    3. Splits string by separator
    4. Interprets command inside mrParse()
*/

#if !defined(MrEngine)
#define MrEngine 1

#include "stdlib.h"
#include "string.h"

/*
    List containing all delimiters,
    '<' is start of the command
    ':' is separator for properties
    '>' is end of the command
*/
extern const char* delim;

int mrParse(const char* comBuffer); // Parses command, returns 1 when command successfully parsed
int mrCountProp(const char* comBuffer); // Returns number of parameters in command

#endif // MrParser

#include <Arduino.h>

#ifndef LEDlib
#define LEDlib 1
#define R 15
#define G 13
#define B 12

void ledInitialize();
void ledSet(byte red, byte green, byte blue);

#endif

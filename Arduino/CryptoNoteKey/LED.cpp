#include "LED.h"
#include <Arduino.h>

void ledInitialize()
{
  pinMode(R, OUTPUT);
  pinMode(G, OUTPUT);
  pinMode(B, OUTPUT);
}

void ledSet(byte red, byte green, byte blue)
{
  analogWrite(R, red);
  analogWrite(G, green);
  analogWrite(B, blue);
}

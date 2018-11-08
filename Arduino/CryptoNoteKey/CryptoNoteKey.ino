#include "LED.h"
#include "devInfo.h"
#include "MrParser.h"
#include "FS.h"

char dataBuf[devBufSize];
int bufPos = 0;
bool readBusy = false;
bool bufferCleared = false;
bool debug = false;
bool startToken = false;
bool endToken = false;
bool lock = true;

void setup() {
  //Serial.begin(921600);
  Serial.begin(115200);
  ledInitialize();
  ledSet(0, 10, 0);
  SPIFFS.begin();

}

bool addToBuffer(char c)
{
  if (bufPos < devBufSize) {
    dataBuf[bufPos] = c;
    bufPos++;
  }
}

void loop() {
  if(lock)
    ledSet(120, 0 , 200);
  
  if (Serial.available())
  {
    char tmp = (char)Serial.read();

    if (bufPos == 0 && tmp == '<')
    {
      ledSet(15, 80 , 80);
      memset(dataBuf, '\0', devBufSize);
      startToken = true;
      addToBuffer(tmp);
    }
    else if (startToken && tmp == '>')
    {
      ledSet(255, 255 , 255);
      addToBuffer(tmp);
      endToken = true;

      if (bufPos > 2)
      {
        if (debug) {
          for (int i = 0; i < devBufSize; i++) {
            if (dataBuf[i] == '\0')
              break;

            Serial.print(dataBuf[i]);
          }
          Serial.print('\n');
        }

        if (mrParse(dataBuf)) {
          ledSet(0, 255, 0); // Command parsed
        }
        else {
          ledSet(255, 0, 0); // Invalid command
        }

        startToken = false;
        endToken = false;
        bufPos = 0;
      }
    }
    else if(startToken)
    {
      ledSet(80, 255 , 120);
      if (!addToBuffer(tmp))
      {
        startToken = false;
        endToken = false;
        bufPos = 0;
      }
    }
  }
}

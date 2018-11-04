#include "LED.h"
#include "devInfo.h"
#include "MrParser.h"
#include "FS.h"

char dataBuf[devBufSize];
int bufPos = 0;
bool readBusy = false;
bool bufferCleared = false;
bool debug = false;

void setup() {
  Serial.begin(921600);
  ledInitialize();
  ledSet(0, 10, 0);
  SPIFFS.begin();

}

void loop() {
  ledSet(0, 10, 0);
  if (Serial.available()) {
    ledSet(15, 80 , 255);
    bufPos = 0;
    memset(dataBuf, '\0', devBufSize);

    while (Serial.available()) {
      char tmp = (char)Serial.read();
      if (bufPos < devBufSize) {
        dataBuf[bufPos] = tmp;
        bufPos++;
      }
      else {
        if (debug)
          Serial.println("Buffer too small");
        else
          Serial.print("<endOfBuffer>");
      }

      delay(1);
    }

    if (debug && !Serial.available()) {
      for (int i = 0; i < devBufSize; i++) {
        if (dataBuf[i] == '\0')
          break;

        Serial.print(dataBuf[i]);
      }
      Serial.print('\n');
    }

    // parsing a command
    if (mrParse(dataBuf)) {
      ledSet(0, 255, 0); // Command parsed
    }
    else {
      ledSet(255, 0, 0); // Invalid command
    }
  }
}

/*
    MrParser.c
    Author: Jakub W�jcik

    For details see MrParser.h
*/

#include "MrParser.h"
#include "Arduino.h"
#include "HardwareSerial.h"
#include "devInfo.h"
#include "FS.h"
#include "LED.h"
#include "stdio.h"
#include "stdlib.h"

const char* delim = "<:>"; // This char array must have 3 elements!
File curF;

int mrParse(const char* comBuffer)
{
  char* com; // Contains parsed command
  int bufLenght = strlen((char*)comBuffer); // Contains length (how many signs) of entered string, is used for validation

  int propCount = mrCountProp(comBuffer); // Contains how many properties has comBuffer
  char* prop[propCount]; // Contains all properties

  if (bufLenght > 2 &&
      comBuffer[0] == delim[0] &&
      (comBuffer[bufLenght - 1] == delim[2] ||
       comBuffer[bufLenght - 2] == delim[2]
      )) // Command validation
  {
    int i = 0;

    char tempBuf[bufLenght + 1]; // Buffer dedicated for strtok() which is destructive
    strcpy(tempBuf, (char*)comBuffer);

    // Command and properties extraction
    char* ectract;
    ectract = com = strtok(tempBuf, delim);

    while (ectract != NULL)
    {
      ectract = strtok(NULL, delim);
      prop[i] = ectract;
      i++;
    }

    if (!strcmp("getInfo", com)) {
      String data = "<devInfo:";
      data += devName;
      data += ':';
      data += devID;
      data += ':';
      data += devBufSize;
      data += ':';
      data += lock;
      data += '>';

      Serial.print(data);

      if (debug)
        Serial.print('\n');
    }
    else if (!strcmp("setDebug", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }

      if (propCount == 1)
      {
        if (!strcmp("true", prop[0]) || !strcmp("1", prop[0]))
        {
          Serial.println("<Debug on>");
          debug = true;
        }
        else if (!strcmp("false", prop[0]) || !strcmp("0", prop[0]))
          debug = false;
      }
      else
      {
        Serial.print("<err:Too few arguments>");
        if (debug)
          Serial.println("");
      }
    }
    else if (!strcmp("format", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }

      if (debug)
        Serial.println("<Formating memory>");
      SPIFFS.format();
      Serial.print("<formatDone>");
      if (debug)
        Serial.print('\n');
    }
    else if (!strcmp("list", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }

      String str = "<list";
      Dir dir = SPIFFS.openDir("/");
      while (dir.next())
      {
        str += ":";
        str += dir.fileName();
        //str += " / ";
        //str += dir.fileSize();

      }
      str += ">";
      if (debug)
        str += "\r\n";
      Serial.print(str);
    }
    else if (!strcmp("open", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }

      if (propCount == 2)
      {
        if (!strcmp("r", prop[1]))
          curF = SPIFFS.open(prop[0], "r");
        else if (!strcmp("w", prop[1]))
          curF = SPIFFS.open(prop[0], "w");
        else if (!strcmp("r+", prop[1]))
          curF = SPIFFS.open(prop[0], "r+");
        else if (!strcmp("w+", prop[1]))
          curF = SPIFFS.open(prop[0], "w+");
        else if (!strcmp("a", prop[1]))
          curF = SPIFFS.open(prop[0], "a");
        else if (!strcmp("a+", prop[1]))
          curF = SPIFFS.open(prop[0], "a+");
        else
        {
          if (debug)
            Serial.println("Mode string is invalid");
        }

        if (curF)
        {
          ledSet(0, 0, 255);
          Serial.print("<ok>");
          if (debug)
            Serial.println("");
        }
        else
        {
          Serial.print("<err:Coudn't open file> ");
          if (debug)
            Serial.println("");
        }
      }
      else
      {
        Serial.print("<err: Too few arguments>");
        if (debug)
          Serial.println("");
      }
    }
    else if (!strcmp("close", com))
    {
      if (curF)
      {
        curF.close();
      }

      Serial.print("<ok>");
      if (debug)
        Serial.println("");
    }
    else if (!strcmp("read", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }

      if (propCount == 1)
      {
        File f = SPIFFS.open(prop[0], "r");
        if (f)
        {
          ledSet(0, 0, 255);
          Serial.print("<read:");

          //Serial.print(f.readStringUntil('\0'));

          while (f.available())
          {
            Serial.print((char)f.read());
            delayMicroseconds(100);
          }
          Serial.print(">");
          if (debug)
            Serial.println("");
        }
        else
        {
          Serial.print("<err: Coudn't open/read file>");
          if (debug)
            Serial.println("");
        }

        f.close();
      }
      else
      {
        Serial.print("<err:Too few arguments");
        if (debug)
          Serial.println("");
      }
    }
    else if (!strcmp("newFile", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }


      if (propCount == 1)
      {
        curF = SPIFFS.open(prop[0], "w");
        if (curF)
        {
          ledSet(18, 100, 255);
          Serial.print("<ok>");
          if (debug)
            Serial.println("");
        }
        else
        {
          Serial.print("<err:Coudn't create new file>");
          if (debug)
            Serial.println("");
        }
      }
      else
      {
        Serial.print(" < err: Too few arguments");
        if (debug)
          Serial.println("");
      }
    }
    else if (!strcmp("a", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }


      if (propCount == 1)
      {
        ledSet(25, 180, 255);
        if (curF)
        {
          if (debug)
          {
            Serial.print("Writing to file: ");
            Serial.println(curF.name());
            Serial.print("Data: ");
            Serial.print(prop[0]);
            Serial.print(" len: ");
            Serial.println(strlen(prop[0]));
          }

          for (int i = 0; i < strlen(prop[0]); i++)
          {
            curF.write((uint8_t)prop[0][i]);
          }

          Serial.print("<ok>");
          if (debug)
            Serial.println("");
        }
        else
        {
          Serial.print("<err: There is no opened file>");
          if (debug)
            Serial.println("");
        }
      }
      else
      {
        Serial.print(" <ok>");
        if (debug)
          Serial.println("");
      }
    }
    else if (!strcmp("getMemInfo", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }


      FSInfo nfo;
      SPIFFS.info(nfo);

      Serial.print("<");
      Serial.print("memInfo");
      Serial.print(":");
      Serial.print(nfo.totalBytes);
      Serial.print(":");
      Serial.print(nfo.usedBytes);
      Serial.print(":");
      Serial.print(nfo.blockSize);
      Serial.print(":");
      Serial.print(nfo.pageSize);
      Serial.print(":");
      Serial.print(nfo.maxOpenFiles);
      Serial.print(":");
      Serial.print(nfo.maxPathLength);
      Serial.print(">");
      if (debug)
        Serial.println("");
    }
    else if (!strcmp("rename", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }


      if (propCount == 2)
      {
        if (SPIFFS.rename(prop[0], prop[1]))
        {
          Serial.print("<ok>");
          if (debug)
            Serial.println("");
        }
        else
        {
          Serial.print("<err: Coudn't rename file>");
          if (debug)
            Serial.println("");
        }
      }
      else
      {
        Serial.print("<err:Too few arguments>");
        if (debug)
          Serial.println("");
      }
    }
    else if (!strcmp("remove", com))
    {
      if (lock)
      {
        Serial.print("<err:Device is locked!>");
        if (debug)
          Serial.println("");
        return 0;
      }

      if (propCount = 1)
      {
        if (SPIFFS.remove(prop[0]))
        {
          Serial.print("<ok>");
          if (debug)
            Serial.println("");
        }
        else
        {
          Serial.print("<err:Coudn't remove file>");
          if (debug)
            Serial.println("");
        }
      }
      else
      {
        Serial.print("<err: Too few arguments>");
        if (debug)
          Serial.println("");
      }
    }
    else if (!strcmp("unlock", com))
    {
      if (propCount == 1)
      {
        if (!strcmp(pass, prop[0]))
        {
          lock = false;
          Serial.print("<ok>");
          if (debug)
            Serial.println("");
        }
        else
        {
          Serial.print("<err: Password incorect!>");
          if (debug)
            Serial.println("");
        }
      }
    }
    else if (!strcmp("lock", com))
    {
      lock = true;
      Serial.print("<ok>");
      if (debug)
        Serial.println("");
    }

    return 1;
  }
  else
  {
    //("Command \"%s\" unrecognized\n", comBuffer);
    return 0;
  }
}

int mrCountProp(const char* comBuffer)
{
  int i = 0;
  int count = 0;
  for (; i < strlen(comBuffer); i++)
  {
    if (comBuffer[i] == delim[1]) // delim[1] == ':'
      count++;
  }

  return count;
}

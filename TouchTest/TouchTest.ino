#include <CapacitiveSensor.h>


/*
 * CapitiveSense Library Demo Sketch
 * Paul Badger 2008
 * Uses a high value resistor e.g. 10 megohm between send pin and receive pin
 * Resistor effects sensitivity, experiment with values, 50 kilohm - 50 megohm. Larger resistor values yield larger sensor values.
 * Receive pin is the sensor pin - try different amounts of foil/metal on this pin
 * Best results are obtained if sensor foil and wire is covered with an insulator such as paper or plastic sheet
 */


CapacitiveSensor cs_4_5 = CapacitiveSensor(4,5);        // 10 megohm resistor between pins 4 & 2, pin 2 is sensor pin, add wire, foil
CapacitiveSensor cs_4_6 = CapacitiveSensor(4,6);
CapacitiveSensor cs_4_7 = CapacitiveSensor(4,7);        // 10 megohm resistor between pins 4 & 6, pin 6 is sensor pin, add wire, foil


void setup()                    
{

   //cs_4_5.set_CS_AutocaL_Millis(0xFFFFFFFF);     // turn off autocalibrate on channel 1 - just as an example
   Serial.begin(57600);

}

long time =0;
void loop()                    
{
    long start = millis();
    long total5 =  cs_4_5.capacitiveSensor(30);
    long total6 =  cs_4_6.capacitiveSensor(30);
    long total7 =  cs_4_7.capacitiveSensor(30);
    long total8 =  0;//cs_4_8.capacitiveSensor(30);
    long total9 =  0;//cs_4_9.capacitiveSensor(30);


    //Serial.print(millis() - start);        // check on performance in milliseconds
    
    //Serial.print(",");                    // tab character for debug window spacing
    Serial.print(total9);                  // print sensor output 1
    
    Serial.print(",");
    Serial.print(total8);                  // print sensor output 2
    
    Serial.print(",");
    Serial.print(total7);                // print sensor output 3

    Serial.print(",");
    Serial.print(total6);                // print sensor output 3
    
    Serial.print(",");
    Serial.print(total5);                // print sensor output 3
    
    //Serial.print("\t");
    //Serial.print((millis() - time));
    
    Serial.println();
    //delay(10);                             // arbitrary delay to limit data to serial port 
    time = millis();
}

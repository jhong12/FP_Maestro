import processing.serial.*;

Serial myPort;
float pos = 0;
long startTime = -1;
long elapsed = -1;

void setup(){
  size(700, 800);        

  // List all the available serial ports
  println(Serial.list());
  myPort = new Serial(this, Serial.list()[7], 57600);
  myPort.bufferUntil('\n');
  
  background(0);
  stroke(255);
  
  startTime = millis();
}

float[] values = {0, 0, 0, 0, 0};
float[] maxValues = {0, 0, 0, 0, 0};
float[] minValues = {1000, 1000, 1000, 1000, 1000};
float touchThreshold = 100;
float x = 10;
void draw(){
  background(0);
  elapsed = millis() - startTime;
  
  //calibration
  if(elapsed < 10000){
    textSize(10);
    text(elapsed, 200, 100);
    for(int i=0; i<5; i++){
      text(maxValues[i], 100*(i+1), 550);
    }
  }else{
    fill(255);
    textSize(15);
    boolean touched = false;
    float newX = 0;
    float sum = 0;
    int maxIndex = -1;
    for (int i=0; i<5; i++){
      if(values[i] > touchThreshold) touched = true;
      
      if(sum < values[i]) {
        sum = values[i];
        maxIndex = i;
      }
    }
    
    float mask[] = {0.15, 0.7, 0.15};
    float maskedValue[] = {0.0, 0.0, 0.0, 0.0, 0.0};
    for(int i=0; i<5; i++){
      for(int j=0; j<3; j++){
        int maskingIndex = i + j - 1;
        if (maskingIndex < 0) maskingIndex = 1;
        else if (maskingIndex > 4) maskingIndex = 3;
        maskedValue[i] += mask[j] * values[maskingIndex];
      }
    }
    
    //noFill();
    //beginShape();
    //curveVertex(150,  500 - values[0] - 100);
    newX = 0;
    sum = 0;
    for(int i=0; i<5; i++){
      rect(100*(i+1), 500 - values[i], 80, values[i]);
      
      fill(255, 0, 0, 128);
      rect(100*(i+1), 500 - maskedValue[i], 80, maskedValue[i]);
      fill(255);
      
      text(maxValues[i], 100*(i+1), 550);
      text(values[i], 100*(i+1), 600);
      //text(rawValues[i], 100*(i+1), 650);
      //curveVertex(100*(i+1) + 50,  500 - values[i] - 100);
      
      if (maskedValue[i] > 100){
        newX += (100*(i+1) + 50)*maskedValue[i]; 
        sum += maskedValue[i];
      }
    }
    //println(values);
    if (sum > 0) newX = newX/sum;
    
    //if (Float.isNaN(x)) x = 0;
    x = x*alpha_x + newX*(1-alpha_x);
    
    //curveVertex(500,  500 - values[4] - 100);
    //endShape();
    text(touched+"", 100, 700);
    text(x+"", 200, 700);
    
    textSize(40);
    //text(x, 100, 650);
    fill(255);
    if (touched){
      ellipse(x, 650, 10, 10);
    }
  }  
}

float alpha_x = 0.3;
float alpha_value = 0.7;
float[] rawValues;
void serialEvent (Serial myPort) {
  // get the ASCII string:
  String inString = myPort.readStringUntil('\n');

  if (inString != null) {
    // trim off any whitespace:
    inString = trim(inString);
    inString = inString.replace("#YPR=", "");
    println(inString);
    rawValues = float(split(inString, ','));
    if(rawValues.length == 6){
      for(int i=3; i<6; i++){
        if (elapsed > 10000) {
          values[i-3] = values[i-3]*alpha_value + map(rawValues[i], minValues[i-3], maxValues[i-3], 0, 400) *(1-alpha_value);
        }else{
          if (maxValues[i-3] < rawValues[i]) maxValues[i-3] = rawValues[i];
          if (minValues[i-3] > rawValues[i]) minValues[i-3] = rawValues[i];
        }
      }
    }
  }
}

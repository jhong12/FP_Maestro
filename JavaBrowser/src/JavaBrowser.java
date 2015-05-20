import gnu.io.*;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import javax.swing.*;

import org.apache.commons.math3.filter.*;
import org.apache.commons.math3.linear.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Enumeration;

public class JavaBrowser {
	static CommPortIdentifier portId;
    static Enumeration portList;

    InputStream inputStream;
    SerialPort serialPort;
    Thread readThread;
    
    int displayWidth;
    int displayHeight;
    
    public class SerialThread extends Thread{
    	public boolean keepGoing = true;
    	BufferedReader in;
    	BufferedWriter out;
    	Robot r;
    	
    	public SerialThread(){
    		try{
    			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("/dev/tty.usbserial-AE01COQ2");
    			if ( portIdentifier.isCurrentlyOwned() )
    			{
    				System.out.println("Error: Port is currently in use");
    			}
    			else
    			{
    				CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
    				
    				if ( commPort instanceof SerialPort )
    				{
    					SerialPort serialPort = (SerialPort) commPort;
    					serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
    					
    					in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
    					out = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));
    					
    				}
    				else
    				{
    					System.out.println("Error: Only serial ports are handled by this example.");
    				}
    			}
    	    	r = new Robot();
    		}catch(Exception e){
    			e.printStackTrace();
            }
    	}
    	
    	double[] prevYPR = {0, 0, 0}; 
    	double currentX = 500, currentY = 500;
    	double scaleX = 1, scaleY = 1;
    	double alpha = 0.8f;
		long touchSum = 0;
		long prevTouchSum = 0;
    	public void run(){
    		// A = [ 1 ]
    		RealMatrix A = new Array2DRowRealMatrix(new double[][] { {1, 0}, {0, 1} });
    		// no control input
    		RealMatrix B = null;
    		// H = [ 1 ]
    		RealMatrix H = new Array2DRowRealMatrix(new double[][] { {1, 0}, {0, 1} });
    		// Q = [ 0 ]
    		RealMatrix Q = new Array2DRowRealMatrix(new double[][] { {1, 0}, {0, 1} });
    		// R = [ 0 ]
    		RealMatrix R = new Array2DRowRealMatrix(new double[][] { {1, 0}, {0, 1} });
    		/*
    		double[] initYPR = {0, 0}; // -30 ~ 30;
    		while(true){
				try {
					String line = in.readLine();
					initYPR = getYPR(line); // -30 ~ 30;
					if (initYPR[0] != 0 && initYPR[1] != 0) break;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
			
    		ProcessModel pm
    		   = new DefaultProcessModel(A, B, Q, new ArrayRealVector(new double[] { initYPR[0], initYPR[1] }), null);
    		   */
    		ProcessModel pm
 		   		= new DefaultProcessModel(A, B, Q, new ArrayRealVector(new double[] { currentX, currentY }), null);
    		MeasurementModel mm = new DefaultMeasurementModel(H, R);
    		KalmanFilter filter = new KalmanFilter(pm, mm);
    		
    		boolean touched = false;
    		long prevTouchTime = 0;
    		float touchAlpha = 0.3f;
    		long directionTime = 0;
    		while(keepGoing){
    			try{
    				String line = in.readLine();
    				//System.out.println(line); //#YPR=-178.96,28.98,38.48
    				prevTouchSum = touchSum;
    				touchSum = 0;
    				double[] ypr = getYPR(line); // -30 ~ 30;
    				touchSum = (long) (touchSum*(1-touchAlpha) + prevTouchSum*touchAlpha);
					long currentTime = Calendar.getInstance().getTimeInMillis();
					long touchDuration = currentTime - prevTouchTime;
					long touchDiff = touchSum - prevTouchSum;
    				
    				double[] yprDiff = new double[3];
    				
    				if (prevYPR[0] == 0 && prevYPR[1] == 0 && prevYPR[2] == 0){
    					for(int i=0; i<3; i++){
    						prevYPR[i] = ypr[i];
    					}
    				}
    				else{
    					for(int i=0; i<3; i++){
    						yprDiff[i] = ypr[i] - prevYPR[i];
    						
    						if(yprDiff[i] > 300 || yprDiff[i] < -300){
    							yprDiff[i] = 0;
    						}
    						if(yprDiff[i] < 0.5 && yprDiff[i] > -0.5){
    							yprDiff[i] = 0;
    						}
    						yprDiff[i] = Math.pow(yprDiff[i], 2)*Math.signum(yprDiff[i]) / 10;
    					}
    					
    					// kalman filter?
    					long touchThreshold = 150;
    					double realMoveThreshold = 2;
    					if (!touched){
    						if (touchSum > touchThreshold) {
    							prevTouchTime = currentTime;
    							touched = true; 
    						}
    					}else{
    						if (touchSum < touchThreshold) {
    							if (touchDuration < 500) {
    								r.mousePress(InputEvent.BUTTON1_MASK);
    					    	    try { Thread.sleep(200); } catch (Exception e) {}
    					    	    r.mouseRelease(InputEvent.BUTTON1_MASK);
    							}
    							touched = false;
    						}
    						else {
    							if (touchSum > 800){
    								if (directionTime == 0) directionTime = currentTime;
    								else if (currentTime - directionTime > 500){
    									r.mouseWheel(1);
    									
    								}
    							}else if (touchSum < 400){
    								if (directionTime == 0) directionTime = currentTime;
    								else if (currentTime - directionTime > 500){
    									r.mouseWheel(-1);
    								}
    							}else{
    								directionTime = 0;
    							}
    						}
    					}
    					
    					if (touchSum > touchThreshold && touchDuration > 500 && ypr[1] < 70 && ypr[1] > -70){
    						
    						if(yprDiff[0] > realMoveThreshold || yprDiff[0] < -realMoveThreshold) 
    							scaleX = 10;
    						else scaleX = 0.5;
    						if(yprDiff[1] > realMoveThreshold-2 || yprDiff[1] < -realMoveThreshold-2) 
    							scaleY = 10;
    						else 
    							scaleY = 0.5;
    						
    						currentX += yprDiff[0]*scaleX;
    						currentY += yprDiff[1]*scaleY;
    						if (currentX < 0) currentX = 0;
    						else if (currentX > displayWidth) currentX = displayWidth;
    						if (currentY < 0) currentY = 0;
    						else if (currentY > displayHeight) currentY = displayHeight;
    						
    						
    						filter.predict();
    						RealVector z = new ArrayRealVector(new double[]{currentX, currentY});
    						filter.correct(z);
    						double[] stateEstimate = filter.getStateEstimation();
    						currentX = stateEstimate[0]; currentY = stateEstimate[1];
    						
    				    	r.mouseMove((int)currentX, (int)currentY);
    					}

    					for(int i=0; i<3; i++){
    						prevYPR[i] = ypr[i];
    					}
    				}
    				
    				System.out.println(String.format("%.2f", yprDiff[0])+", "+String.format("%.2f", yprDiff[1])
    						+ "("+String.format("%.2f", currentX)+","+String.format("%.2f", currentY)+")\t"+touchSum+","+touchDuration+","+touchDiff);
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    		}
    	}
    	
    	long touch[] = {0, 0, 0};
    	public double[] getYPR(String input){
    		double[] result = {0.0f, 0.0f, 0.0f};
    		String[] numbers = input.replace("#YPR=", "").split(",");
    		for(int i=0; i<3; i++){
    			result[i] = Double.parseDouble(numbers[i]);
    		}
    		for(int i=0; i<3; i++){
    			touch[i] = Long.parseLong(numbers[i+3]);
    			touchSum += touch[i];
    		}
    		return result;
    	}
    }
	
	public JavaBrowser(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		displayWidth = (int) screenSize.getWidth();
		displayHeight = (int) screenSize.getHeight();
		
		SerialThread st = new SerialThread();
		st.start();
        try {
        	/*
        	Thread.sleep(5000);
        	Robot r = new Robot();
    	    r.mouseMove(500, 500);
        	Thread.sleep(1000);
    	    r.mouseMove(550, 550);
    	    
        	Thread.sleep(1000);
    	    r.mouseMove(600, 600);

        	Thread.sleep(1000);
    	    r.mouseMove(600, 600);
    	    

        	Thread.sleep(1000);
    	    r.mouseMove(100, 100);

    	    r.mousePress(InputEvent.BUTTON1_MASK);
    	    try { Thread.sleep(200); } catch (Exception e) {}
    	    r.mouseRelease(InputEvent.BUTTON1_MASK);
    	    
    	    r.mousePress(InputEvent.BUTTON1_MASK);
    	    try { Thread.sleep(200); } catch (Exception e) {}
    	    r.mouseRelease(InputEvent.BUTTON1_MASK);
    	    */
        } catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	
	public static void main(String[] args) {
		JavaBrowser jb = new JavaBrowser();
    }
}

package com.dynaton.xavierlizarraga.testsignalgenerator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import processing.core.PApplet;

public class UICharacResponse extends PApplet {

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uicharac_response);
    }*/

    Point a, b, diff;
    int fs = 44100;
    int offset, offset2, x_range;
    //PShape grid;

    public void setup()
    {

        println("Display size: " + width + ", " + height);
        background(255);
        fill(0);
        int offset = displayHeight / 12;
        int offset2 = 2*offset;
        // Draw Title
        textSize(26);
        text("Characteristic Response", displayWidth/2-(offset2), offset);
        // Draw display boundaries with a white rect with black strokes
        fill(255);
        stroke(0);
        rect(2*offset, 2*offset, displayWidth-(2*offset2), displayHeight-(2*offset2));
        // Draw a mark
  /*drawRedPointMark(2*offset, 2*offset);
   drawRedPointMark(displayWidth-(2*offset), displayHeight-(2*offset));
   drawRedPointMark(2*offset, 2*offset+600);
   drawRedPointMark(2*offset+1140, 2*offset);*/
        a = new Point();
        b = new Point();
        a.set_pos(offset2, offset2);
        b.set_pos(displayWidth-(offset2), displayHeight-(offset2));
        double d = distBetweenPoints(a, b);
        println("Distance between point A and B: "+d);
        // Initially, the display space can be divided in 78dB (-60 - 18 dBFS)
        // Compute the pixels difference between boundaries
        Point diff = diffBetweenPoints(a, b);
        println("Difference between point A and B: "+diff.x+", "+diff.y);
        x_range = diff.x;
        int down_bond = -60;
        int up_bond = 18;
        // pixels per dB
        int pixPerdB = diff.x/(up_bond-down_bond);
        println( pixPerdB + " pixels per dB unit");
        // how many lines fit the display space
        int nlines = diff.x/(pixPerdB*6);
        println(nlines + " horizontal lines");
        // draw db range at left vertical border - (y-axis)
        textSize(18);
        fill(0);
        int down_vt_label = offset2+diff.y;
        int up_vt_label = offset2+offset/10;
        text(str(down_bond), offset+5+30, down_vt_label);  // down boundary
        text(str(up_bond), offset+15+30, up_vt_label);  // up boundary
        int pixelVertRange =  down_vt_label - (offset2+offset/8);
        println(pixelVertRange + " pixels between up and down label");
        int step_6dBs = pixelVertRange / nlines;
        println(step_6dBs + " pixels each 6 dB label");
        for (int i = 1; i<nlines; i++) {
            String label = str(up_bond-(6*i));
            int x_posLabel = offset+5+30;
            // Correction of label location
            if (label.length()<3)
                x_posLabel = offset+15+30;
            if (label.length()<2)
                x_posLabel = offset+25+30;
            // print labels
            fill(0);
            text(label, x_posLabel, up_vt_label+(i*(step_6dBs+1)));
            // print line
            stroke(175);
            drawHorizontalDashedLine(offset2+1, offset2+(i*(step_6dBs+1)), offset2+diff.x-2, 175);
        }
        // Draw y-axis label
        ylabel(offset, height/2, "Magnitude [dB]");
        xlabel(width/2, height-offset, "Frequency [Hz]");

        // Draw x-axis with Frequency in lineal way (20-20000Hz)
        int x_range = diff.x;
        int fmin = 20;
        int fmax = 20000;//fs/2;

        // Draw x-axis with Frequency in log way

        // f should be defined in terms of fs, fmin and fmax
        double b1 = Math.log(fmin)/Math.log(2);
        double b2 = Math.log(fmax)/Math.log(2);

        // define log2 range
        double rb = b2 - b1;
        //println(rb);
        // define steps by display space resolution
        double log_step = (rb/x_range);//diff.x);
        double nf = b1;
        double f[]= new double[x_range];
        //println(log_step+" or "+Math.pow(2,b1));
        boolean cdn_draw = false;
        boolean cdn_label = false;
        textSize(18);
        // Process first mapped log frequency
        f[0] = Math.pow(2,nf);
        nf = nf + log_step;
        // Frequency labels
        int f_label[] = {20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000};
        // Draw the first and last frequency label
        text(round((float)f[0]), offset2,height-(1.65f*offset));
        text(fmax,offset2+x_range,height-(1.65f*offset));
        // Draw the rest of frequency labels
        for(int i = 1; i<x_range;i++){
            f[i] = Math.pow(2,nf);
            println(f[i]+"Hz with pixel "+ i);
            // print frequencies <100Hz
            if (f[i]<=100){
                if (f[i-1]%10.f>9.5f && f[i]%10.f<0.3f){
                    stroke(0);
                    cdn_draw = true;
                    if(f[i]>45 && f[i]<55)
                        cdn_label = true;
                }
            }
            else{
                // print frequencies >100Hz and <1kHz
                if (f[i]<1000){
                    if (f[i-1]%100.f>90 && f[i]%100.f<5){
                        cdn_draw = true;
                        for (int j = 0; j<3; j++)
                            if(f_label[j+2]>f[i]-50 && f_label[j+2]<f[i]+50)
                                cdn_label = true;
                    }
                }
                else{
                    // print frequencies >1kHz and <10kHz
                    if(f[i]<10000){
                        if (f[i-1]%1000.f>900 && f[i]%1000.f<50){
                            cdn_draw = true;
                            for (int j = 0; j<3; j++)
                                if(f_label[j+5]>f[i]-500 && f_label[j+5]<f[i]+500)
                                    cdn_label = true;
                        }
                    }
                    else{
                        // print frequencies >10kHz and <20kHz
                        //println(f[i]%10000, f[i-1]%10000);
                        if (f[i-1]%10000.f>9000 && f[i]%10000.f<500){
                            cdn_draw = true;
                            if(f_label[8]>f[i]-5000 && f_label[8]<f[i]+5000)
                                cdn_label = true;
                        }
                    }
                }
            }
            if (cdn_draw){
                // print vertical lines
                line(offset2+(i),offset2+1,offset2+(i),height-(offset2)-1);  // draw vertical line
                // print labels
                if (cdn_label){
                    double label = f[i];
                    double new_label = f[i];
                    if(f[i]%10>1){
                        new_label = f[i]-f[i]%10;
                        label = new_label;
                        if(f[i]>1000 && f[i]%1000>10){
                            new_label = f[i]-f[i]%1000;
                            label = new_label;
                        }
                    }
                    text(round((float)label), offset2+(i),height-(1.65f*offset));
                }
            }
            nf = nf + log_step;
            cdn_draw = false;
            cdn_label = false;
        }

        // Draw a 4096 - Characteristic response - point to point
        int specSize = 4096;
        float f_step = (fs/2.f)/specSize;
        float Hw[][] = new float[specSize][2];
        // read data from a text file with frequency bin and magnitude in two columns
        String[] lines = loadStrings("8192-samples_spectrum_myroom.txt");
        println(lines.length +" frequency bins on the H(w) readed from text file");
        Hw[0][0] = -50;
        for (int i = 0; i<specSize-1; i++){
            //fill Hw array with -3dB (testing flat Hw)
            //Hw[i][0] = -50;
            //println(f_step*i, i);
            // fill Hw array with a recorded IR from my room
            Hw[i+1][0] = -1*PApplet.parseFloat(split(lines[i+1],"-")[1]) + 40;
        }

        // NEXT TODO -
  /* 1 - Draw on logarithmic MODE - DONE - THINK on increase low frequency resolution
     2 - Use grid class.
     2 - Apply zoom with key or buttons.
     3 - Encapsulate Hw plot in a class.
     */
        // First compute clustering from H(w) to log frequency reoslution on the display
        // as we did for linear resolution

        // Clustering of frequency function to draw

        // IMPORTANT - THIS CLUSTERING PROCESSING CAN BE IMPROVED
        // right now it loses a lot of magnitude values on low frequencies because the loop design
        // dont let to recover old data and it should be revised. Besides we need to give a
        // frequency value to each pixel on a logarithmic resolution because the draw will improve
        nf = b1;
        float freq_step_ct = fmin;
        float dfreq_step_ct = fmin;
        float f_log_step = (float) Math.pow(2,log_step);
        int diff_fs = specSize/x_range;
        float mappedHw [][] = new float[x_range][2];
        // give a pixel label to each frequency bin
        for (int j = 0; j<x_range; j++){
            int ct_bin = 0;
            float sum_mag = 0;
            for(int k = 0; k <= specSize;k++){
                float val = f_step*k;
                //if (val>freq_step_ct-(f_log_step/2) && val<freq_step_ct+(f_log_step/2)){
                if(val>f[j]-(f_log_step/2) && val<f[j]+(f_log_step/2)){
                    Hw[k][1] = j;              // give label to each frequency bin
                    // Convert to lineal to make magnitude addition
                    sum_mag = sum_mag + dB2Lineal(Hw[k][0]);
                    ct_bin++;
                    //println(k, Hw[k][1], freq_step_ct, val);
                } else if (val>f[j]+(f_log_step/2))
                    continue;
            }
            // Convert to log domain (db), just after averaging
            mappedHw[j][0] = lineal2DB(sum_mag/ct_bin);
            mappedHw[j][1] = freq_step_ct;
            //println(mappedHw[j][0], j);
            // update freq index for frequency display range
            nf = nf + log_step;
            dfreq_step_ct = freq_step_ct;
            freq_step_ct = (float) Math.pow(2,nf);
            f_log_step = freq_step_ct - dfreq_step_ct;
        }
        // Clean NaN values
        float bufferMagValues [][]= new float[x_range][3];
        int ct = 0;
        for (int j = 0; j<x_range; j++){
            if(!Float.isNaN(mappedHw[j][0])){
                bufferMagValues[ct][0] = mappedHw[j][0];
                bufferMagValues[ct][1] = mappedHw[j][1];
                bufferMagValues[ct][2] = j;
                System.out.format("%.4fdB for %.4fHz for pixel %.1f in array position %d\n",bufferMagValues[ct][0],bufferMagValues[ct][1],bufferMagValues[ct][2], ct);
                ct++;
            }
        }
        // Draw on logarithmic MODE

        // Already we have a f array with the frequency values related to a
        // logarithmic frequency resolution. So, in this case we can try to draw the pixel
        // to the closer frequency bin related from the H(w) readed from txt.

        // Drawing with linear interpolation a 4096 - Characteristic response (mappedHw)
        bufferMagValues[0][1] = map(bufferMagValues[0][0], up_bond, down_bond,offset2+1, height-(offset2+1));
        int ctt = 1;
        for(int i=1;i<x_range-1;i++){
            if(i == PApplet.parseInt(bufferMagValues[ctt][2])){
                // map decibel value to pixel on the display
                bufferMagValues[ctt][1] = map(bufferMagValues[ctt][0], up_bond, down_bond,offset2+1, height-(offset2+1));
                // draw by points
                strokeWeight(3);
                stroke(200,50,50);
                // Avoiding function values out of display range - limited by magnitude boundaries
                float a_y = bufferMagValues[ctt-1][1];
                float b_y = bufferMagValues[ctt][1];
                // applying boundaries
                if (bufferMagValues[ctt-1][1]>height-offset2)
                    a_y = height-(offset2+1);
                else if (bufferMagValues[ctt-1][1]<offset2)
                    a_y = offset2+1;
                else if (bufferMagValues[ctt][1]>height-offset2)
                    b_y = height-(offset2+1);
                else if (bufferMagValues[ctt][1]<offset2)
                    b_y = offset2+1;
                // Avoiding values out of display range - in terms of magnitude
                if((bufferMagValues[ctt-1][1]> height-offset2 && bufferMagValues[ctt][1]>height-offset2) ||(bufferMagValues[ctt-1][1]<offset2+1 && bufferMagValues[ctt][1]<offset2+1))
                {
                    ctt++;
                    continue;
                }
                // Lineal interpolation
                //line(offset2+2+bufferMagValues[ctt-1][2],a_y, offset2+1+bufferMagValues[ctt][2],a_y);
                line(offset2+1+i,a_y, offset2+1+i+1,b_y);
                // TOTHINK - use other interpolation or apply a smoothing function.
                ctt ++;
            }
        }
    }

    public void draw() {
    }

    public float dB2Lineal(float dB){
        float lin = (float)Math.pow(10.f,dB/20.f);
        return lin;
    }

    public float lineal2DB(float smp){
        float dB = (float)(20.f*(Math.log(smp)/Math.log(10.f)));
        return dB;
    }

    public void drawLinealVerticalLinesAndLabels(){

    }
/*
void drawLogVerticalLinesAndLabels(){
  // Draw x-axis with Frequency in log way
  int fmin = 20;
  int fmax = 20000;//fs/2;

  // f should be defined in terms of fs, fmin and fmax
  double b1 = Math.log(fmin)/Math.log(2);
  double b2 = Math.log(fmax)/Math.log(2);

  // define log2 range
  double rb = b2 - b1;
  //println(rb);
  // define steps by display space resolution
  double log_step = (rb/x_range);//diff.x);
  double nf = b1;
  double f[]= new double[x_range];
  //println(log_step+" or "+Math.pow(2,b1));
  boolean cdn_draw = false;
  boolean cdn_label = false;
  textSize(18);
  // Process first mapped log frequency
  f[0] = Math.pow(2,nf);
  nf = nf + log_step;
  // Frequency labels
  int f_label[] = {20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000};
  // Draw the first and last frequency label
  text(round((float)f[0]), offset2,height-(1.65*offset));
  text(fmax, offset2+x_range,height-(1.65*offset));
  // Draw the rest of frequency labels
  for(int i = 1; i<x_range;i++){
    f[i] = Math.pow(2,nf);
    //println(f[i]+"Hz with pixel "+ i);
    // print frequencies <100Hz
    if (f[i]<=100){
      if (f[i-1]%10.>9.5 && f[i]%10.<0.3){
        stroke(0);
        cdn_draw = true;
        if(f[i]>45 && f[i]<55)
          cdn_label = true;
      }
    }
    else{
      // print frequencies >100Hz and <1kHz
      if (f[i]<1000){
        if (f[i-1]%100.>90 && f[i]%100.<5){
          cdn_draw = true;
          for (int j = 0; j<3; j++)
            if(f_label[j+2]>f[i]-50 && f_label[j+2]<f[i]+50)
              cdn_label = true;
        }
      }
      else{
        // print frequencies >1kHz and <10kHz
        if(f[i]<10000){
          if (f[i-1]%1000.>900 && f[i]%1000.<50){
            cdn_draw = true;
            for (int j = 0; j<3; j++)
              if(f_label[j+5]>f[i]-500 && f_label[j+5]<f[i]+500)
              cdn_label = true;
          }
        }
        else{
          // print frequencies >10kHz and <20kHz
          //println(f[i]%10000, f[i-1]%10000);
          if (f[i-1]%10000.>9000 && f[i]%10000.<500){
            cdn_draw = true;
            if(f_label[8]>f[i]-5000 && f_label[8]<f[i]+5000)
              cdn_label = true;
          }
        }
      }
    }
    if (cdn_draw){
      // print vertical lines
      line(offset2+(i),offset2+1,offset2+(i),height-(offset2)-1);  // draw vertical line
      // print labels
      if (cdn_label){
        double label = f[i];
        double new_label = f[i];
        if(f[i]%10>1){
          new_label = f[i]-f[i]%10;
          label = new_label;
          if(f[i]>1000 && f[i]%1000>10){
            new_label = f[i]-f[i]%1000;
            label = new_label;
          }
        }
        text(round((float)label), offset2+(i),height-(1.65*offset));
      }
    }
    nf = nf + log_step;
    cdn_draw = false;
    cdn_label = false;
  }
}*/

    public void drawHorizontalDashedLine(int lx, int y, int gx, int color_line) {
        for (int x = lx; x < gx; x += 3) {
            stroke(-(x+y>>1 & 1));
            //println(-(x+y>>1 & 1));
            if (-(x+y>>1 & 1)==0)
                stroke(color_line);
            line(x, y, x+3, y);
        }
    }

    public void ylabel(int x, int y, String label){
        textAlign(CENTER,BOTTOM);
        textSize(22);
        pushMatrix();
        translate(x,y);
        rotate(-HALF_PI);
        text(label,0,0);
        popMatrix();
    }

    public void xlabel(int x, int y, String label){
        textAlign(CENTER,BOTTOM);
        textSize(22);
        pushMatrix();
        translate(x,y);
        text(label,0,0);
        popMatrix();
    }

    public void drawRedPointMark(int p_x, int p_y) {
        // Draw mark
        strokeWeight(10);
        fill(255, 0, 0);
        stroke(255, 0, 0);
        point(p_x, p_y);
    }

    public double distBetweenPoints(Point A, Point B) {
        double dist = Math.sqrt(Math.pow(A.x-B.x, 2)+Math.pow(A.y-B.y, 2));
        return dist;
    }

    public Point diffBetweenPoints(Point A, Point B) {
        Point diff = new Point();
        diff.set_pos(B.x-A.x, B.y-A.y);
        return diff;
    }

    class Point {
        int x;
        int y;

        Point() {
            this.x = 0;
            this.y = 0;
        }
        public void set_pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    public void settings() {  size(displayWidth, displayHeight); }
    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "dynatonTest_UI_Log_Resolution2" };
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}

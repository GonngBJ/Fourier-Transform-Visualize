import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.io.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class main extends PApplet {

ArrayList points = new ArrayList<Complex>();
ArrayList<Complex> cal = new ArrayList<Complex>();
ArrayList<Point> trace = new ArrayList<Point>();
float scale = 0.03f;
float sx, sy;
boolean dragging = false;
boolean following = false;
boolean timeStop = false;
int lastMilllis = 0;
boolean showGraph= true;
float speed = 60f;
int timeCheckPoint = 0;

public void setup() {
  //fullScreen();
  
  prepareData(points);
  cal = solveDFT(points);
  surface.setResizable(true);
  surface.setTitle("Fourier Trasform @ made by gongbj");
  Complex a = new Complex(1, 2);
  Complex b = new Complex(3, 4);
  a.multiply(b);
  sx = 0;
  sy = 0;
}

String instruction = "g : Show/Hide Graph\nEnter : Follow Trace\nSpacebar : Reset Zoom&Location\nMouseWheel : Zoom In/Out\nAny Other Keys : Stop Time\n+/- : Change Speed\nc : clear drawing";
public void arrow(float x1, float y1, float x2, float y2) {
  line(x1, y1, x2, y2);
  pushMatrix();
  translate(x2, y2);
  float a = atan2(x1-x2, y2-y1);
  rotate(a);
  line(0, 0, -8 / scale, -10/ scale);
  line(0, 0, 8 / scale, -10 / scale);
  popMatrix();
} 

public int getTime() {
  int time;
  if (timeStop) {
    time = timeCheckPoint + (int)((lastMilllis / 1000f * cal.size() / speed));
  } else {
    time = timeCheckPoint + (int)(((millis() - lastMilllis)/ 1000f * cal.size() / speed));
  }
  return time;
}

int lastTime = 0;
public void draw() {
  background(0);
  pushMatrix();
  translate(width / 2, height / 2);
  scale(scale);
  int time = getTime();

  while (lastTime < time - 1) {
    lastTime++;
    Point preCal = preCalculate(cal, lastTime, 0, cal.size());
    if (trace.size() == 0) {
      trace.add(preCal);
    } else {
      if (trace.size() > 0 && !trace.get(trace.size() - 1).equals(preCal)) {
        trace.add(preCal);
      }
    }
  }
  if (following) {
    Point preCal = preCalculate(cal, time, 0, focusedFrequency != -1 ? focusedFrequency : cal.size());

    translate(-(float)preCal.x - sx, -(float)preCal.y - sy);
  } else {

    translate(-sx, -sy);
  }

  visualize(cal, time, 0);
  drawTrace();

  fill(255, 255, 255);

  popMatrix();

  textSize(10);
  textAlign(CENTER);
  text("Zoom : x" + scale + " | Speed : " + (61 - speed), width / 2, 30);
  time++;

  if (showGraph)
    drawFrequencyGraph(cal);


  strokeWeight(0);
  textSize(10);
  fill(100, 100, 100, 100);
  rect(0, 0, textWidth(instruction) + 20, (10 + textAscent()) * 6 + 20);
  fill(255, 255, 255);
  textAlign(LEFT);
  text(instruction, 10, textAscent() + 10);
}


public void drawTrace() {
  final int tlen = 10;
  final int tfade = 40;
  if (trace.size() <= 1) return;
  while (trace.size() > cal.size() - tlen) {
    trace.remove(0);
  }

  int alpha = (focusedFrequency != -1 ? 100 : 255);
  strokeWeight(3 / scale);
  for (int i = 1; i < trace.size(); i ++) {
    if (trace.size() == cal.size() - tlen && i <= tlen + tfade) {
      stroke(255, 255, 255, 255.0f / (tlen+tfade) * i * alpha / 255 );
    } else {
      stroke(255, 255, 255, alpha);
    }
    line((float)trace.get(i - 1).x, (float)trace.get(i - 1).y, (float)trace.get(i).x, (float)trace.get(i).y);
  }
}

public ArrayList<Complex> solveDFT(ArrayList<Complex> data) {
  int N = data.size();
  ArrayList<Complex> result = new ArrayList<Complex>();
  for (int k = 0; k < N; k ++) {

    Complex d = new Complex();

    for (int i = 0; i < N; i ++) {
      Complex cc = new Complex(cos(TWO_PI * k * i / N), -sin(TWO_PI * k * i / N));
      d.add(cc.multiply(data.get(i)));
    }
    d.devide(N);
    //print(d.size());
    print("\n");
    result.add(d);
  }
  return result;
}


public Point preCalculate(ArrayList<Complex> data, double t, double wt, int f) {
  double x, y;
  x=0.0f; 
  y=0.0f;

  double radius, angle;
  double N = data.size();

  for (int k = 0; k < f; k ++) {
    angle = TWO_PI * k * t / N;

    Complex cc = new Complex(cos((float)angle), sin((float)angle));
    cc.multiply(data.get(k));

    x += cc.re;
    y += cc.im;
  }

  return new Point(x, y);
}

public void visualize(ArrayList<Complex> data, double t, double wt) {
  double x, y, px, py;
  x=0.0f; 
  y=0.0f;

  double radius, angle;
  double N = data.size();

  //blendMode(ADD);
  ellipseMode(RADIUS);

  for (int k = 0; k < N; k ++) {
    angle = TWO_PI * k * t / N;

    Complex cc = new Complex(cos((float)angle), sin((float)angle));
    cc.multiply(data.get(k));

    radius = cc.size();
    px = x; 
    py = y;
    x += cc.re;
    y += cc.im;

    if (radius * scale < 2 && k != focusedFrequency)continue;
    fill(0, 0, 0, 0);
    if (focusedFrequency != -1) {
      if (focusedFrequency == k) {
        stroke(255, 100, 100, 255);
        strokeWeight( 5 / scale);
      } else if (focusedFrequency == k - 1 || focusedFrequency == k + 1) {
        stroke(100, 100, 100, 200);
        strokeWeight( 1.5f / scale);
      } else {
        stroke(100, 100, 100, 50);
        strokeWeight( 1.5f / scale);
      }
    } else {
      stroke(100, 100, 100, 200);
      strokeWeight( 1.5f / scale);
    }

    ellipse((float)px, (float)py, (float)radius, (float)radius);

    if (focusedFrequency == -1) {
      strokeWeight( 3 / scale);
      stroke(100, 100, 250, 220);
    } else {
      if (focusedFrequency == k) {
        strokeWeight( 3 / scale);
        stroke(100, 100, 250, 220);
      } else {
        strokeWeight( 3 / scale);
        stroke(100, 100, 250, 25);
      }
    }
    arrow((float)px, (float)py, (float)x, (float)y);

    if (focusedFrequency == -1) {
      stroke(180, 180, 220, 110);
      fill(10, 100, 100, 50);
    } else {
      if (focusedFrequency == k || focusedFrequency == k + 1) {
        stroke(255, 100, 100, 200);
        fill(255, 100, 100, 150);
      } else if (focusedFrequency == k + 2) {
        stroke(180, 180, 220, 110);
        fill(10, 100, 100, 50);
      } else {
        stroke(180, 180, 220, 25);
        fill(10, 100, 100, 25);
      }
    }
    ellipse((float)x, (float)y, 5 / scale, 5 / scale);
  }
  Point p = new Point(x, y);
  if (trace.size() == 0) {
    trace.add(p);
  } else {
    if (!trace.get(trace.size() - 1).equals(p)) {
      trace.add(p);
    }
  }
}

float graphFactor = 1;
int focusedFrequency = -1;

public void drawFrequencyGraph(ArrayList<Complex> data) {
  colorMode(HSB);

  for (int i = 0; i < data.size(); i ++) {
    float h = sqrt(pow((float)data.get(i).re, 2) + pow((float)data.get(i).im, 2));

    stroke(0, 0, 0, 0);

    fill(0, 0, 100, 100);
    rect((float)width / data.size() * i, 
      height - h * graphFactor - height * 0.3f, 
      (float)width / data.size(), 
      h * graphFactor + height * 0.3f);

    fill(20.0f / data.size() * i + 10, 255, 255);
    rect((float)width / data.size() * i, 
      height - h * graphFactor, 
      (float)width / data.size(), 
      h * graphFactor);

    fill(100, 100, 200);
    circle((float)width / data.size() * (2 * i + 1) / 2.0f, 
      height - h * graphFactor - height * 0.3f, 1);
  }

  colorMode(RGB);

  int i = (int)((float)mouseX / width * data.size());  
  try {
    float h = sqrt(pow((float)data.get(i).re, 2) + pow((float)data.get(i).im, 2));
    if (mouseY > height - h * graphFactor - height * 0.3f) {
      focusedFrequency = i;
      stroke(0, 255, 0);
      strokeWeight(5);
      fill(0, 255, 0);
      rect((float)width / data.size() * i, 
        height - h * graphFactor, 
        (float)width / data.size(), 
        h * graphFactor);
      stroke(200, 200, 200);
      strokeWeight(0.5f);
      line(mouseX, height - h * graphFactor - height * 0.3f, (float)width / data.size() * (2 * i + 1) / 2.0f, height - h * graphFactor);

      stroke(0, 0, 0, 0);
      fill(255, 255, 255);
      circle(mouseX, mouseY, 3);

      stroke(0, 0, 0, 0);
      colorMode(HSB);
      fill(100, 100, 200);
      circle((float)width / data.size() * (2 * i + 1) / 2.0f, 
        height - h * graphFactor - height * 0.3f, 4);
      colorMode(RGB);

      fill(255, 255, 255);
      String text = "Graph Is x" + String.valueOf(graphFactor) + "\nFrequency : " + String.valueOf(i) + "\nAmplitude : " + 
        String.valueOf(sqrt(pow((float)data.get(i).re, 2) + pow((float)data.get(i).im, 2)));

      textSize(15);

      if (mouseX > width / 2) {
        textAlign(RIGHT);
        text(text, mouseX - 10, mouseY - 70);
      } else {
        textAlign(LEFT);
        text(text, mouseX + 10, mouseY - 70);
      }
    } else {
      focusedFrequency = -1;
    }
  }
  catch(Exception e) {
  }
}

public void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  float preS = scale;

  int i = (int)((float)mouseX / width * cal.size());  
  float h = sqrt(pow((float)cal.get(i).re, 2) + pow((float)cal.get(i).im, 2));
  if (showGraph && mouseY > height - h * graphFactor - height * 0.3f) {
    graphFactor -= e * graphFactor / 50f;
    graphFactor = max(0.1f, graphFactor);
    return;
  }

  if (dragging) {
    mouseXOffset = mouseX;
    mouseYOffset = mouseY;
    preSx = sx;
    preSy = sy;
  }
  scale -= e / 5f * scale;
  scale = max(0.006f, scale);
}

float mouseXOffset, mouseYOffset;
float preSx, preSy;
public void mousePressed() {
  dragging = true;
  mouseXOffset = mouseX;
  mouseYOffset = mouseY;
  preSx = sx;
  preSy = sy;
}

public void mouseDragged() {
  sx = preSx + -(mouseX - mouseXOffset) / scale;
  sy = preSy + -(mouseY - mouseYOffset) / scale;
}

public void mouseReleased() {
  dragging = false;
}

public void keyReleased() {
  if (key == ' ') {
    sx = 0;
    sy = 0;
    if (!following) {
      scale = 0.03f;
    } else {
      scale = 0.5f;
    }
    timeCheckPoint = getTime();
    lastMilllis = millis();
    speed = 60;
    mouseXOffset = mouseX;
    mouseYOffset = mouseY;
    preSx = sx;
    preSy = sy;
  } else if (key == '\n') {
    if (following) {
      sx = ((float)trace.get(trace.size() - 1).x) + sx;
      sy = ((float)trace.get(trace.size() - 1).y) + sy;
    } else {
      sx = 0;
      sy = 0;
    }
    following = !following;
  } else if (key == 'g') {
    showGraph= !showGraph;
  } else if (key == '+') {
    timeCheckPoint = getTime();
    lastMilllis = millis();
    speed -= 1f;
    speed = max(1.0f, speed);
  } else if (key == '-') {
    timeCheckPoint = getTime();
    lastMilllis = millis();
    speed += 1f;
    speed = max(1.0f, speed);
  } else if (key == 'c') {
    trace.clear();
    lastTime = 0;
    lastMilllis = millis();
    timeCheckPoint = 0;
  } else {
    if (timeStop) {
      lastMilllis = millis() - lastMilllis;
      timeStop = false;
    } else {
      lastMilllis = millis() - lastMilllis;
      timeStop = true;
    }
  }
}


public String getEquation() {
  StringBuffer sb = new StringBuffer();
  for (int i = 0; i < cal.size(); i ++) {
    Complex item = cal.get(i);
    sb.append("e^{-2PI*i(" + Integer.valueOf(i) + " / " + cal.size() + ")}" + " * (" + String.valueOf(item.re) + " + " + String.valueOf(item.im) + "i)\n");
  }
  return sb.toString();
}

class Point {
  public double x = 0;
  public double y = 0;
  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  @Override
    public boolean equals(Object a) {
    if (a instanceof Point) {
      Point b = (Point)a;
      if (x == b.x && y == b.y) return true;
    }
    return false;
  }
}

public class Complex {
  public double re;
  public double im;
  private double size = -1;
  private double angle = 1000;

  public Complex() {
    this(0, 0);
  }

  public Complex(double re, double im) {
    this.re = re;
    this.im = im;
    size = -1;
    angle = 1000;
  }

  public Complex add(Complex b) {
    re += b.re;
    im += b.im;
    size = -1;
    angle = 1000;
    return this;
  }

  public Complex multiply(Complex b) {
    //(a1 + b1 i )(a2 + b2 i) = (a1a2 - b1b2) + (a1b2 + a2b1)
    
    double re2 = re * b.re - im * b.im;
    double im2 = re * b.im + b.re * im;
    re = re2;
    im = im2;
    size = -1;
    angle = 1000;
    return this;
  }
 public Complex multiply(double a) {
    re *= a;
    im *= a;
    size = -1;
    angle = 1000;
    return this;
  }

  public Complex devide(double a) {
    re = re / a;
    im = im / a;
    size = -1;
    angle = 1000;
    return this;
  }

  public double size() {
   if (size != -1) return size;
    size = (double)Math.sqrt(re * re + im * im);
    return size;
  }

  public double angle() {
    if (angle != 1000) return angle;
    angle = (double)Math.atan2(im, re);
    return angle;
  }
}
//https://shinao.github.io/PathToPoints/


public void prepareData(ArrayList<Complex> points) {
  try  
  {  
    //File file=new File("D:/과제/수학/Fourier Series/main/data.txt");    //creates a new file instance  
    File file=new File(sketchPath("data.txt"));    //creates a new file instance  
    FileReader fr=new FileReader(file);   //reads the file  
    BufferedReader br=new BufferedReader(fr);  //creates a buffering character input stream  
    String line;  
    while ((line=br.readLine())!=null)  
    {  
      String r[] = line.split(",");
      points.add(new Complex(Float.valueOf(r[0]), Float.valueOf(r[1])));
    }
    
    float centerRe = 0;
    float centerIm = 0;
    for(int i = 0; i < points.size(); i ++){
      centerRe += points.get(i).re / points.size();
      centerIm += points.get(i).im / points.size();
    }
    
    for(int i = 0; i < points.size(); i ++){
      points.get(i).add(new Complex(-centerRe,-centerIm));
    }
    for(int i = 0; i < points.size(); i ++){
      points.get(i).multiply(100);
    }
    fr.close();    //closes the stream and release the resources  
  }  
  catch(IOException e)  
  {  
    e.printStackTrace();
  }
}
  public void settings() {  size(1200, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "main" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

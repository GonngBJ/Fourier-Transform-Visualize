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

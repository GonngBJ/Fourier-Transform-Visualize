//https://shinao.github.io/PathToPoints/
import java.io.*;

void prepareData(ArrayList<Complex> points) {
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

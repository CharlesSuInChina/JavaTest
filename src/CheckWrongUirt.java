import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CheckWrongUirt {
    public static void main(String[] args){
        String uirtPath  = "/Users/suhao/Desktop/uirt(1).txt";
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(uirtPath));
            String line = null;
            while ((line = bufferedReader.readLine())!=null){
                int length = line.split(",").length;
                if (length!=11){
                    //System.out.println(line);
                    String[] lineArray = line.split(",");
                    StringBuffer stringBuffer = new StringBuffer();
                    for(int i = 0; i< length; i++){
                        if(!(i == 10 || i == 12 || i == 13)){
                            if(i==11){
                                stringBuffer.append(lineArray[i]);
                            }else {
                                stringBuffer.append(lineArray[i]+ ",");
                            }
                        }
                    }
                    System.out.println(stringBuffer.toString());
                }
            }
            bufferedReader.close();
        }catch (IOException e){
            e.printStackTrace();
        }


    }

}

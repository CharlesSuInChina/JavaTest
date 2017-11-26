import java.io.IOException;

/**
 * Created by suhao on 29/08/2017.
 */
public class TestForWift {

    public void functionA(){
        System.out.print("funtionA");
        functionB();
    }

    public void functionB(){
        System.out.print("funtionB");
        functionC();
    }

    public void functionC(){
        System.out.print("funtionC");
        IOException e = new IOException();
        try {
            throw e;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args){
        TestForWift testForWift = new TestForWift();
        testForWift.functionA();

    }
}

/**
 * Created by suhao on 26/05/2017.
 */
public class MainTest {

    public String a = "a";

    public static String b = "b";

    public void print2(){

    }

    public static void print(){

    }

    public static void main(String[] args){
        print();
        MainTest mainTest = new MainTest();
        mainTest.print2();
        MainTest.print();
        System.out.println();
    }
}

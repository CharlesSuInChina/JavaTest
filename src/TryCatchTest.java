/**
 * Created by suhao on 17/01/2017.
 */
public class TryCatchTest {
    public static void main(String[] args){
        try{
            Exception exception = new Exception();
            throw exception;
        }catch(Exception e){
            System.out.println("exception");
        }
        System.out.println("hello");
    }
}

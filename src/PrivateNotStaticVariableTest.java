/**
 * Created by suhao on 18/01/2017.
 */
public class PrivateNotStaticVariableTest {
    private String str;
    private static int i=0;

    private void testMethod(String string){
        str = string;
        i++;
        System.out.println(str);
        System.out.println(i);
    }

    private String getStr(){
        testMethod("cc");
        return str;
    }

    public static void main(String[] args){

        PrivateNotStaticVariableTest privateNotStaticVariableTest = new PrivateNotStaticVariableTest();
        privateNotStaticVariableTest.testMethod("aa");
        System.out.println(privateNotStaticVariableTest.getStr());
        PrivateNotStaticVariableTest privateNotStaticVariableTest2 = new PrivateNotStaticVariableTest();
        privateNotStaticVariableTest2.testMethod("bb");
        System.out.println(privateNotStaticVariableTest2.getStr());
    }




}

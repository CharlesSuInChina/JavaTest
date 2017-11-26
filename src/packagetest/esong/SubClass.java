package packagetest.esong;

/**
 * Created by suhao on 03/06/2017.
 */
public class SubClass{
    private static Father father = new Father();

    public SubClass(){
        System.out.println("aaaa");
        test("bbb");
    }

    private void test(String name){
        father.name=name;
    }

}

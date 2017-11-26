package packagetest;

import javax.swing.plaf.synth.SynthTextAreaUI;

/**
 * Created by suhao on 28/05/2017.
 */
public class PackageTest2 extends PackageTest{

    private String getParamAAA(){
        System.out.println(super.getAaa());

        return this.getAaa();
    }

    public String getAaa() {
        return "sub Aaa";
    }

    public static void main(String[] args){
        PackageTest2 packageTest2 = new PackageTest2();
        String aaa = packageTest2.getAaa();
        String subAaa = packageTest2.getParamAAA();
        packageTest2.getAaa();
        System.out.println(subAaa);
    }
}

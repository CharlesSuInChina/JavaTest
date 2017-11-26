package packagetest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Created by suhao on 15/06/2017.
 */
public class TestJSON {
    private void a(){

    }

    private static void b(){

    }

    public static void main(String[] args){
        aaa();

        b();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("aa","AA");
        jsonObject.put("bb","BB");
        jsonArray.add(jsonObject);
        String a = jsonArray.toString();
        System.out.println(a);

    }

    public static void aaa(String... abc){
        int a = abc.length;
        System.out.println(a);
    }
}

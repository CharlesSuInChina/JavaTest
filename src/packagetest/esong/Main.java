package packagetest.esong;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by suhao on 03/06/2017.
 */
public class Main {
    public static void main(String[] args){



        String str3 = "abcdefg";
        String str4 = str3.substring(1,3);



        Enum exceptionEnum;



        String str1 = "";
        String regularExpression = "^-?[0-9]+$";
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(str1);
        boolean bool = matcher.find();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("AAA","aaa");
        jsonObject.put("BBB","bbb");
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("CCC","ccc");
        jsonObject1.put("DDD","ddd");
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);
        jsonArray.add(jsonObject1);
        String str = jsonArray.toJSONString();
        System.out.println("");
    }

    public Enum getEnum(){
        return ExceptionEnum.ANR;
    }
}

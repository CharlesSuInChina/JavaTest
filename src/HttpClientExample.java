
import com.alibaba.fastjson.JSONObject;
import java.awt.peer.SystemTrayPeer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


public class HttpClientExample {
    private static final String USER_AGENT = "Mozilla/5.0";
    public static int requestCount = 0;

    // HTTP GET请求
    public static Double[] sendGet(String address) throws Exception {
        String baiduAK = "555108c049e87a0b11be97236e2181e4";
        baiduAK = "31rn828McAdqWWBGDEUxRTnm9st1E0RT";
        String url = String.format("http://api.map.baidu.com/geocoder/v2/?address=%s&output=json&ak=%s&callback=showLocation", address, baiduAK);
        System.out.println(url);
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        //添加请求头
        request.addHeader("User-Agent", USER_AGENT);
        requestCount++;
        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        if(responseCode != 200){
            System.out.println("Response Code : " + responseCode);
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        String resultStr = result.toString();
        System.out.println(resultStr);
        String lngStr;
        String latStr;
        Double lngDouble = 0.0;
        Double latDouble = 0.0;
        try{
            lngStr = resultStr.split("lng\":")[1].split(",")[0];
            latStr = resultStr.split("lat\":")[1].split("}")[0];
            lngDouble = Double.parseDouble(lngStr);
            latDouble = Double.parseDouble(latStr);
            System.out.println(lngStr+","+latStr);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Double[]{lngDouble, latDouble};
    }

    // HTTP POST请求
    private void sendPost() throws Exception {

        String url = "https://selfsolve.apple.com/wcResults.do";

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        //添加请求头
        post.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));
        urlParameters.add(new BasicNameValuePair("cn", ""));
        urlParameters.add(new BasicNameValuePair("locale", ""));
        urlParameters.add(new BasicNameValuePair("caller", ""));
        urlParameters.add(new BasicNameValuePair("num", "12345"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " +
            response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
            new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        System.out.println(result.toString());

    }

}

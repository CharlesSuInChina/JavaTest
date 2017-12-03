import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationHandler {
    static Map<String,Integer> addressMap = new HashMap<String,Integer>();
    static String UIRTByBlockCountFilePath = "result/" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "uirtByBlockCount.txt";
    private static double minLng = 112.0;
    private static double maxLng = 120.0;
    private static double minLat = 35.0;
    private static double maxLat = 43.0;
    private static double stepSize = 50.0; // km


    public static void main(String[] args){
        //calculate block
        Map<String, Double> modeMap = new LocationHandler().getBlockMode(minLng, minLat, maxLng, maxLat);
        System.out.println(modeMap.toString());
        // first step read uirt.txt
        new LocationHandler().readFileByLines("source/uirt(3).txt", modeMap);
        System.out.println("the request count is : " + HttpClientExample.requestCount);
    }

    public void readFileByLines(String fileName, Map<String, Double> modeMap) {
        File file = new File(fileName);
        BufferedReader reader = null;
        BufferedWriter bufferedWriter = null;
        Map<Double, Double> map = new HashMap<Double, Double>();
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(UIRTByBlockCountFilePath));


            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            int blockCount = -1;
            HttpClientExample http = new HttpClientExample();
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                System.out.println(line + ": " + tempString);
                line++;
                String[] tempStringArray = tempString.split(",");
                //String address = tempStringArray[tempStringArray.length-1].trim() + tempStringArray[tempStringArray.length-2].trim();
                // 坐标优化，省名+景点名
                String detailLocation = tempStringArray[tempStringArray.length-1].trim();
                String parkName = tempStringArray[tempStringArray.length-2].trim();
                String address = getAddress(detailLocation, parkName);

                if(!addressMap.keySet().contains(address)){
                    try {
                        Double[] doubles = HttpClientExample.sendGet(address);
                        if(doubles[0].equals(0.0) && doubles[1].equals(0.0)){
                            addressMap.put(address,-1);
                            continue;
                        }
                        if(doubles[0] < minLng || doubles[0] > maxLng){
                            addressMap.put(address,-1);
                            continue;
                        }else if(doubles[1] < minLat || doubles[1] > maxLat){
                            addressMap.put(address,-1);
                            continue;
                        }
                        blockCount = (int)getBlock(doubles[0], doubles[1], modeMap);
                    }catch (Exception e){
                        e.printStackTrace();
                        continue;
                    }
                }
                for(int i = 0; i < tempStringArray.length - 2; i++){
                    bufferedWriter.write(tempStringArray[i] + ",");
                }
                addressMap.put(address,blockCount);
                bufferedWriter.write(addressMap.get(address).toString());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    bufferedWriter.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    private String getAddress(String detailLocation, String parkName){
        String provinceName = "";
        String cityName = "";
        String countyName = "";
        String parkNameIndetailLocation = "";

        parkNameIndetailLocation = checkParkNameIndetailLocation(detailLocation);
        if(parkNameIndetailLocation.equals("")){
            countyName = checkCountyName(detailLocation);
            if(countyName.equals("")){
                cityName = checkCityName(detailLocation);
                if(cityName.equals("")){
                    provinceName = checkProvinceName(detailLocation);
                }
            }
        }

        String address = provinceName + cityName + countyName + parkNameIndetailLocation + parkName;
        System.out.println(address);
        return  address;
    }

    private String checkParkNameIndetailLocation(String detailLocation){
        String parkNameIndetailLocation = "";
        if(detailLocation.contains("野三坡")){
            parkNameIndetailLocation = "野三坡，";
        }
        return parkNameIndetailLocation;
    }

    private Double calculateStepSizeLng(Double lat){
        //calculate lng step size(degree)
        Double cosValue = Math.cos(2 * Math.PI * lat / 360.0);
        Double lngKmPerDu = 111.314 * cosValue;
        Double stepSizeLng = stepSize / lngKmPerDu;
        return  stepSizeLng;
    }

    private Map<String, Double> getBlockMode(Double minLng, Double minLat, Double maxLng, Double maxLat){
        Double stepSizeLng = calculateStepSizeLng(minLat);

        //calculate lat step size(degree)
        Double stepSizeLat = stepSize / 110.95;

        //calculate rank count
        Double lngDiff = maxLng - minLng;
        double lngDiffCount = Math.ceil(lngDiff / stepSizeLng);

        Map<String, Double> resultMap = new HashMap<String, Double>();
        resultMap.put("minLng", minLng);
        resultMap.put("minLat", minLat);
        resultMap.put("maxLng", maxLng);
        resultMap.put("maxLat", maxLat);
        resultMap.put("stepSizeLat", stepSizeLat);
        resultMap.put("lngDiffCount", lngDiffCount);
        return resultMap;
    }

    private double getBlock(Double lng, Double lat, Map<String, Double> map){
        Double minLng = map.get("minLng");
        Double minLat = map.get("minLat");
        Double stepSizeLng = calculateStepSizeLng(lat);
        Double stepSizeLat = map.get("stepSizeLat");
        Double lngDiffCount = map.get("lngDiffCount");

        double xCoordinate = Math.floor((lng - minLng) / stepSizeLng);
        double yCoordinate = Math.floor((lat - minLat) / stepSizeLat);

        double blockCount = yCoordinate * lngDiffCount + xCoordinate;
        return blockCount;
    }

    private String checkProvinceName(String detailLocation){
        String provinceName = "";
        if(detailLocation.contains("河北")){
            provinceName = "河北省，";
        }else if (detailLocation.contains("内蒙古")){
            provinceName = "内蒙古自治区，";
        }else if (detailLocation.contains("山西")){
            provinceName = "山西省，";
        }else if (detailLocation.contains("河南")){
            provinceName = "河南省，";
        }else if (detailLocation.contains("山东")){
            provinceName = "山东省，";
        }else if (detailLocation.contains("辽宁")){
            provinceName = "辽宁省，";
        }else if (detailLocation.contains("天津")){
            provinceName = "天津市，";
        }else if(detailLocation.contains("北京")){
            provinceName = "北京市，";
        }
        return provinceName;
    }

    private String checkCountyName(String detailLocation){
        String countyName = "";
        if (detailLocation.contains("涞水")){
            countyName = "涞水县，";
        }else if(detailLocation.contains("涞水")){
            countyName = "怀来县，";
        }else if(detailLocation.contains("张北")){
            countyName = "张北县，";
        }else if(detailLocation.contains("北戴河")){
            countyName = "北戴河区，";
        }else if(detailLocation.contains("蔚县")){
            countyName = "蔚县，";
        }else if(detailLocation.contains("井陉")){
            countyName = "井陉县，";
        }else if(detailLocation.contains("丰宁满族")){
            countyName = "丰宁满族自治县，";
        }
        return countyName;
    }

    private String checkCityName(String detailLocation){
        String cityName = "";
        if(detailLocation.contains("张家口")){
            cityName = "张家口市，";
        }else if(detailLocation.contains("石家庄")){
            cityName = "石家庄市，";
        }else if(detailLocation.contains("唐山")){
            cityName = "唐山市，";
        }else if(detailLocation.contains("秦皇岛")){
            cityName = "秦皇岛市，";
        }else if(detailLocation.contains("邯郸")){
            cityName = "邯郸市，";
        }else if(detailLocation.contains("邢台")){
            cityName = "邢台市，";
        }else if(detailLocation.contains("保定")){
            cityName = "保定市，";
        }else if(detailLocation.contains("承德")){
            cityName = "承德市，";
        }else if(detailLocation.contains("沧州")){
            cityName = "沧州市，";
        }else if(detailLocation.contains("廊坊")){
            cityName = "廊坊市，";
        }else if(detailLocation.contains("衡水")){
            cityName = "衡水市，";
        }else if(detailLocation.contains("涿州")){
            cityName = "涿州市，";
        }else if(detailLocation.contains("霸州")){
            cityName = "霸州市，";
        }
        return  cityName;
    }
}

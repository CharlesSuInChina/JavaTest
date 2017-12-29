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

//    private static double minLng = 112.0;
//    private static double maxLng = 120.0;
//    private static double minLat = 35.0;
//    private static double maxLat = 43.0;

    private static double minLng = 110.0;
    private static double maxLng = 123.0;
    private static double minLat = 31.0;
    private static double maxLat = 43.0;
    private static double stepSize; // km

    public static void main(String[] args){
        stepSize = Double.valueOf(args[0]);

        //calculate block
        Map<String, Double> modeMap = new LocationHandler().getBlockMode(minLng, minLat, maxLng, maxLat);
        System.out.println(modeMap.toString());
        String UIRTByBlockCountFilePath = "result/" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + "-"+stepSize + ".txt";
        // first step read uirt.txt
       // new LocationHandler().readFileByLines("source/uirt(3).txt", modeMap);
        new LocationHandler().readFileByLines("source/uirtAllLo.txt", modeMap,UIRTByBlockCountFilePath);
        System.out.println("the request count is : " + HttpClientExample.requestCount);
        System.out.println("横向分割数："+modeMap.get("lngDiffCount"));
        System.out.println("纵向分割数："+modeMap.get("latCount"));
    }

    public void readFileByLines(String fileName, Map<String, Double> modeMap,String UIRTByBlockCountFilePath) {
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
                        Double[] doubles = HttpClientExample.sendGet(address);//调用百度API
                        if(doubles[0].equals(0.0) && doubles[1].equals(0.0)){
                            addressMap.put(address,-1);
                            continue;
                        }
                        //删除异常数据
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
        String provinceName = "";//省
        String cityName = "";//市
        String countyName = "";//县
        String townName = "";//乡镇
        String villageName = "";//村
        String parkNameIndetailLocation = "";//景区名

        parkNameIndetailLocation = checkParkNameIndetailLocation(detailLocation);
        villageName = checkVillageName(detailLocation);
        townName = checkTownName(detailLocation);
        countyName = checkCountyName(detailLocation);
        cityName = checkCityName(detailLocation);
        provinceName = checkProvinceName(detailLocation);
//        if(parkNameIndetailLocation.equals("")){
//            countyName = checkCountyName(detailLocation);
//            if(countyName.equals("")){
//                cityName = checkCityName(detailLocation);
//                if(cityName.equals("")){
//                    provinceName = checkProvinceName(detailLocation);
//                }
//            }
//        }

        String address = provinceName + cityName + countyName + townName + villageName + parkNameIndetailLocation + parkName;
        System.out.println(address);
        return  address;
    }
    private Double calculateStepSizeLng(Double lat){
        //calculate lng step size(degree)
        Double radians = Math.toRadians(lat);//转成弧度
        Double cosValue = Math.cos(radians);
        Double lngKmPerDu = 111.314 * cosValue;//经度   一度所对应的长度（单位：km）
        Double stepSizeLng = stepSize / lngKmPerDu;//经度上步长（单位：度）
        return  stepSizeLng;
    }

    private Map<String, Double> getBlockMode(Double minLng, Double minLat, Double maxLng, Double maxLat){
        Double stepSizeLng = calculateStepSizeLng(minLat);//最小纬度下的经度步长

        //calculate lat step size(degree)
        Double stepSizeLat = stepSize / 110.95;//纬度步长，定值

        double latCount = Math.ceil((maxLat-minLat)/stepSizeLat);
        System.out.println("纵向分割个数："+latCount);
        //calculate rank count
        Double lngDiff = maxLng - minLng;
        double lngDiffCount = Math.ceil(lngDiff / stepSizeLng);//向上取整，横向块数
        System.out.println("横向分割个数："+lngDiffCount);

        Map<String, Double> resultMap = new HashMap<String, Double>();
        resultMap.put("minLng", minLng);
        resultMap.put("minLat", minLat);
        resultMap.put("maxLng", maxLng);
        resultMap.put("maxLat", maxLat);
        resultMap.put("stepSizeLat", stepSizeLat);//纬度步长
        resultMap.put("lngDiffCount", lngDiffCount);//横向块数
        resultMap.put("latCount", latCount);//纵向块数
        return resultMap;
    }

    private double getBlock(Double lng, Double lat, Map<String, Double> map){
        Double minLng = map.get("minLng");
        Double minLat = map.get("minLat");
        Double stepSizeLng = calculateStepSizeLng(lat);//计算经度的步长
        Double stepSizeLat = map.get("stepSizeLat");//纬度步长，定值
        Double lngDiffCount = map.get("lngDiffCount");

        double xCoordinate = Math.floor((lng - minLng) / stepSizeLng);//经度坐标
        double yCoordinate = Math.floor((lat - minLat) / stepSizeLat);//纬度坐标

        double blockCount = yCoordinate * lngDiffCount + xCoordinate;//块编码
        return blockCount;
    }
    private String checkTownName(String detailLocation){
        String townName = "";//乡镇
        if(detailLocation.contains("台怀镇")){
            townName = "山西省，忻州市，台怀镇，";
        }else if(detailLocation.contains("金村镇")){
            townName = "金村镇，";
        }else if(detailLocation.contains("风峪沟")){
            townName = "风峪沟，";
        }else if(detailLocation.contains("恒山")){
            townName = "大同市，浑源县，恒山，";
        }else if(detailLocation.contains("碛口古镇")){
            townName = "碛口古镇，";
        }else if(detailLocation.contains("晋祠镇")){
            townName = "晋祠镇，";
        }else if(detailLocation.contains("大黑山乡")){
            townName = "大黑山乡，";
        }else if(detailLocation.contains("成山镇")){
            townName = "成山镇，";
        }else if(detailLocation.contains("西营镇")){
            townName = "西营镇，";
        }else if(detailLocation.contains("柳埠乡")){
            townName = "柳埠乡，";
        }else if(detailLocation.contains("郑路镇")){
            townName = "郑路镇，";
        }else if(detailLocation.contains("归德镇")){
            townName = "归德镇，";
        }else if(detailLocation.contains("城关镇")){
            townName = "城关镇，";
        }else if(detailLocation.contains("芒山镇")){
            townName = "芒山镇，";
        }else if(detailLocation.contains("克井镇")){
            townName = "克井镇，";
        }else if(detailLocation.contains("尹店乡")){
            townName = "尹店乡，";
        }else if(detailLocation.contains("夏邑胡桥乡")){
            townName = "夏邑胡桥乡，";
        }else if(detailLocation.contains("侯岭乡")){
            townName = "侯岭乡，";
        }
        return  townName;
    }
    private String checkVillageName(String detailLocation){
        String villageName = "";//村
        if(detailLocation.contains("郭里口村")){
            villageName = "郭里口村，";
        }else if(detailLocation.contains("东水关街23号")){
            villageName = "东水关街23号，";
        }else if(detailLocation.contains("平遥古城")){
            villageName = "平遥古城，";
        }else if(detailLocation.contains("佛光新村")){
            villageName = "佛光新村，";
        }else if(detailLocation.contains("平遥东大街")){
            villageName = "平遥东大街，";
        }else if(detailLocation.contains("寺南庄村")){
            villageName = "寺南庄村，";
        }else if(detailLocation.contains("壶口瀑布")){
            villageName = "壶口瀑布，";
        }else if(detailLocation.contains("北恍村")){
            villageName = "北洸村，";
        }else if(detailLocation.contains("李家村")){
            villageName = "李家村，";
        }else if(detailLocation.contains("贾家庄村")){
            villageName = "贾家庄村，";
        }else if(detailLocation.contains("塞罕坝国家森林公园")){
            villageName = "塞罕坝国家森林公园，";
        }else if(detailLocation.contains("裕华路")){
            villageName = "裕华路，";
        }else if(detailLocation.contains("长寿山路")){
            villageName = "长寿山路，";
        }else if(detailLocation.contains("榆次老城")){
            villageName = "榆次老城，";
        }else if(detailLocation.contains("杨塔村")){
            villageName = "杨塔村，";
        }else if(detailLocation.contains("史壁村")){
            villageName = "临汾市，浮山县，史壁村，";
        }else if(detailLocation.contains("新广武村")){
            villageName = "新广武村，";
        }else if(detailLocation.contains("王郭村")){
            villageName = "王郭村，";
        }else if(detailLocation.contains("土岛村")){
            villageName = "土岛村，";
        }else if(detailLocation.contains("泰山")){
            villageName = "泰山，";
        }else if(detailLocation.contains("天外村")){
            villageName = "天外村，";
        }else if(detailLocation.contains("西霞口村")){
            villageName = "西霞口村，";
        }else if(detailLocation.contains("西关新四村")){
            villageName = "西关新四村，";
        }else if(detailLocation.contains("宁阳路")){
            villageName = "青岛，宁阳路，";
        }else if(detailLocation.contains("康成大街")){
            villageName = "康成大街，";
        }else if(detailLocation.contains("梁王冢村")){
            villageName = "梁王冢村，";
        }else if(detailLocation.contains("双乳村")){
            villageName = "双乳村，";
        }else if(detailLocation.contains("陈庄")){
            villageName = "陈庄，";
        }else if(detailLocation.contains("潭瀑峡")){
            villageName = "潭瀑峡，";
        }else if(detailLocation.contains("北社村")){
            villageName = "北社村，";
        }else if(detailLocation.contains("吴岗村")){
            villageName = "吴岗村，";
        }else if(detailLocation.contains("槐柏树街")){
            villageName = "槐柏树街12号，";
        }else if(detailLocation.contains("柿园村")){
            villageName = "柿园村，";
        }else if(detailLocation.contains("蔡楼村")){
            villageName = "蔡楼村，";
        }else if(detailLocation.contains("呼庄")){
            villageName = "呼庄，";
        }
        return  villageName;
}
    private String checkParkNameIndetailLocation(String detailLocation){
        String parkNameIndetailLocation = "";//景区
        if(detailLocation.contains("野三坡")&& detailLocation.contains("白草畔")){
            parkNameIndetailLocation = "野三坡，白草畔景区，";
        }else if(detailLocation.contains("野三坡")&& detailLocation.contains("百草畔")){
            parkNameIndetailLocation = "野三坡，百草畔景区，";
        } else if(detailLocation.contains("野三坡")&& detailLocation.contains("百里峡")){
            parkNameIndetailLocation = "野三坡，百里峡风景区，";
        }else if(detailLocation.contains("野三坡")&& detailLocation.contains("景区中部")){
            parkNameIndetailLocation = "野三坡，景区中部，";
        }else if(detailLocation.contains("野三坡")&& detailLocation.contains("龙门天关景区")){
            parkNameIndetailLocation = "野三坡，龙门天关景区，";
        }else if(detailLocation.contains("野三坡")&& detailLocation.contains("檀木沟景区")){
            parkNameIndetailLocation = "野三坡，檀木沟景区，";
        }else if(detailLocation.contains("野三坡")&& detailLocation.contains("鱼骨洞")){
            parkNameIndetailLocation = "野三坡，鱼骨洞景区，";
        }else if(detailLocation.contains("南台景区")){
            parkNameIndetailLocation = "南台景区，";
        }else if(detailLocation.contains("五台山风景名胜区")){
            parkNameIndetailLocation = "五台山风景名胜区，";
        }else if(detailLocation.contains("三台景区")){
            parkNameIndetailLocation = "三台景区，";
        }else if(detailLocation.contains("九龙岗景区")){
            parkNameIndetailLocation = "九龙岗景区，";
        }else if(detailLocation.contains("云冈石窟")){
            parkNameIndetailLocation = "山西省，大同市，云冈石窟景区，";
        }else if(detailLocation.contains("五台山")){
            parkNameIndetailLocation = "山西省，忻州市，五台山，";
        }else if(detailLocation.contains("绵山风景区")){
            parkNameIndetailLocation = "绵山风景区，";
        }else if(detailLocation.contains("绵山哀号坡")){
            parkNameIndetailLocation = "绵山哀号坡，";
        }else if(detailLocation.contains("绵山景区")){
            parkNameIndetailLocation = "绵山景区，";
        }else if(detailLocation.contains("介公岭")){
            parkNameIndetailLocation = "介休市，介公岭，";
        }else if(detailLocation.contains("太行山大峡谷")){
            parkNameIndetailLocation = "太行山大峡谷，";
        }else if(detailLocation.contains("大乳山休闲旅游度假区")){
            parkNameIndetailLocation = "大乳山休闲旅游度假区，";
        }else if(detailLocation.contains("五四广场")){
            parkNameIndetailLocation = "五四广场，";
        }else if(detailLocation.contains("桃花峪")){
            parkNameIndetailLocation = "泰山，";
        }else if(detailLocation.contains("瞻鲁台")){
            parkNameIndetailLocation = "瞻鲁台，";
        }else if(detailLocation.contains("开发区黄河路")){
            parkNameIndetailLocation = "烟台开发区黄河路，";
        }else if(detailLocation.contains("牡丹北路")){
            parkNameIndetailLocation = "菏泽，牡丹北路，";
        }else if(detailLocation.contains("状元街")){
            parkNameIndetailLocation = "淄博市，周村，状元街，";
        }else if(detailLocation.contains("南大街")){
            parkNameIndetailLocation = "烟台市，南大街，";
        }else if(detailLocation.contains("岱阴谷山北麓")){
            parkNameIndetailLocation = "岱阴谷山北麓，";
        }else if(detailLocation.contains("灵山大街")){
            parkNameIndetailLocation = "灵山大街，";
        }else if(detailLocation.contains("钟楼北路")){
            parkNameIndetailLocation = "蓬莱市，钟楼北路，";
        }else if(detailLocation.contains("灵山岛")){
            parkNameIndetailLocation = "青岛市，灵山岛，";
        }else if(detailLocation.contains("双忠祠街")){
            parkNameIndetailLocation = "济南市，历下区，";
        }else if(detailLocation.contains("汶河大道")){
            parkNameIndetailLocation = "汶河大道，";
        }else if(detailLocation.contains("兰山路28号")){
            parkNameIndetailLocation = "青岛市，市南区，兰山路28号，";
        }else if(detailLocation.contains("同心巷339")){
            parkNameIndetailLocation = "济宁市，同心巷339，";
        }else if(detailLocation.contains("龙门西山")){
            parkNameIndetailLocation = "洛阳市，龙门西山，";
        }else if(detailLocation.contains("云台山")){
            parkNameIndetailLocation = "云台山景区，";
        }else if(detailLocation.contains("少林寺")){
            parkNameIndetailLocation = "少林寺景区，";
        }else if(detailLocation.contains("白马寺")){
            parkNameIndetailLocation = "白马寺，";
        }else if(detailLocation.contains("白马寺齐云塔院")){
            parkNameIndetailLocation = "白马寺齐云塔院，";
        }else if(detailLocation.contains("宋都御街")){
            parkNameIndetailLocation = "宋都御街，";
        }else if(detailLocation.contains("淇水大道")){
            parkNameIndetailLocation = "淇水大道，";
        }else if(detailLocation.contains("222省道")){
            parkNameIndetailLocation = "鹤壁市，222省道，";
        }else if(detailLocation.contains("禹州登封和新密")){
            parkNameIndetailLocation = "郑州市，";
        }else if(detailLocation.contains("清丰出口")){
            parkNameIndetailLocation = "濮阳市，清丰县，";
        }else if(detailLocation.contains("卫辉大道")){
            parkNameIndetailLocation = "卫辉大道，";
        }else if(detailLocation.contains("南二环路")){
            parkNameIndetailLocation = "新乡市，南二环路，";
        }
        return parkNameIndetailLocation;
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
        }else if(detailLocation.contains("怀来")){
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
        }else if(detailLocation.contains("平山县")){
            countyName = "平山县，";
        }else if(detailLocation.contains("平遥县")){
            countyName = "平遥县，";
        }else if(detailLocation.contains("五台县")){
            countyName = "五台县，";
        }else if(detailLocation.contains("泽州县")){
            countyName = "泽州县，";
        }else if(detailLocation.contains("太谷县")){
            countyName = "太谷县，";
        }else if(detailLocation.contains("浑源县")){
            countyName = "浑源县，";
        }else if(detailLocation.contains("尖草坪区")){
            countyName = "尖草坪区，";
        }else if(detailLocation.contains("五台县")){
            countyName = "五台县，";
        }else if(detailLocation.contains("晋源区")){
            countyName = "晋源区，";
        }else if(detailLocation.contains("平定县")){
            countyName = "平定县，";
        }else if(detailLocation.contains("新绛县")){
            countyName = "新绛县，";
        }else if(detailLocation.contains("祁县")){
            countyName = "祁县，";
        }else if(detailLocation.contains("平遥县")){
            countyName = "平遥县，";
        }else if(detailLocation.contains("太谷县")){
            countyName = "太谷县，";
        }else if(detailLocation.contains("兴县")){
            countyName = "兴县，";
        }else if(detailLocation.contains("阳高县")){
            countyName = "阳高县，";
        }else if(detailLocation.contains("代县")){
            countyName = "代县，";
        }else if(detailLocation.contains("霍州")){
            countyName = "霍州，";
        }else if(detailLocation.contains("长岛县")){
            countyName = "长岛县，";
        }else if(detailLocation.contains("曲阜")){
            countyName = "曲阜，";
        }else if(detailLocation.contains("李沧区")){
            countyName = "李沧区，";
        }else if(detailLocation.contains("市南区")){
            countyName = "市南区，";
        }else if(detailLocation.contains("历下区")){
            countyName = "历下区，";
        }else if(detailLocation.contains("荣成市")){
            countyName = "荣成市，";
        }else if(detailLocation.contains("高密市")){
            countyName = "高密市，";
        }else if(detailLocation.contains("章丘")){
            countyName = "章丘市，";
        }else if(detailLocation.contains("历城区")){
            countyName = "历城区，";
        }else if(detailLocation.contains("商河县")){
            countyName = "商河县，";
        }else if(detailLocation.contains("长清县")){
            countyName = "长清县，";
        }else if(detailLocation.contains("永城")){
            countyName = "永城县，";
        }else if(detailLocation.contains("济源市")){
            countyName = "济源市，";
        }else if(detailLocation.contains("民权县")){
            countyName = "民权县，";
        }else if(detailLocation.contains("修武县")){
            countyName = "修武县，";
        }else if(detailLocation.contains("宣武区")){
            countyName = "宣武区，";
        }
        return countyName;
    }

    private String checkCityName(String detailLocation){
        String cityName = "";
        if(detailLocation.contains("张家口")){//河北
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
        }else if(detailLocation.contains("郑州")){//河南
            cityName = "郑州市，";
        }else if(detailLocation.contains("安阳")){
            cityName = "安阳市，";
        }else if(detailLocation.contains("鹤壁")){
            cityName = "鹤壁市，";
        }else if(detailLocation.contains("焦作")){
            cityName = "焦作市，";
        }else if(detailLocation.contains("开封")){
            cityName = "开封市，";
        }else if(detailLocation.contains("漯河")){
            cityName = "漯河市，";
        }else if(detailLocation.contains("洛阳")){
            cityName = "洛阳市，";
        }else if(detailLocation.contains("南阳")){
            cityName = "南阳市，";
        }else if(detailLocation.contains("平顶山")){
            cityName = "平顶山市，";
        }else if(detailLocation.contains("濮阳")){
            cityName = "濮阳市，";
        }else if(detailLocation.contains("三门峡")){
            cityName = "三门峡市，";
        }else if(detailLocation.contains("商丘")){
            cityName = "商丘市，";
        }else if(detailLocation.contains("新乡")){
            cityName = "新乡市，";
        }else if(detailLocation.contains("信阳")){
            cityName = "信阳市，";
        }else if(detailLocation.contains("许昌")){
            cityName = "许昌市，";
        }else if(detailLocation.contains("周口")){
            cityName = "周口市，";
        }else if(detailLocation.contains("驻马店")){
            cityName = "驻马店市，";
        }else if(detailLocation.contains("济南")){//山东
            cityName = "济南市，";
        }else if(detailLocation.contains("青岛")){
            cityName = "青岛市，";
        }else if(detailLocation.contains("滨州")){
            cityName = "滨州市，";
        }else if(detailLocation.contains("德州")){
            cityName = "德州市，";
        }else if(detailLocation.contains("东营")){
            cityName = "东营市，";
        }else if(detailLocation.contains("菏泽")){
            cityName = "菏泽市，";
        }else if(detailLocation.contains("济宁")){
            cityName = "济宁市，";
        }else if(detailLocation.contains("莱芜")){
            cityName = "莱芜市，";
        }else if(detailLocation.contains("聊城")){
            cityName = "聊城市，";
        }else if(detailLocation.contains("临沂")){
            cityName = "临沂市，";
        }else if(detailLocation.contains("日照")){
            cityName = "日照市，";
        }else if(detailLocation.contains("泰安")){
            cityName = "泰安市，";
        }else if(detailLocation.contains("潍坊")){
            cityName = "潍坊市，";
        }else if(detailLocation.contains("威海")){
            cityName = "威海市，";
        }else if(detailLocation.contains("烟台")){
            cityName = "烟台市，";
        }else if(detailLocation.contains("枣庄")){
            cityName = "枣庄市，";
        }else if(detailLocation.contains("淄博")){
            cityName = "淄博市，";
        }else if(detailLocation.contains("太原")){//山西
            cityName = "太原市，";
        }else if(detailLocation.contains("长治")){
            cityName = "长治市，";
        }else if(detailLocation.contains("大同")){
            cityName = "大同市，";
        }else if(detailLocation.contains("晋城")){
            cityName = "晋城市，";
        }else if(detailLocation.contains("晋中")){
            cityName = "晋中市，";
        }else if(detailLocation.contains("临汾")){
            cityName = "临汾市，";
        }else if(detailLocation.contains("吕梁")){
            cityName = "吕梁市，";
        }else if(detailLocation.contains("朔州")){
            cityName = "朔州市，";
        }else if(detailLocation.contains("忻州")){
            cityName = "忻州市，";
        }else if(detailLocation.contains("阳泉")){
            cityName = "阳泉市，";
        }else if(detailLocation.contains("运城")){
            cityName = "运城市，";
        }else if(detailLocation.contains("介休市")){
            cityName = "介休市，";
        }
        return  cityName;
    }
}

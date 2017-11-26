import packagetest.PackageTest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String SPAAT = " @";

    public static String getCurrentDate() {
        return formatter.format(new Date(System.currentTimeMillis()));
    }

    private static String getCaller() {
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        StackTraceElement callerStack = stackElements[stackElements.length >= 2 ? 2 : stackElements.length];
        String className = getLastPart(callerStack.getClassName());
        if (className.equals("Thread") || className.equals("MyThread")) {
            if (stackElements.length - 2 >= 0) {
                callerStack = stackElements[stackElements.length - 2];
                className = getLastPart(callerStack.getClassName());
            }
        }
        return new StringBuilder().append(SPAAT).append(className).append("_").append(callerStack.getMethodName()).append("():").append(callerStack.getLineNumber()).toString();
    }

    private static String getLastPart(String str) {
        if (str.contains(".")) {
            return str.substring(str.lastIndexOf(".") + 1);
        }
        return str;
    }

    public static void main(String[] args) {

        String a = "23743298";
        char[] aCharArray = a.toCharArray();
        for(int i = 0; i < a.length(); i ++){
            char aChar = aCharArray[i];
            int aInt = 1;
        }




        System.out.println("Hello World!");
//        for(int i = 0 ; i<10000; i++){
//            String log = new StringBuilder().append('[').append(getCurrentDate()).append(']').append("[DEBUG] ").append("the i is : " + i).append(" [").append(getCaller()).append(']').toString();
//            System.out.println(log);
//        }
        PackageTest packageTest = new PackageTest();

        for(int i = 0 ; i<100000; i++){
            //String log = new StringBuilder().append('[').append(getCurrentDate()).append(']').append("[DEBUG] ").append("the i is : " + i).append(" [").append(getCaller()).append(']').toString();
            String log = "["+getCurrentDate()+"]"+"[DEBUG]"+" the i is : "+i+" ["+getCaller()+"]";
            System.out.println(log);
        }
    }
}

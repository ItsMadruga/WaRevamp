package its.madruga.warevamp.app.core;


import its.madruga.warevamp.App;

public class Utils {
    public static String WHATSAPP_PACKAGE = "com.whatsapp";
    public static String WHATSAPP_WEB_PACKAGE = "com.whatsapp.w4b";

    public static boolean isInstalled(String packageWpp) {
        try {
            App.getInstance().getPackageManager().getPackageInfo(packageWpp, 0);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }
}

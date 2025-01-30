package its.madruga.warevamp.broadcast;

import its.madruga.warevamp.BuildConfig;

public class Events {
    public static final String ACTION_WA_REVAMP_CHECK = BuildConfig.APPLICATION_ID + ".WaRevampCheck";
    public static final String ACTION_WA_REVAMP_REBOOT = BuildConfig.APPLICATION_ID + ".WaRevampReboot";
    public static final String ACTION_WA_REVAMP_ENABLED = BuildConfig.APPLICATION_ID + ".WaRevampEnabled";
    public static final String ACTION_WA_REVAMP_NEED_REBOOT = BuildConfig.APPLICATION_ID + ".WaRevampNeedReboot";
    public static final String ACTION_WA_REVAMP_CLEAN_DATABASE = BuildConfig.APPLICATION_ID + ".WaRevampCleanDatabase";

    public static final String ACTION_WA_REVAMP_PERMISSIONS = BuildConfig.APPLICATION_ID + ".WaRevampPermissions";
}

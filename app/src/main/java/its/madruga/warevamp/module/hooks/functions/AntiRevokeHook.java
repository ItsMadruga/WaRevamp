package its.madruga.warevamp.module.hooks.functions;

import static its.madruga.warevamp.module.hooks.functions.CustomPrivacyHook.getCustomPref;
import static its.madruga.warevamp.module.references.ModuleResources.string.message_deleted;
import static its.madruga.warevamp.module.references.References.*;
import static its.madruga.warevamp.module.core.WppUtils.*;
import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.TextView;
import androidx.annotation.NonNull;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.BuildConfig;
import its.madruga.warevamp.module.core.FMessageInfo;
import its.madruga.warevamp.module.hooks.core.HooksBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

public class AntiRevokeHook extends HooksBase {
    private static HashSet<String> messageRevokedList = new HashSet<>();
    @SuppressLint("StaticFieldLeak")
    private static Activity mConversation;
    private static SharedPreferences mShared;

    public AntiRevokeHook(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
        mShared = mApp.getSharedPreferences(BuildConfig.APPLICATION_ID + "_antiRevoke", Activity.MODE_PRIVATE);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        String antiRevoke = prefs.getString("antiRevoke", "disable");
        String antiRevokeStatus = prefs.getString("antiRevokeStatus", "disable");


        XposedBridge.hookMethod(antiRevokeMethod(loader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                FMessageInfo fMessageInfo = new FMessageInfo(param.args[0]);
                FMessageInfo.Key key = fMessageInfo.getKey();
                String id = stripJID(getRawString(fMessageInfo.getKey().remoteJid));

                if ((!antiRevoke.equals("disable") || !antiRevokeStatus.equals("disable") || getCustomPref(id, "antiRevoke")) && !key.isFromMe) {
                    if (!antiRevoke(fMessageInfo).equals("disable")) param.setResult(true);
                } else super.beforeHookedMethod(param);
            }
        });


        XposedBridge.hookMethod(resumeConv(loader), hookConv());
        XposedBridge.hookMethod(startConv(loader), hookConv());

        XposedBridge.hookMethod(bubbleMethod(loader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                FMessageInfo fMessageInfo = new FMessageInfo(param.args[2]);
                TextView dateTextView = (TextView) param.args[1];
                isMRevoked(fMessageInfo, dateTextView, "antiRevoke");
            }
        });
        XposedBridge.hookMethod(unknownStatusPlaybackMethod(loader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                FMessageInfo fMessageInfo = new FMessageInfo(param.args[0]);
                Object obj = param.args[1];
                Object objView = statusPlaybackField(loader).get(obj);
                TextView dateTextView = (TextView) XposedHelpers.getObjectField(objView, "A0D");
                isMRevoked(fMessageInfo, dateTextView, "antiRevokeStatus");
            }
        });
    }

    private XC_MethodHook hookConv() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mConversation = (Activity) param.thisObject;
                setCurrentJid(stripJID(getRawString(getChatJid(mConversation))));
            }
        };
    }

    private void isMRevoked(FMessageInfo fMessageInfo, TextView dateTextView, String antiRevokeType) {
        if (dateTextView == null) return;
        messageRevokedList.clear();
        String messageKey = fMessageInfo.getKey().messageID;
        if (messageRevokedList.isEmpty()) {
            String[] currentRevokedMessages = getRevokedMessages(fMessageInfo);
            if (currentRevokedMessages == null) currentRevokedMessages = new String[]{""};
            Collections.addAll(messageRevokedList, currentRevokedMessages);
        }
        if (messageRevokedList != null && messageRevokedList.contains(messageKey)) {
            String antiRevokeValue = prefs.getString(antiRevokeType, "disable");
            if (antiRevokeValue.equals("disable") && getCustomPref(stripJID(getRawString(fMessageInfo.getKey().remoteJid)), "antiRevoke")) antiRevokeValue = "text";
            if (antiRevokeValue.equals("text")) {
                String newTextData = mApp.getString(message_deleted) + " | " + dateTextView.getText();
                dateTextView.setText(newTextData);
            } else if (antiRevokeValue.equals("icon")) {
                Drawable icon = mApp.getDrawable(getResourceId("ic_block_small", "drawable"));
                icon.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP));
                dateTextView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                dateTextView.setCompoundDrawablePadding(5);
            }
        } else {
            dateTextView.setCompoundDrawables(null, null, null, null);
            String revokeNotice = mApp.getString(message_deleted) + " | ";
            String dateText = dateTextView.getText().toString();
            if (dateText.contains(revokeNotice)) {
                dateTextView.setText(dateText.replace(revokeNotice, ""));
            }
        }
    }

    private String antiRevoke(FMessageInfo fMessageInfo) {
        String messageKey = (String) XposedHelpers.getObjectField(fMessageInfo.getObject(), "A01");
        String stripJID = stripJID(getRawString(fMessageInfo.getKey().remoteJid));
        String revokeBoolean = stripJID.equals("status") ? prefs.getString("antiRevokeStatus", "disable") : getCustomPref(stripJID, "antiRevoke") ? "text" : prefs.getString("antiRevoke", "disable");
        if (revokeBoolean.equals("disable")) return revokeBoolean;
        if (!messageRevokedList.contains(messageKey)) {
            try {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
                    saveRevokedMessage(stripJID, messageKey, fMessageInfo);
                    try {
                        if (mConversation != null && getCurrentJid().equals(stripJID)) {
                            if (mConversation.hasWindowFocus()) {
                                mConversation.startActivity(mConversation.getIntent());
                                mConversation.overridePendingTransition(0, 0);
                            } else {
                                mConversation.recreate();
                            }
                        }
                    } catch (Exception e) {
                        XposedBridge.log(e.getMessage());
                    }
                });
            } catch (Exception e) {
                XposedBridge.log(e.getMessage());
            }
        }
        return revokeBoolean;
    }

    private void saveRevokedMessage(String stripJID, String messageKey, FMessageInfo fMessageInfo) {
        String newRevokedMessages;
        String[] revokedMessagesArray = getRevokedMessages(fMessageInfo);
        if (revokedMessagesArray != null) {
            HashSet<String> newRevokedMessagesArray = new HashSet<>();
            Collections.addAll(newRevokedMessagesArray, revokedMessagesArray);
            newRevokedMessagesArray.add(messageKey);
            messageRevokedList = newRevokedMessagesArray;
            newRevokedMessages = Arrays.toString(newRevokedMessagesArray.toArray());
        } else {
            newRevokedMessages = "[" + messageKey + "]";
            messageRevokedList = new HashSet<>(Collections.singleton(messageKey));
        }
        mShared.edit().putString(stripJID + "_revoked", newRevokedMessages).apply();
    }

    private static String[] getRevokedMessages(FMessageInfo fMessageInfo) {
        String stripJID = stripJID(getRawString(fMessageInfo.getKey().remoteJid));
        try {
            String revokedsString = mShared.getString(stripJID + "_revoked", "");
            if (revokedsString.isEmpty()) {
                return null;
            } else return StringToStringArray(revokedsString);
        } catch (Exception e) {
            XposedBridge.log(e.getMessage());
            return null;
        }
    }

    private static void setCurrentJid(String jid) {
        if (jid == null || mShared == null) return;
        mShared.edit().putString("jid", jid).apply();
    }

    private static String getCurrentJid() {
        if (mShared == null) return "";
        else return mShared.getString("jid", "");
    }

    public static Object getChatJid(Object mConversation) throws Exception {
        return XposedHelpers.callMethod(mConversation, "getChatJid");
    }

}

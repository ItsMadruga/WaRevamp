package its.madruga.warevamp.module.hooks.customization;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import androidx.annotation.NonNull;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.module.core.databases.MsgstoreDatabase;
import its.madruga.warevamp.module.hooks.core.HooksBase;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;
import static its.madruga.warevamp.module.references.ModuleResources.string.tab_groups;
import static its.madruga.warevamp.module.references.References.*;
import static its.madruga.warevamp.module.references.ReferencesUtils.getFieldByExtendType;
import static its.madruga.warevamp.module.references.ReferencesUtils.getFieldByType;

public class SeparateGroupsHook extends HooksBase {
    public static final int CHATS = 200;
    public static final int STATUS = 300;
    public static final int CALLS = 400;
    public static final int COMMUNITY = 600;
    public static final int GROUPS = 700;
    public static ArrayList<Integer> tabs = new ArrayList<>();
    public static HashMap<Integer, Object> tabInstances = new HashMap<>();

    public SeparateGroupsHook(@NonNull @NotNull ClassLoader loader, @NonNull @NotNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        boolean filterChats = prefs.getBoolean("separateGroups", false);

        if (!filterChats) return;

        hookTabList();

        hookTabInstance();

        hookTabName();

        hookTabCount();

        hookTabIcon();
    }

    private void hookTabInstance() throws Exception {
        Class<?> cFrag = XposedHelpers.findClass("com.whatsapp.conversationslist.ConversationsFragment", loader);
        Method getTabMethod = getTabMethod(loader);
        Method methodTabInstance = tabFragmentMethod(loader);
        Constructor<?> recreateFragmentMethod = recreateFragmentConstructor(loader);

        XposedBridge.hookMethod(recreateFragmentMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var object = param.args[2];
                var desc = XposedHelpers.getObjectField(object, "A06");
                log(desc != null ? desc.toString() : "void");
                if (desc == null) return;
                var split = desc.toString().split(":");
                var id = 0;
                try {
                    id = Integer.parseInt(split[split.length - 1]);
                } catch (Exception ignored) {
                    return;
                }
                if (id == GROUPS || id == CHATS) {
                    var convFragment = XposedHelpers.getObjectField(param.thisObject, "A02");
                    tabInstances.remove(id);
                    tabInstances.put(id, convFragment);
                }
            }
        });

        XposedBridge.hookMethod(getTabMethod, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var tabId = ((Number) tabs.get((int) param.args[0])).intValue();
                if (tabId == GROUPS || tabId == CHATS) {
                    var convFragment = cFrag.newInstance();
                    param.setResult(convFragment);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var tabId = ((Number) tabs.get((int) param.args[0])).intValue();
                tabInstances.remove(tabId);
                tabInstances.put(tabId, param.getResult());
            }
        });

        XposedBridge.hookMethod(methodTabInstance, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var chatsList = (List<?>) param.getResult();
                var resultList = filterChat(param.thisObject, chatsList);
                param.setResult(resultList);
            }
        });

        XposedBridge.hookMethod(fabMethod(loader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Objects.equals(tabInstances.get(GROUPS), param.thisObject)) {
                    param.setResult(GROUPS);
                }
            }
        });

        Method publishResultsMethod = filtersMethod(loader);

        XposedBridge.hookMethod(publishResultsMethod, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var filters = param.args[1];
                var chatsList = (List<?>) XposedHelpers.getObjectField(filters, "values");
                var baseField = getFieldByExtendType(publishResultsMethod.getDeclaringClass(), BaseAdapter.class);
                if (baseField == null) return;
                var convField = getFieldByType(baseField.getType(), cFrag);
                Object thiz = convField.get(baseField.get(param.thisObject));
                if (thiz == null) return;
                var resultList = filterChat(thiz, chatsList);
                XposedHelpers.setObjectField(filters, "values", resultList);
                XposedHelpers.setIntField(filters, "count", resultList.size());
            }
        });
    }

    private void hookTabCount() throws Exception {
        Method enableCountMethod = enableCountTabMethod(loader);
        Constructor<?> constructor1 = enableCountTabConstructor1(loader);
        Constructor<?> constructor2 = enableCountTabConstructor2(loader);
        Constructor<?> constructor3 = enableCountTabConstructor3(loader);
        constructor3.setAccessible(true);

        XposedBridge.hookMethod(enableCountMethod, new XC_MethodHook() {
            @Override
            @SuppressLint({"Range", "Recycle"})
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int indexTab = (int) param.args[2];
                if (indexTab == tabs.indexOf(CHATS)) {

                    int chatCount = 0;
                    int groupCount = 0;
                    synchronized (SeparateGroupsHook.class) {
                        var db = MsgstoreDatabase.getInstance().getDatabase();
                        var sql = "SELECT * FROM chat WHERE unseen_message_count != 0";
                        var cursor = db.rawQuery(sql, null);
                        while (cursor.moveToNext()) {
                            int jid = cursor.getInt(cursor.getColumnIndex("jid_row_id"));
                            int groupType = cursor.getInt(cursor.getColumnIndex("group_type"));
                            int archived = cursor.getInt(cursor.getColumnIndex("archived"));
                            int chatLocked = cursor.getInt(cursor.getColumnIndex("chat_lock"));
                            if (archived != 0 || (groupType != 0 && groupType != 6) || chatLocked != 0)
                                continue;
                            var sql2 = "SELECT * FROM jid WHERE _id == ?";
                            var cursor1 = db.rawQuery(sql2, new String[]{String.valueOf(jid)});
                            if (!cursor1.moveToFirst()) continue;
                            var server = cursor1.getString(cursor1.getColumnIndex("server"));
                            if (server.equals("g.us")) {
                                groupCount++;
                            } else {
                                chatCount++;
                            }
                            cursor1.close();
                        }
                        cursor.close();
                    }
                    if (tabs.contains(CHATS) && tabInstances.containsKey(CHATS)) {
                        var instance12 = chatCount <= 0 ? constructor3.newInstance() : constructor2.newInstance(chatCount);
                        var instance22 = constructor1.newInstance(instance12);
                        param.args[1] = instance22;
                    }
                    if (tabs.contains(GROUPS) && tabInstances.containsKey(GROUPS)) {
                        var instance2 = groupCount <= 0 ? constructor3.newInstance() : constructor2.newInstance(groupCount);
                        var instance1 = constructor1.newInstance(instance2);
                        enableCountMethod.invoke(param.thisObject, param.args[0], instance1, tabs.indexOf(GROUPS));
                    }
                }
            }
        });
    }

    private void hookTabIcon() throws Exception {
        Method iconTabMethod = iconTabMethod(loader);
        Field iconField = iconTabField(loader);
        Field iconFrameField = iconTabLayoutField(loader);
        Field iconMenuField = iconMenuField(loader);

        XposedBridge.hookMethod(iconTabMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Class<?> superClass = param.thisObject.getClass().getSuperclass();
                Object field1;

                if (superClass != null && superClass == iconTabMethod.getDeclaringClass()) {
                    field1 = superClass.getDeclaredField(iconField.getName()).get(param.thisObject);
                } else {
                    Class<?> cls = param.thisObject.getClass();
                    Object field = cls.getDeclaredField("A00").get(param.thisObject);
                    field1 = iconField.get(field);

                }
                if (field1 == null) return;

                Object field2 = XposedHelpers.getObjectField(field1, iconFrameField.getName());
                if (field2 == null) return;
                Menu menu = (Menu) XposedHelpers.getObjectField(field2, iconMenuField.getName());
                if (menu == null) return;

                Drawable communityIcon = null;
                MenuItem communityMenu = menu.findItem(COMMUNITY);
                if (communityMenu != null) {
                    communityIcon = communityMenu.getIcon();
                }

                MenuItem menuItem = menu.findItem(GROUPS);
                if (menuItem != null && communityIcon != null) {
                    menuItem.setIcon(communityIcon);
                }
            }
        });
    }

    private void hookTabName() throws Exception {
        XposedBridge.hookMethod(tabNameMethod(loader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                var tab = (int) param.args[0];
                if (tab == GROUPS) {
                    param.setResult(mApp.getString(tab_groups));
                }
            }
        });
    }

    private void hookTabList() throws Exception {
        Class<?> home = XposedHelpers.findClass("com.whatsapp.HomeActivity", loader);
        Field fieldTabsList = Arrays.stream(home.getDeclaredFields()).filter(f -> f.getType().equals(List.class)).findFirst().orElse(null);
        if (fieldTabsList == null) {
            throw new NullPointerException("fieldTabList is NULL!");
        }
        fieldTabsList.setAccessible(true);

        XposedBridge.hookMethod(tabListMethod(loader), new XC_MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                tabs = (ArrayList<Integer>) fieldTabsList.get(null);
                if (tabs == null) return;
                if (!prefs.getBoolean("separateGroups", false)) return;
                if (!tabs.contains(GROUPS)) {
                    tabs.add(tabs.isEmpty() ? 0 : 1, GROUPS);
                }
            }
        });
    }

    private List<?> filterChat(Object obj, List<?> chatsList) {
        var tabChat = tabInstances.get(CHATS);
        var tabGroup = tabInstances.get(GROUPS);
        if (!Objects.equals(tabChat, obj) && !Objects.equals(tabGroup, obj)) {
            return chatsList;
        }
        var editableChatList = new ArrayListFilter(Objects.equals(tabGroup, obj));
        editableChatList.addAll(chatsList);
        return editableChatList;
    }

    public static class ArrayListFilter extends ArrayList<Object> {

        private final boolean isGroup;

        public ArrayListFilter(boolean isGroup) {
            this.isGroup = isGroup;
        }


        @Override
        public void add(int index, Object element) {
            if (checkGroup(element)) {
                super.add(index, element);
            }
        }

        @Override
        public boolean add(Object object) {
            if (checkGroup(object)) {
                return super.add(object);
            }
            return true;
        }

        @Override
        public boolean addAll(@NonNull Collection c) {
            for (var chat : c) {
                if (checkGroup(chat)) {
                    super.add(chat);
                }
            }
            return true;
        }

        private boolean checkGroup(Object chat) {
            var requiredServer = isGroup ? "g.us" : "s.whatsapp.net";
            var jid = XposedHelpers.getObjectField(chat, "A00");
            if (jid == null) jid = XposedHelpers.getObjectField(chat, "A01");
            if (jid == null) return true;
            if (XposedHelpers.findMethodExactIfExists(jid.getClass(), "getServer") != null) {
                var server = (String) XposedHelpers.callMethod(jid, "getServer");
                return server.equals(requiredServer);
            }
            return true;
        }
    }

}

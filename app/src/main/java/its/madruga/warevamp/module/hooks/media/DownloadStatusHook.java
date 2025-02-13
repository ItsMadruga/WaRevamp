package its.madruga.warevamp.module.hooks.media;

import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;
import static its.madruga.warevamp.module.references.ModuleResources.string.download_status;
import static its.madruga.warevamp.module.references.References.menuManagerClass;
import static its.madruga.warevamp.module.references.References.menuStatusClickMethod;

import android.media.MediaScannerConnection;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.module.core.FMessageInfo;
import its.madruga.warevamp.module.hooks.core.HooksBase;
import its.madruga.warevamp.module.references.ReferencesUtils;

public class DownloadStatusHook extends HooksBase {

    public DownloadStatusHook(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        if (!prefs.getBoolean("download_status", false)) return;

        Class<?> StatusPlaybackBaseFragmentClass = loader.loadClass("com.whatsapp.status.playback.fragment.StatusPlaybackBaseFragment");
        Class<?> StatusPlaybackContactFragmentClass = loader.loadClass("com.whatsapp.status.playback.fragment.StatusPlaybackContactFragment");
        Field listStatusField = ReferencesUtils.getFieldsByExtendType(StatusPlaybackContactFragmentClass, List.class).get(0);

        XposedBridge.hookMethod(menuStatusClickMethod(loader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<?> fieldObjects = Arrays.stream(param.method.getDeclaringClass().getDeclaredFields()).map(field -> ReferencesUtils.getObjectField(field, param.thisObject)).filter(Objects::nonNull).collect(Collectors.toList());


                Class<?> menuManagerClass = menuManagerClass(loader);
                Object menuManager = fieldObjects.stream().filter(menuManagerClass::isInstance).findFirst().orElse(null);
                Field menuField = ReferencesUtils.getFieldByExtendType(menuManagerClass, Menu.class);
                Menu menu = (Menu) ReferencesUtils.getObjectField(menuField, menuManager);
                Object fragmentInstance = fieldObjects.stream().filter(StatusPlaybackBaseFragmentClass::isInstance).findFirst().orElse(null);

                int index = (int) XposedHelpers.getObjectField(fragmentInstance, "A00");
                List<?> listStatus = (List<?>) listStatusField.get(fragmentInstance);
                FMessageInfo messageInfo = new FMessageInfo(XposedHelpers.getObjectField(listStatus.get(index), "A00"));

                File file = messageInfo.getMediaFile();
                MenuItem menuItem = menu.findItem(download_status);
                if (menuItem != null) return;

                menuItem = menu.add(0, download_status, 0, download_status);
                menuItem.setOnMenuItemClickListener(menuItem1 -> {
                    if (copyFile(file)) {
                        Toast.makeText(mApp, "Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mApp, "Error when saving, try again", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });

                super.afterHookedMethod(param);
            }
        });

    }

    private static boolean copyFile(File p) {
        if (p == null) return false;

        var destination = getPathDestination(p);

        try (FileInputStream in = new FileInputStream(p);
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] bArr = new byte[1024];
            while (true) {
                int read = in.read(bArr);
                if (read <= 0) {
                    in.close();
                    out.close();

                    String[] parts = destination.split("\\.");
                    String ext = parts[parts.length - 1].toLowerCase();

                    MediaScannerConnection.scanFile(mApp,
                            new String[]{destination},
                            new String[]{getMimeTypeFromExtension(ext)},
                            (path, uri) -> {});

                    return true;
                }
                out.write(bArr, 0, read);
            }
        } catch (IOException e) {
            XposedBridge.log(e.getMessage());
            return false;
        }
    }

    private static String getPathDestination(File f) {
        var filePath = f.getAbsolutePath();
        var isVideo = false;
        var isImage = false;
        var isAudio = false;

        String[] videoFormats = {
                "3gp", "mp4", "mkv", "avi", "wmv", "flv", "mov", "webm", "ts", "m4v", "divx", "xvid", "mpg", "mpeg", "mpg2", "ogv", "vob", "f4v", "asf"
        };

        String[] imageFormats = {
                "jpeg", "jpg", "png", "gif", "bmp", "webp", "heif", "tiff", "raw", "svg", "eps", "ai"
        };

        String[] audioFormats = {
                "mp3", "wav", "ogg", "m4a", "aac", "flac", "amr", "wma", "opus", "mid", "xmf", "rtttl", "rtx", "ota", "imy", "mpga", "ac3", "ec3", "eac3"
        };

        for (String format : videoFormats) {
            if (filePath.toLowerCase().endsWith("." + format)) {
                isVideo = true;
                break;
            }
        }

        for (String format : imageFormats) {
            if (filePath.toLowerCase().endsWith("." + format)) {
                isImage = true;
                break;
            }
        }

        for (String format : audioFormats) {
            if (filePath.toLowerCase().endsWith("." + format)) {
                isAudio = true;
                break;
            }
        }

        if (isVideo) {
            var folderPath = Environment.getExternalStorageDirectory() + "/Movies/WhatsApp/MdgWa Status/Status Videos/";
            var videoPath = new File(folderPath);
            if (!videoPath.exists()) videoPath.mkdirs();
            return videoPath.getAbsolutePath() + "/" + f.getName();
        } else if (isImage) {
            var folderPath = Environment.getExternalStorageDirectory() + "/Pictures/WhatsApp/MdgWa Status/Status Images/";
            var imagePath = new File(folderPath);
            if (!imagePath.exists()) imagePath.mkdirs();
            return imagePath.getAbsolutePath() + "/" + f.getName();
        } else if (isAudio) {
            var folderPath = Environment.getExternalStorageDirectory() + "/Music/WhatsApp/MdgWa Status/Status Sounds/";
            var audioPath = new File(folderPath);
            if (!audioPath.exists()) audioPath.mkdirs();
            return audioPath.getAbsolutePath() + "/" + f.getName();
        }
        return null;
    }

    public static String getMimeTypeFromExtension(String extension) {
        return switch (extension) {
            case "3gp", "mp4", "mkv", "avi", "wmv", "flv", "mov", "webm", "ts", "m4v", "divx",
                 "xvid", "mpg", "mpeg", "mpg2", "ogv", "vob", "f4v", "asf" -> "video/*";
            case "jpeg", "jpg", "png", "gif", "bmp", "webp", "heif", "tiff", "raw", "svg", "eps",
                 "ai" -> "image/*";
            case "mp3", "wav", "ogg", "m4a", "aac", "flac", "amr", "wma", "opus", "mid", "xmf",
                 "rtttl", "rtx", "ota", "imy", "mpga", "ac3", "ec3", "eac3" -> "audio/*";
            default -> "*/*";
        };
    }

}

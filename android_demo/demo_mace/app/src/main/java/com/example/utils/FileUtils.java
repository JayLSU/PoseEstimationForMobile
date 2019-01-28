package com.example.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    public static String DIR_NAME = "UsbWebCamera";
    private static final SimpleDateFormat mDateTimeFormat;
    public static float FREE_RATIO;
    public static float FREE_SIZE_OFFSET;
    public static float FREE_SIZE;
    public static float FREE_SIZE_MINUTE;
    public static long CHECK_INTERVAL;

    public FileUtils() {
    }

    public static final File getCaptureFile(Context context, String type, String ext, int save_tree_id) {
        return getCaptureFile(context, type, (String)null, ext, save_tree_id);
    }

    public static final File getCaptureFile(Context context, String type, String prefix, String ext, int save_tree_id) {
        File result = null;
        String file_name = (TextUtils.isEmpty(prefix) ? getDateTimeString() : prefix + getDateTimeString()) + ext;
        if (save_tree_id > 0 && SDUtils.hasStorageAccess(context, save_tree_id)) {
            result = SDUtils.createStorageDir(context, save_tree_id);
            if (result == null || !result.canWrite()) {
                Log.w(TAG, "なんでか書き込めん");
                result = null;
            }
        }

        if (result == null) {
            File dir = getCaptureDir(context, type, 0);
            if (dir != null) {
                dir.mkdirs();
                if (dir.canWrite()) {
                    result = dir;
                }
            }
        }

        if (result != null) {
            result = new File(result, file_name);
        }

        return result;
    }

    @SuppressLint({"NewApi"})
    public static final File getCaptureDir(Context context, String type, int save_tree_id) {
        File result = null;
        if (save_tree_id > 0 && SDUtils.hasStorageAccess(context, save_tree_id)) {
            result = SDUtils.createStorageDir(context, save_tree_id);
        }

        File dir = result != null ? result : new File(Environment.getExternalStoragePublicDirectory(type), DIR_NAME);
        dir.mkdirs();
        return dir.canWrite() ? dir : null;
    }

    public static final String getDateTimeString() {
        GregorianCalendar now = new GregorianCalendar();
        return mDateTimeFormat.format(now.getTime());
    }

    public static String getExternalMounts() {
        String externalpath = null;
        String internalpath = "";
        Runtime runtime = Runtime.getRuntime();

        try {
            Process proc = runtime.exec("mount");
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line;
            while((line = br.readLine()) != null) {
                if (!line.contains("secure") && !line.contains("asec")) {
                    String[] columns;
                    if (line.contains("fat")) {
                        columns = line.split(" ");
                        if (columns != null && columns.length > 1 && !TextUtils.isEmpty(columns[1])) {
                            externalpath = columns[1];
                            if (!externalpath.endsWith("/")) {
                                externalpath = externalpath + "/";
                            }
                        }
                    } else if (line.contains("fuse")) {
                        columns = line.split(" ");
                        if (columns != null && columns.length > 1) {
                            internalpath = internalpath.concat("[" + columns[1] + "]");
                        }
                    }
                }
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return externalpath;
    }

    public static final boolean checkFreeSpace(Context context, long max_duration, long start_time, int save_tree_id) {
        return context == null ? false : checkFreeSpace(context, FREE_RATIO, max_duration > 0L ? (float)(max_duration - (System.currentTimeMillis() - start_time)) / 60000.0F * FREE_SIZE_MINUTE + FREE_SIZE_OFFSET : FREE_SIZE, save_tree_id);
    }

    public static final boolean checkFreeSpace(Context context, float ratio, float minFree, int save_tree_id) {
        if (context == null) {
            return false;
        } else {
            boolean result = false;

            try {
                File dir = getCaptureDir(context, Environment.DIRECTORY_DCIM, save_tree_id);
                if (dir != null) {
                    float freeSpace = dir.canWrite() ? (float)dir.getUsableSpace() : 0.0F;
                    if (dir.getTotalSpace() > 0L) {
                        result = freeSpace / (float)dir.getTotalSpace() > ratio || freeSpace > minFree;
                    }
                }
            } catch (Exception var7) {
                Log.w("checkFreeSpace:", var7);
            }

            return result;
        }
    }

    public static final long getAvailableFreeSpace(Context context, String type, int save_tree_id) {
        long result = 0L;
        if (context != null) {
            File dir = getCaptureDir(context, type, save_tree_id);
            if (dir != null) {
                result = dir.canWrite() ? dir.getUsableSpace() : 0L;
            }
        }

        return result;
    }

    public static final float getFreeRatio(Context context, String type, int save_tree_id) {
        if (context != null) {
            File dir = getCaptureDir(context, type, save_tree_id);
            if (dir != null) {
                float freeSpace = dir.canWrite() ? (float)dir.getUsableSpace() : 0.0F;
                if (dir.getTotalSpace() > 0L) {
                    return freeSpace / (float)dir.getTotalSpace();
                }
            }
        }

        return 0.0F;
    }

    public static final String removeFileExtension(String path) {
        int ix = !TextUtils.isEmpty(path) ? path.lastIndexOf(".") : -1;
        return ix > 0 ? path.substring(0, ix) : path;
    }

    static {
        mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
        FREE_RATIO = 0.03F;
        FREE_SIZE_OFFSET = 2.097152E7F;
        FREE_SIZE = 3.145728E8F;
        FREE_SIZE_MINUTE = 4.194304E7F;
        CHECK_INTERVAL = 45000L;
    }
}

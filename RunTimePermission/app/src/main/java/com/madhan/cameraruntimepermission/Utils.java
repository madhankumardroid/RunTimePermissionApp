package com.madhan.cameraruntimepermission;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

public class Utils {
    private static final String TMP_FILE_NAME = "ImageFile";

    public static File getTempFilePath(Context context) {
        String fileFormat = ".JPEG";
        File file = new File(getExternalFolder(context), TMP_FILE_NAME + fileFormat);
        if (file.exists()) {
            file.delete();
        }
        file = new File(getExternalFolder(context), TMP_FILE_NAME + fileFormat);
        return file;
    }

    private static File getExternalFolder(Context context) {
        try {
            String state = Environment.getExternalStorageState();
            switch (state) {
                case Environment.MEDIA_MOUNTED:
                    File file = new File(Environment.getExternalStorageDirectory(),
                            context.getString(R.string.app_name));
                    file.mkdir();
                    return file;
                case Environment.MEDIA_MOUNTED_READ_ONLY:
                    Toast.makeText(context, "Can not write on external storage.",
                            Toast.LENGTH_LONG).show();
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

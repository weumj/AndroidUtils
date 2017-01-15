package mj.android.utils.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public final class ImageUtils {

    /**
     * Bitmap 을 파일에 저장하고, 이미지 저장 Broadcast 를 발송한다.
     *
     * @param isBitmapRecycle 작업이 끝난 후, 주어진 bitmap 에 Bitmap.recycle() 을 호출 할 건지 여부.
     * @return 이미지 저장 성공 여부
     */
    public static boolean saveImage(Context context, Bitmap bitmap, File file, boolean isBitmapRecycle) throws FileNotFoundException {
        try {
            boolean result = saveImage(bitmap, file);
            if (result) {
                sendBroadcastForNewImage(context, file);
                return true;
            } else
                return false;
        } finally {
            if (isBitmapRecycle)
                bitmap.recycle();
        }
    }

    /**
     * 이미지가 새로 저장되었음을 알리는 Broadcast 를 발송하여 갤러리 어플리케이션이 이미지를 불러올 수 있도록 한다.
     */
    private static void sendBroadcastForNewImage(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))));
        }
    }

    /**
     * Bitmap 을 파일에 저장한다.
     */
    public static boolean saveImage(final Bitmap bitmap, File savingFile) throws FileNotFoundException {
        return saveImage(bitmap, savingFile, Bitmap.CompressFormat.JPEG, 100);
    }

    /**
     * Bitmap 을 파일에 저장한다.
     */
    public static boolean saveImage(final Bitmap bitmap, File savingFile, Bitmap.CompressFormat format, int quality) throws FileNotFoundException {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(savingFile));
            return bitmap.compress(format, quality, bos);
        } finally {
            IOUtils.closeStream(bos);
        }
    }

    /**
     * View 로 부터 Bitmap 을 생성한다.
     */
    public static Bitmap createBitmapFromView(View v) throws IllegalArgumentException {

        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getWidth(), v.getHeight());
        v.draw(c);

        return b;
    }
}

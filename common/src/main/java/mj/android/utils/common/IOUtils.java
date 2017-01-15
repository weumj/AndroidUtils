package mj.android.utils.common;

import android.Manifest;
import android.content.Context;
import android.support.annotation.RequiresPermission;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class IOUtils {
    private IOUtils() {
    }

    /**
     * 주어진 디렉토리에서 디렉토리를 생성한다.
     */
    public static File mkDir(File directory, String dirName) throws IOException {
        File file = new File(directory, dirName);

        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new IOException("Fail to create a directory.");
            }
        }

        return file;
    }

    /**
     * 주어진 디렉토리에서 파일을 생성한다.
     */
    public static File mkFile(File directory, String fileName) throws IOException {
        File file = new File(directory, fileName);

        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Fail to create a file.");
            }
        }

        return file;
    }

    static void closeStream(Closeable close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static String readStringFile(FileInputStream fis) throws IOException {
        if (fis == null) {
            throw new NullPointerException("Cannot read String from null fileStream.");
        }
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(fis);
            byte[] bytes = new byte[bis.available()];
            int read = bis.read(bytes);
            if (read == -1) throw new IOException("unexpected EOF");
            return new String(bytes);
        } finally {
            closeStream(bis);
            closeStream(fis);
        }
    }


    private static <T> T readFileInternal(FileInputStream fis) throws IOException, ClassNotFoundException {
        if (fis == null) {
            throw new NullPointerException("Cannot read Object from null fileStream.");
        }
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
            //noinspection unchecked
            return (T) ois.readObject();
        } finally {
            closeStream(ois);
            closeStream(bis);
            closeStream(fis);
        }
    }

    /**
     * 내부저장소에 저장된 파일을 읽어온다.
     */
    public static <T> T readInternalFile(Context context, String fileName) throws IOException, ClassNotFoundException {
        return readFileInternal(context.openFileInput(fileName));
    }

    public static String readInternalStringFile(Context context, String fileName) throws IOException {
        return readStringFile(context.openFileInput(fileName));
    }

    /**
     * 외부저장소에 저장된 파일을 읽어온다.
     */
    //@RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public static <T> T readExternalFile(File file) throws IOException, ClassNotFoundException {
        return readFileInternal(new FileInputStream(file));
    }

    public static String readExternalStringFile(File file) throws IOException {
        return readStringFile(new FileInputStream(file));
    }

    private static void writeObject(FileOutputStream fos, Object obj) throws IOException {
        if (obj == null) {
            throw new NullPointerException("Cannot write null object.");
        }

        if (fos == null) {
            throw new NullPointerException("Cannot write object to null file stream.");
        }

        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
        } finally {
            closeStream(oos);
            closeStream(bos);
            closeStream(fos);
        }
    }

    private static void writeStringObject(FileOutputStream fos, String s) throws IOException {
        if (s == null) {
            throw new NullPointerException("Cannot write null String.");
        }

        if (fos == null) {
            throw new NullPointerException("Cannot write object to null file stream.");
        }

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(fos);
            bos.write(s.getBytes());
        } finally {
            closeStream(bos);
            closeStream(fos);
        }
    }

    /**
     * 외부 저장소 (ex : storage)에 주어진 이름으로 파일을 저장한다.
     *
     * @param obj 저장할 객체
     * @throws IOException
     */
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static void writeExternalFile(String fileName, Object obj) throws IOException {
        writeObject(new FileOutputStream(fileName), obj);
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static void writeExternalFile(String fileName, String obj) throws IOException {
        writeStringObject(new FileOutputStream(fileName), obj);
    }

    /**
     * 내부 저장소에 주어진 이름으로 파일을 저장한다.
     *
     * @param obj 저장할 객체
     * @throws IOException
     */
    public static void writeInternalFile(Context context, String fileName, Object obj) throws IOException {
        writeObject(context.openFileOutput(fileName, Context.MODE_PRIVATE), obj);
    }

    public static void writeInternalFile(Context context, String fileName, String obj) throws IOException {
        writeStringObject(context.openFileOutput(fileName, Context.MODE_PRIVATE), obj);
    }

    /**
     * 외부 저장소 (ex : storage)에서 해당 이름의 파일을 삭제한다.
     */
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static boolean deleteExternalFile(String fileName) {
        File file = new File(fileName);

        return !file.exists() || file.delete();

    }

    /**
     * 내부 저장소에서 해당 이름의 파일을 삭제한다.
     */
    public static boolean deleteInternalFile(Context context, String fileName) {
        return context.getApplicationContext().deleteFile(fileName);
    }

    public static byte[] toByteArray(Serializable obj) throws IOException {
        ObjectOutputStream objectOutput = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            objectOutput = new ObjectOutputStream(output);
            objectOutput.writeObject(obj);
            return output.toByteArray();
        } finally {
            closeStream(output);
            closeStream(objectOutput);
        }
    }

    public static <T extends Serializable> T fromByteArray(byte[] array) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(array);

        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            @SuppressWarnings("unchecked")
            T obj = (T) objectInputStream.readObject();
            return obj;
        } finally {
            closeStream(objectInputStream);
            closeStream(byteArrayInputStream);
        }
    }
}

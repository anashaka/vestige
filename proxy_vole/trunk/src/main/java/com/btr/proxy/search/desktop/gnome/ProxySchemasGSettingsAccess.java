package com.btr.proxy.search.desktop.gnome;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class ProxySchemasGSettingsAccess {

    private static void closeStream(Closeable c) {
        try {
            c.close();
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }

    static void copy(InputStream source, OutputStream dest) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int read = 0;
            while (read >= 0) {
                dest.write(buffer, 0, read);
                read = source.read(buffer);
            }
            dest.flush();
        } finally {
            closeStream(source);
            closeStream(dest);
        }
    }

    static {
        String arch = System.getProperty("os.arch");
        if (arch.equals("x86_64") || arch.equals("x64") || arch.equals("x86-64")) {
            arch = "amd64";
        }
        if (arch.equals("i386") || arch.equals("i686")) {
            arch = "x86";
        }
        String libName = "gsettings-" + arch + ".so";
        File libFile;
        try {
            InputStream source = ProxySchemasGSettingsAccess.class.getResourceAsStream("/lib/" + libName);
            libFile = File.createTempFile("gsettings", ".so");
            libFile.deleteOnExit();
            FileOutputStream destination = new FileOutputStream(libFile);
            copy(source, destination);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.load(libFile.getAbsolutePath());
    }

    public static native Map<String, Map<String, Object>> getValueByKeyBySchema();

}

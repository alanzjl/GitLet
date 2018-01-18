package gitlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.List;


class Utils {

    /* SHA-1 HASH VALUES. */

    /** Returns the SHA-1 hash of the concatenation of VALS, which may
     *  be any mixture of byte arrays and Strings. */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /** Returns the SHA-1 hash of the concatenation of the strings in
     *  VALS. */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /* FILE DELETION */

    /** Deletes FILE if it exists and is not a directory.  Returns true
     *  if FILE was deleted, and false otherwise.  Refuses to delete FILE
     *  and throws IllegalArgumentException unless the directory designated by
     *  FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /** Deletes the file named FILE if it exists and is not a directory.
     *  Returns true if FILE was deleted, and false otherwise.  Refuses
     *  to delete FILE and throws IllegalArgumentException unless the
     *  directory designated by FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /* READING AND WRITING FILE CONTENTS */

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    static Object readObject(File file) {
        byte[] bytes = readContents(file);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        Object res = null;
        try {
            in = new ObjectInputStream(bis);
            res = in.readObject();
        } catch (IOException ex) {
            System.out.println("read failed");
        } catch (ClassNotFoundException ex) {
            System.out.println("read failed");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                System.out.println("read failed");
            }
        }
        return res;
    }

    /** Write the entire contents of BYTES to FILE, creating or overwriting
     *  it as needed.  Throws IllegalArgumentException in case of problems. */
    static void writeContents(File file, byte[] bytes) {
        try {
            if (file.isDirectory()) {
                throw
                    new IllegalArgumentException("cannot overwrite directory");
            }
            Files.write(file.toPath(), bytes);
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    static void writeObject(File file, Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.flush();
            bytes = bos.toByteArray();
            writeContents(file, bytes);
        } catch (IOException ex) {
            System.out.println("write failed.");
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                System.out.println("write failed");
            }
        }
    }

    static byte[] objToByte(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.flush();
            bytes = bos.toByteArray();
            return bytes;
        } catch (IOException ex) {
            System.out.println("write failed.");
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                System.out.println("write failed");
            }
        }
        System.out.println("unnormal");
        return null;
    }

    /* DIRECTORIES */

    /** Filter out all but plain files. */
    private static final FilenameFilter PLAIN_FILES =
        new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        };

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    static DataBlobs findBlob(String index) {
        File dir = new File(BLOBSDIR);
        File[] files = dir.listFiles();
        for (File file : files) {
            String filename = file.toString();
            String[] names = filename.split("/");
            if (names[names.length - 1].equals(index)) {
                DataBlobs blob = (DataBlobs) readObject(file);
                return blob;
            }
        }
        return null;
    }

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    static boolean fileExists(String filename) {
        File tmpDir = new File(filename);
        return tmpDir.exists() & tmpDir.isFile();
    }
    static boolean dirExists(String filename) {
        File tmpDir = new File(filename);
        return tmpDir.exists() & tmpDir.isDirectory();
    }

    static boolean dbExists() {
        return dirExists(DATABASE);
    }
    static String timeGen() {
        return new SimpleDateFormat("EEE MMM d "
                + "HH:mm:ss yyyy Z").format(new Date());
    }

    static String initialTime() {
        return new SimpleDateFormat("EEE MMM "
                + "d HH:mm:ss yyyy Z").format(new Date(0));
    }

    static final String DATABASE = ".gitlet";
    static final String COMMITSDIR = DATABASE + "/commits";
    static final String BLOBSDIR = DATABASE + "/blobs";
    static final String BRANCHREC = DATABASE + "/branch.db";
    static final String CURBRANCH = DATABASE + "/currentbranch.db";
    static final String CURRENTCM = DATABASE + "/current.db";
    static final String STAGINGDIR = DATABASE + "/staging";
}

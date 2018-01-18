package gitlet;


import java.io.File;

import static java.lang.System.exit;

/**
 * gitlet add FILENAME
 * @author Jialiang
 */
public class Add implements GitBody {
    private String filename;

    public boolean check(String ... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return false;
        }
        filename = args[1];
        return true;
    }

    @Override
    public void run(DataBase db) {
        if (!Utils.fileExists((filename))) {
            System.out.println("File does not exist.");
            exit(0);
        }
        File src = new File(filename);
        DataBlobs tmp = new DataBlobs();
        tmp.setfile(filename, src);

        DataCommits cm = db.getcurrentCm();
        for (MyFiles blob : cm.getblobs()) {
            if (blob.index().equals(tmp.index())
                    && blob.filename().equals(tmp.getfilename())) {
                File dir = new File(Utils.STAGINGDIR);
                File[] files = dir.listFiles();
                for (File file : files) {
                    DataBlobs blb = (DataBlobs) Utils.readObject(file);
                    if (blb.index().equals(tmp.index())) {
                        file.delete();
                        break;
                    }
                }
                return;
            }
        }


        tmp.setStaged(true, 1);
        File dest = new File(Utils.STAGINGDIR
                + "/" + tmp.index());
        Utils.writeObject(dest, tmp);
    }
}

package gitlet;

import java.io.File;

import static java.lang.System.exit;

/**
 * git rm
 * @author Jialiang
 */
public class Rm implements GitBody {
    private String _filename;

    public boolean check(String ... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return false;
        }
        _filename = args[1];
        return true;
    }

    @Override
    public void run(DataBase db) {
        boolean legal = false;
        File stageDir = new File(Utils.STAGINGDIR);
        File[] staged = stageDir.listFiles();
        for (File file : staged) {
            DataBlobs blob = (DataBlobs) Utils.readObject(file);
            if (blob.getfilename().equals(_filename)
                    && blob.isstaged()
                    && blob.getstageState() == 1) {
                file.delete();
                legal = true;
                break;
            }
        }

        for (MyFiles blobindex : db.getcurrentCm().getblobs()) {
            if (blobindex.filename().equals(_filename)) {
                DataBlobs blob = Utils.findBlob(blobindex.index());
                DataBlobs tmp = blob.copy();
                tmp.setStaged(true, 2);
                File dest = new File(Utils.STAGINGDIR
                        + "/" + tmp.index());
                Utils.writeObject(dest, tmp);

                File file = new File(_filename);
                if (file.exists()) {
                    file.delete();
                }
                legal = true;
            }
        }

        if (!legal) {
            System.out.println("No reason to remove the file.");
            exit(0);
        }
    }
}

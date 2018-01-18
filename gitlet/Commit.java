package gitlet;


import java.io.File;
import java.util.ArrayList;

import static java.lang.System.exit;

/**
 * gitlet commit
 * @author Jialiang
 */
public class Commit implements GitBody {
    private String _message;
    private String _time;
    private boolean hasSecPar;

    private String secPar;

    public Commit() {
        hasSecPar = false;
        secPar = "";
    }

    public Commit(String ssecPar) {
        this.secPar = ssecPar;
        hasSecPar = true;
    }

    public boolean check(String ... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return false;
        }
        _message = args[1];
        return true;
    }

    @Override
    public void run(DataBase db) {
        if (_message.length() == 0) {
            System.out.println("Please enter a commit message.");
            exit(0);
        }
        File stageDir = new File(Utils.STAGINGDIR);
        File[] files = stageDir.listFiles();
        if (files.length == 0) {
            System.out.println("No changes added to the commit.");
            exit(0);
        }
        DataCommits tmp = new DataCommits(_message, db.getcurrentCm());
        tmp.setbranch(db.getcurBranch().getName());
        if (hasSecPar) {
            tmp.setparentSecond(secPar);
        }
        for (File file : files) {
            DataBlobs staged = (DataBlobs) Utils.readObject(file);
            if (staged.getstageState() == 1) {
                boolean found = false;
                for (int i = 0; i < tmp.getblobs().size(); i++) {
                    MyFiles oldFile = tmp.getblobs().get(i);
                    if (oldFile.filename().equals(staged.getfilename())) {
                        MyFiles nFile = new MyFiles(oldFile.filename(),
                                staged.index(), oldFile.version() + 1);
                        ArrayList<MyFiles> htmp = tmp.getblobs();
                        htmp.set(i, nFile);
                        tmp.setBlobs(htmp);
                        found = true;
                    }
                }
                if (!found) {
                    ArrayList<MyFiles> htmp = tmp.getblobs();
                    htmp.add(new MyFiles(staged, 1));
                    tmp.setBlobs(htmp);
                }
                staged.clearStage();
                File newPath = new File(Utils.BLOBSDIR + "/" + staged.index());
                Utils.writeObject(newPath, staged);
            } else if (staged.getstageState() == 2) {
                for (int i = 0; i < tmp.getblobs().size(); i++) {
                    String cmp = tmp.getblobs().get(i).filename();
                    if (cmp.equals(staged.getfilename())) {
                        tmp.getblobs().remove(i);
                        break;
                    }
                }

            }
        }
        for (File file : files) {
            file.delete();
        }
        tmp.write();
        db.setcurrentCm(tmp);
        db.updateBranch(tmp.getbranch(), tmp);
        db.writeCmt();
    }

}

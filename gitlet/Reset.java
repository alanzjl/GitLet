package gitlet;


import java.io.File;
import java.util.ArrayList;

import static java.lang.System.exit;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Jialiang
 */
public class Reset implements GitBody {
    private String _commitID;

    public boolean check(String ... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return false;
        }
        _commitID = args[1];
        return true;
    }

    @Override
    public void run(DataBase db) {
        File tmp = null;
        if (_commitID.length() < db.getcurrentCm().index().length()) {
            File tmpp = new File(Utils.COMMITSDIR);
            File[] files = tmpp.listFiles();
            for (File file : files) {
                String[] names = file.toString().split("/");
                String name = names[names.length - 1];
                if (name.startsWith(_commitID)) {
                    tmp = file;
                    break;
                }
            }
        } else {
            tmp = new File(Utils.COMMITSDIR + "/"  + _commitID);
        }
        if (tmp == null || !tmp.exists()) {
            System.out.println("No commit with that id exists.");
            exit(0);
        }
        DataCommits cmt = (DataCommits) Utils.readObject(tmp);
        ArrayList<MyFiles> trackedFiles = db.getcurrentCm().getblobs();
        ArrayList<MyFiles> destFiles = cmt.getblobs();
        ArrayList<MyFiles> curFiles = new ArrayList<>();
        File tmpfile = new File("./");
        File[] tmpfiles = tmpfile.listFiles();
        for (File tmpFile : tmpfiles) {
            String[] names = tmpFile.toString().split("/");
            String name = names[names.length - 1];
            if (tmpFile.isDirectory()) {
                continue;
            }
            curFiles.add(new MyFiles(name,
                    Utils.sha1(Utils.readContents(tmpFile))));
        }
        delt(trackedFiles, destFiles);
        for (MyFiles tfile : destFiles) {
            if (Checkout.containsName(trackedFiles, tfile)
                    || !Checkout.containsName(curFiles, tfile)) {
                tmp = new File(Utils.BLOBSDIR + "/" + tfile.index());
                DataBlobs blb = (DataBlobs) Utils.readObject(tmp);
                tmp = new File(tfile.filename());
                Utils.writeContents(tmp, blb.getfile());
            } else {
                System.out.println("There is an untracked file in the "
                        + "way; delete it or add it first.");
                exit(0);
            }
        }
        clearstage();
        String branch = db.getcurBranch().getName();
        for (DataBranch br : db.getbranches()) {
            if (br.getName().equals(branch)) {
                br.setPointer(cmt);
            }
        }
        db.setcurrentCm(cmt);
        db.writeCmt();
    }

    public void delt(ArrayList<MyFiles> trackedFiles,
                     ArrayList<MyFiles> destFiles) {
        for (MyFiles tmpf : trackedFiles) {
            if (!Checkout.containsName(destFiles, tmpf)) {
                File tm = new File(tmpf.filename());
                tm.delete();
            }
        }
    }

    public void clearstage() {
        File stage = new File(Utils.STAGINGDIR);
        File[] files = stage.listFiles();
        for (File file : files) {
            file.delete();
        }
    }
}

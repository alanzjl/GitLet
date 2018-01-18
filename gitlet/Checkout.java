package gitlet;

import java.io.File;
import java.util.ArrayList;
import static java.lang.System.exit;

/**
 * gitlet checkout 
 * @author Jialiang
 */
public class Checkout implements GitBody {
    private String _mode;

    private String _branch, _filename, _commitID;

    public boolean check(String... args) {
        if (args.length == 3 && !args[1].equals("--")) {
            System.out.println("Incorrect operands.");
            exit(0);
        }
        if (args.length == 4 && !args[2].equals("--")) {
            System.out.println("Incorrect operands.");
            exit(0);
        }
        switch (args.length) {
        case 2:
            _mode = "Branch";
            _branch = args[1];
            break;
        case 3:
            _mode = "Filename";
            _filename = args[2];
            break;
        case 4:
            _mode = "Commit";
            _commitID = args[1];
            _filename = args[3];
            break;
        default:
            System.out.println("Incorrect operands.");
            return false;
        }
        return true;
    }

    @Override
    public void run(DataBase db) {
        if (_mode.equals("Filename")) {
            String branch = db.getcurBranch().getName();
            DataCommits cmt = null;
            for (DataBranch br : db.getbranches()) {
                if (br.getName().equals(branch)) {
                    File tmp = new File(Utils.COMMITSDIR
                            + "/" + br.getPointer());
                    cmt = (DataCommits) Utils.readObject(tmp);
                }
            }
            replaceFileCommit(cmt);
        } else if (_mode.equals("Commit")) {
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
            replaceFileCommit(cmt);
        } else if (_mode.equals("Branch")) {
            branchCond(db);
        }
    }

    public void branchCond(DataBase db) {
        ArrayList<DataBranch> branches = db.getbranches();
        DataBranch destBranch = null;
        for (DataBranch branch : branches) {
            if (branch.getName().equals(_branch)) {
                destBranch = branch;
            }
        }
        if (destBranch == null) {
            System.out.println("No such branch exists.");
            exit(0);
        }
        if (_branch.equals(db.getcurBranch().getName())) {
            System.out.println("No need to checkout the current branch.");
            exit(0);
        }
        ArrayList<MyFiles> curname = findcurName();
        ArrayList<MyFiles> tracked = db.getcurrentCm().getblobs();
        DataCommits destcmt = null;
        File tmp = new File(Utils.COMMITSDIR
                + "/" + destBranch.getPointer());
        destcmt = (DataCommits) Utils.readObject(tmp);
        ArrayList<MyFiles> desttracked = destcmt.getblobs();
        checker(db, curname, tracked, desttracked, destcmt);
    }

    public ArrayList<MyFiles> findcurName() {
        File cur = new File("./");
        File[] files = cur.listFiles();
        ArrayList<MyFiles> curname = new ArrayList<>();
        for (File file: files) {
            String[] names = file.toString().split("/");
            String name = names[names.length - 1];
            if (name.equals(Utils.DATABASE)) {
                continue;
            } else if (name.equals(".")) {
                continue;
            } else if (name.equals("..")) {
                continue;
            }
            if (file.isDirectory()) {
                continue;
            }
            String sha = Utils.sha1(Utils.readContents(file), name);
            curname.add(new MyFiles(name, sha, 0));
        }
        return curname;
    }

    private void checker(DataBase db, ArrayList<MyFiles> cur,
                         ArrayList<MyFiles> tracked,
                         ArrayList<MyFiles> dest, DataCommits cmt) {
        for (MyFiles file : cur) {
            if (!containsName(tracked, file)) {
                if (containsChange(dest, file)) {
                    System.out.println("There is an untracked file "
                            + "in the way; delete it or add it first.");
                    exit(0);
                }
            }
        }
        for (MyFiles file : dest) {
            File tmp = new File(Utils.BLOBSDIR + "/" + file.index());
            DataBlobs data = (DataBlobs) Utils.readObject(tmp);
            File target = new File(data.getfilename());
            if (target.exists()) {
                target.delete();
            }
            Utils.writeContents(target, data.getfile());
        }
        for (MyFiles file : tracked) {
            boolean sameName = false;
            boolean sameCon = false;
            for (MyFiles tmp : dest) {
                if (tmp.filename().equals(file.filename())) {
                    continue;
                }
            }
            if (!contains(dest, file)) {
                File tmp = new File(file.filename());
                if (Utils.sha1(Utils.readContents(tmp)).equals(file.index())) {
                    tmp.delete();
                }
            }
            if (!containsName(dest, file)) {
                File tmp = new File(file.filename());
                tmp.delete();
            }
        }
        db.setcurrentCm(cmt);
        db.setcurBranch(new DataBranch(_branch, cmt));
        File tmp = new File(Utils.STAGINGDIR);
        File[] files = tmp.listFiles();
        for (File file : files) {
            file.delete();
        }
        db.writeCmt();
    }

    private boolean contains(ArrayList<MyFiles> in, MyFiles f) {
        for (MyFiles tmp : in) {
            if (tmp.filename().equals(f.filename())
                    && tmp.index().equals(f.index())) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsName(ArrayList<MyFiles> in, MyFiles f) {
        for (MyFiles tmp : in) {
            if (tmp.filename().equals(f.filename())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsChange(ArrayList<MyFiles> in, MyFiles f) {
        for (MyFiles tmp : in) {
            if (tmp.filename().equals(f.filename())
                    && !tmp.index().equals(f.index())) {
                return true;
            }
        }
        return false;
    }

    public void replaceFileCommit(DataCommits cmt) {
        for (MyFiles blob : cmt.getblobs()) {
            if (blob.filename().equals(_filename)) {
                File tmp = new File(Utils.BLOBSDIR + "/" + blob.index());
                DataBlobs blb = (DataBlobs) Utils.readObject(tmp);
                byte[] raw = blb.getfile();
                File cur = new File(blob.filename());
                if (cur.exists()) {
                    cur.delete();
                }
                Utils.writeContents(cur, raw);
                return;
            }
        }
        System.out.println("File does not exist in that commit.");
        exit(0);
    }
}

package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * git status
 * @author Jialiang
 */
public class Status implements GitBody {

    public boolean check(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return false;
        }
        return true;
    }

    @Override
    public void run(DataBase db) {
        System.out.println("=== Branches ===");
        DataCommits cmt = db.getcurrentCm();
        ArrayList<DataBranch> branches = db.getbranches();
        ArrayList<String> bname = new ArrayList<>();
        for (DataBranch branch : branches) {
            bname.add(branch.getName());
        }
        Collections.sort(bname);
        for (String br : bname) {
            if (br.equals(cmt.getbranch())) {
                System.out.println("*" + br);
            } else {
                System.out.println(br);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        File filesDir = new File(Utils.STAGINGDIR);
        File[] files = filesDir.listFiles();
        ArrayList<String> sname = new ArrayList<>();
        ArrayList<String> dname = new ArrayList<>();
        for (File file : files) {
            DataBlobs blob = (DataBlobs) Utils.readObject(file);
            if (blob.getstageState() == 1) {
                sname.add(blob.getfilename());
            } else if (blob.getstageState() == 2) {
                dname.add(blob.getfilename());
            }
        }
        Collections.sort(sname);
        for (String br : sname) {
            System.out.println(br);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Collections.sort(dname);
        for (String br : dname) {
            System.out.println(br);
        }
        File cur = new File("./");
        files = cur.listFiles();
        ArrayList<String> curname = new ArrayList<>();
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
            curname.add(name);
        }
        System.out.println();
        mns(sname, dname, curname, cmt);
    }

    public void mns(ArrayList<String> staged, ArrayList<String> deleted,
                    ArrayList<String> cur, DataCommits cmt) {
        System.out.println("=== Modifications Not Staged For Commit ===");
        ArrayList<String> res = new ArrayList<>();
        ArrayList<String> untracked = new ArrayList<>();
        for (String curname : cur) {
            if (!staged.contains(curname)) {
                for (MyFiles blob : cmt.getblobs()) {
                    if (blob.filename().equals(curname)) {
                        File tmp = new File(curname);
                        if (!Utils.sha1(Utils.readContents(tmp),
                                curname).equals(blob.index())) {
                            res.add(curname + " (modified)");
                        }
                        break;
                    }
                }
            } else {
                String curindex = getIndex(curname);
                File file = new File(Utils.STAGINGDIR + "/" + curname);
                if (file.exists()) {
                    String sindex = getIndex(Utils.STAGINGDIR + "/" + curname);
                    if (!curindex.equals(sindex)) {
                        res.add(curname + " (modified)");
                    }
                }
            }
            if (!staged.contains(curname)) {
                boolean flag = true;
                for (MyFiles blob : cmt.getblobs()) {
                    if (blob.filename().equals(curname)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    untracked.add(curname);
                }
            }
        }
        for (String name : staged) {
            if (!cur.contains(name)) {
                res.add(name + " (deleted)");
            }
        }
        for (MyFiles file : cmt.getblobs()) {
            String name = file.filename();
            if (!cur.contains(name)) {
                if (!deleted.contains(name)) {
                    res.add(name + " (deleted)");
                }
            }
        }
        sandp(res);
        System.out.println();
        System.out.println("=== Untracked Files ===");
        sandp(untracked);
        System.out.println();
    }

    public void sandp(ArrayList<String> in) {
        Collections.sort(in);
        for (String s : in) {
            System.out.println(s);
        }
    }

    public String getIndex(String in) {
        File tmp = new File(in);
        return Utils.sha1(Utils.readContents(tmp));
    }
}

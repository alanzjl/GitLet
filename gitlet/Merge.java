package gitlet;

import java.io.File;
import java.util.ArrayList;

import static java.lang.System.exit;

public class Merge implements GitBody {
    private String _branch;

    public boolean check(String ... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return false;
        }
        _branch = args[1];
        return true;
    }

    @Override
    public void run(DataBase db) {
        String curBranchName = db.getcurBranch().getName();
        DataBranch curBr = match(db, curBranchName);
        DataBranch destBr = match(db, _branch);
        if (destBr == null) {
            System.out.println("A branch with that name does not exist.");
            exit(0);
        }
        File tmp = new File(Utils.STAGINGDIR);
        if (tmp.listFiles().length > 0) {
            System.out.println("You have uncommitted changes.");
            exit(0);
        }
        if (curBranchName.equals(_branch)) {
            System.out.println("Cannot merge a branch with itself.");
            exit(0);
        }

        ArrayList<DataCommits> hisDest = history(destBr);
        ArrayList<DataCommits> hisSelf = history(curBr);
        DataCommits split = findSplit(hisSelf, hisDest);
        if (split.index().equals(hisDest.get(0).index())) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            exit(0);
        }
        if (split.index().equals(hisSelf.get(0).index())) {
            db.setcurrentCm(hisDest.get(0));
            db.writeCmt();
            System.out.println("Current branch fast-forwarded.");
            exit(0);
        }
        ArrayList<MyFiles> splitFiles = split.getblobs();
        ArrayList<MyFiles> currentFiles = hisSelf.get(0).getblobs();
        ArrayList<MyFiles> destFiles = hisDest.get(0).getblobs();
        for (MyFiles file : destFiles) {
            boolean flag = false;
            for (MyFiles sfile : splitFiles) {
                if (flag) {
                    break;
                }
                if (file.filename().equals(sfile.filename())
                        && !file.index().equals(sfile.filename())) {
                    for (MyFiles cfile : currentFiles) {
                        if (flag) {
                            break;
                        }
                        if (file.filename().equals(cfile.filename())
                                && cfile.index().equals(sfile.index())) {
                            flag = true;
                            checkoutstage(file.index(), currentFiles);
                        }
                    }
                }
            }
        }
        run2(splitFiles, currentFiles, destFiles, hisDest, db,
                destBr, curBranchName);
    }

    public DataCommits findSplit(ArrayList<DataCommits> hisSelf,
                                  ArrayList<DataCommits> hisDest) {
        DataCommits split = null;
        for (DataCommits cmt : hisSelf) {
            if (split != null) {
                break;
            }
            for (DataCommits cmt2 : hisDest) {
                if (cmt.index().equals(cmt2.index())) {
                    split = cmt;
                    break;
                }
            }
        }
        return split;
    }

    public void run2(ArrayList<MyFiles> splitFiles,
                     ArrayList<MyFiles> currentFiles,
                     ArrayList<MyFiles> destFiles,
                     ArrayList<DataCommits> hisDest,
                     DataBase db, DataBranch destBr,
                     String curBranchName) {
        applyRule2(splitFiles, currentFiles, destFiles);
        applyRule3(splitFiles, currentFiles, destFiles);
        boolean conf = false;
        conf = conflict(splitFiles, currentFiles, destFiles);
        GitBody cm = new Commit(hisDest.get(0).index());
        String[] agr = {"commit", "Merged " + destBr.getName()
                + " into " + curBranchName + "."};
        cm.check(agr);
        cm.run(db);
        if (conf) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public boolean checkUntracked(ArrayList<MyFiles> curFiles,
                                  String name) {
        File tmp = new File(name);
        if (tmp.exists() && !containsFilename(curFiles, name)) {
            System.out.println("There is an untracked file in "
                    + "the way; delete it or add it first.");
            exit(0);
        }
        return true;
    }

    public void applyRule2(ArrayList<MyFiles> splitFiles,
                           ArrayList<MyFiles> curFiles,
                           ArrayList<MyFiles> destFiles) {
        for (MyFiles dfile : destFiles) {
            if (containsFilename(splitFiles, dfile.filename())) {
                continue;
            }
            checkoutstage(dfile.index(), curFiles);
        }
    }

    public void applyRule3(ArrayList<MyFiles> splitFiles,
                           ArrayList<MyFiles> curFiles,
                           ArrayList<MyFiles> destFiles) {
        for (MyFiles sfile : splitFiles) {
            if (containsFilename(destFiles, sfile.filename())) {
                continue;
            }
            for (MyFiles cfile : curFiles) {
                if (cfile.filename().equals(sfile.filename())
                        && cfile.index().equals(sfile.index())) {
                    File tmp = new File(cfile.filename());
                    DataBlobs blb = new DataBlobs();
                    blb.setfile(cfile.filename(), tmp);
                    blb.setStaged(true, 2);
                    File save = new File(Utils.STAGINGDIR + "/" + blb.index());
                    Utils.writeObject(save, blb);
                    tmp.delete();
                }
            }
        }
    }

    public boolean conflict(ArrayList<MyFiles> splitFiles,
                            ArrayList<MyFiles> curFiles,
                            ArrayList<MyFiles> destFiles) {
        boolean conf = false;
        for (MyFiles sfile : splitFiles) {
            if (containsFilename(curFiles, sfile.filename())
                    && containsFilename(destFiles, sfile.filename())) {
                MyFiles cfile = findMyFile(curFiles, sfile.filename());
                MyFiles dfile = findMyFile(destFiles, sfile.filename());
                if (!sfile.index().equals(cfile.index())
                        && !sfile.index().equals(dfile.index())) {
                    conf = true;
                    DataBlobs cblob = findBlob(cfile.index());
                    DataBlobs dblob = findBlob(dfile.index());
                    if (cblob == null || dblob == null) {
                        System.out.println("Wrong in merge couldnt find blob");
                        exit(1);
                    }
                    String res = "<<<<<<< HEAD\n" + new String(cblob.getfile())
                            + "=======\n" + new String(dblob.getfile())
                            + ">>>>>>>\n";
                    DataBlobs blb = new DataBlobs();
                    blb.setfile(cfile.filename(), res.getBytes());
                    blb.setStaged(true, 1);
                    checkUntracked(curFiles, blb.getfilename());
                    save(blb);
                }
            } else if (containsFilename(curFiles, sfile.filename())
                    && !containsFilename(destFiles, sfile.filename())) {
                MyFiles cfile = findMyFile(curFiles, sfile.filename());
                if (!cfile.index().equals(sfile.index())) {
                    conf = true;
                    DataBlobs cblob = findBlob(cfile.index());
                    String res = "<<<<<<< HEAD\n" + new String(cblob.getfile())
                            + "=======\n" + ">>>>>>>\n";
                    DataBlobs blb = new DataBlobs();
                    blb.setfile(cfile.filename(), res.getBytes());
                    blb.setStaged(true, 1);
                    checkUntracked(curFiles, blb.getfilename());
                    save(blb);
                }
            } else if (!containsFilename(curFiles, sfile.filename())
                    && containsFilename(destFiles, sfile.filename())) {
                MyFiles dfile = findMyFile(destFiles, sfile.filename());
                if (!dfile.index().equals(sfile.index())) {
                    conf = true;
                    DataBlobs dblob = findBlob(dfile.index());
                    String res = "<<<<<<< HEAD\n" + "=======\n"
                            + new String(dblob.getfile()) + ">>>>>>>\n";
                    DataBlobs blb = new DataBlobs();
                    blb.setfile(dfile.filename(), res.getBytes());
                    blb.setStaged(true, 1);
                    checkUntracked(curFiles, blb.getfilename());

                    save(blb);
                }
            }
        }
        conf = conf2(conf, curFiles, splitFiles, destFiles);
        return conf;
    }

    public void save(DataBlobs blb) {
        File tmp = new File(Utils.STAGINGDIR + "/" + blb.index());
        Utils.writeContents(tmp, Utils.objToByte(blb));
        tmp = new File(blb.getfilename());
        Utils.writeContents(tmp, blb.getfile());
    }

    public boolean conf2(boolean conf, ArrayList<MyFiles> curFiles,
                         ArrayList<MyFiles> splitFiles,
                         ArrayList<MyFiles> destFiles) {
        for (MyFiles cfile : curFiles) {
            if (!containsFilename(splitFiles, cfile.filename())
                    && containsFilename(destFiles, cfile.filename())) {
                MyFiles dfile = findMyFile(destFiles, cfile.filename());
                if (!cfile.index().equals(dfile.index())) {
                    conf = true;
                    DataBlobs cblob = findBlob(cfile.index());
                    DataBlobs dblob = findBlob(dfile.index());
                    if (cblob == null || dblob == null) {
                        System.out.println("Wrong in merge couldnt find blob");
                        exit(1);
                    }
                    String res = "<<<<<<< HEAD\n" + new String(cblob.getfile())
                            + "=======\n" + new String(dblob.getfile())
                            + ">>>>>>>\n";
                    DataBlobs blb = new DataBlobs();
                    blb.setfile(cfile.filename(), res.getBytes());
                    blb.setStaged(true, 1);
                    checkUntracked(curFiles, blb.getfilename());

                    File tmp = new File(Utils.STAGINGDIR + "/" + blb.index());
                    Utils.writeContents(tmp, Utils.objToByte(blb));
                    tmp = new File(blb.getfilename());
                    Utils.writeContents(tmp, blb.getfile());
                }
            }
        }
        return conf;
    }

    public boolean containsFilename(ArrayList<MyFiles> in, String name) {
        for (MyFiles tmp : in) {
            if (tmp.filename().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public MyFiles findMyFile(ArrayList<MyFiles> in, String name) {
        for (MyFiles tmp : in) {
            if (tmp.filename().equals(name)) {
                return tmp;
            }
        }
        return null;
    }

    private DataBlobs findBlob(String index) {
        File tmp = new File(Utils.BLOBSDIR);
        File[] files = tmp.listFiles();
        for (File file : files) {
            String[] names = file.toString().split("/");
            String nn = names[names.length - 1];
            if (nn.equals(index)) {
                DataBlobs res = (DataBlobs) Utils.readObject(file);
                return res;
            }
        }
        return null;
    }

    public void checkoutstage(String index, ArrayList<MyFiles> curFiles) {
        File tar = new File(Utils.BLOBSDIR + "/" + index);
        DataBlobs data = (DataBlobs) Utils.readObject(tar);
        checkUntracked(curFiles, data.getfilename());

        File dest = new File(data.getfilename());
        Utils.writeContents(dest, data.getfile());
        data.setStaged(true, 1);
        dest = new File(Utils.STAGINGDIR
                + "/" + data.index());
        Utils.writeObject(dest, data);
    }

    public ArrayList<DataCommits> history(DataBranch in) {
        ArrayList<DataCommits> res = new ArrayList<>();
        DataCommits dest = findCmt(in.getPointer());
        while (dest != null) {
            res.add(dest);
            String par = dest.getparent();
            dest = findCmt(par);
        }
        return res;
    }

    private DataBranch match(DataBase db, String name) {
        for (DataBranch br : db.getbranches()) {
            if (br.getName().equals(name)) {
                return br;
            }
        }
        return null;
    }

    private DataCommits findCmt(String index) {
        File tmp = new File(Utils.COMMITSDIR);
        File[] files = tmp.listFiles();
        for (File file : files) {
            String name = file.toString();
            String[] tmpp = name.split("/");
            name = tmpp[tmpp.length - 1];
            if (name.equals(index)) {
                DataCommits res = (DataCommits) Utils.readObject(file);
                return res;
            }
        }
        return null;
    }
}

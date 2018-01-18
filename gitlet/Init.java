package gitlet;

import static java.lang.System.exit;

/**
 * gitlet init
 * @author Jialiang
 */
public class Init implements GitBody {

    public boolean check(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return false;
        }
        return true;
    }

    public void run(DataBase db) {
        if (Utils.dbExists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            exit(0);
        }
        db.init();
        DataCommits cmt = new DataCommits();
        cmt.initCommit();
        db.setcurrentCm(cmt);
        db.setcurBranch(new DataBranch("master", cmt));
        cmt.write();
        db.newBranch("master", cmt);
        db.writeCmt();
    }
}

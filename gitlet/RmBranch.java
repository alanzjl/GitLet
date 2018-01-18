package gitlet;

import static java.lang.System.exit;

/**
 * git rmbranch
 * @author Jialiang
 */
public class RmBranch implements GitBody {
    private String _name;

    public boolean check(String ... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return false;
        }
        _name = args[1];
        return true;
    }

    @Override
    public void run(DataBase db) {
        if (!db.containBranch(_name)) {
            System.out.println("A branch with that name does not exist.");
            exit(0);
        }
        if (db.getcurBranch().getName().equals(_name)) {
            System.out.println("Cannot remove the current branch.");
            exit(0);
        }
        db.removeBranch(_name);
        db.writeCmt();
    }
}

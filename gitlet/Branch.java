package gitlet;


import static java.lang.System.exit;

/**
 * gitlet branch xxx
 * @author Jialiang
 */

public class Branch implements GitBody {

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
        if (db.containBranch(_name)) {
            System.out.println("A branch with that name already exists.");
            exit(0);
        }
        db.newBranch(_name, db.getcurrentCm());
        db.writeCmt();
    }
}

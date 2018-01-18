package gitlet;


import static java.lang.System.exit;

/** Driver class for Gitlet, the tiny version-control system.
 *  @author Jialiang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        GitBody command = null;
        if (args.length < 1) {
            System.out.println("Please enter a command.");
            exit(0);
        }
        switch (args[0]) {
        case "init":
            command = new Init();
            break;
        case "add":
            command = new Add();
            break;
        case "commit":
            command = new Commit();
            break;
        case "rm":
            command = new Rm();
            break;
        case "log":
            command = new Log();
            break;
        case "global-log":
            command = new GlobalLog();
            break;
        case "find":
            command = new Find();
            break;
        case "status":
            command = new Status();
            break;
        case "checkout":
            command = new Checkout();
            break;
        case "branch":
            command = new Branch();
            break;
        case "rm-branch":
            command = new RmBranch(); break;
        case "reset":
            command = new Reset(); break;
        case "merge":
            command = new Merge(); break;
        default:
            System.out.println("No command with that name exists.");
            exit(0);
        }
        if (!command.check(args)) {
            exit(0);
        }
        if (!args[0].equals("init") && !checkInit()) {
            System.out.println("Not in an initialized Gitlet directory.");
            exit(0);
        }
        DataBase db = new DataBase();
        if (checkInit()) {
            db.load();
        }
        command.run(db);
    }

    private static boolean checkInit() {
        if (Utils.dbExists()) {
            return true;
        }
        return false;
    }
}

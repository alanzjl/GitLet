package gitlet;

import java.io.File;

/**
 * gitlet find
 * @author Jialiang
 */
public class Find implements GitBody {
    private String _msg;

    public boolean check(String ... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return false;
        }
        _msg = args[1];
        return true;
    }

    @Override
    public void run(DataBase db) {
        File filesDir = new File(Utils.COMMITSDIR);
        File[] files = filesDir.listFiles();
        DataCommits cmt;
        int cnt = 0;
        for (File file: files) {
            cmt = (DataCommits) Utils.readObject(file);
            if (_msg.equals(cmt.getlog())) {
                System.out.println(cmt.index());
                cnt += 1;
            }
        }
        if (cnt == 0) {
            System.out.println("Found no commit with that message.");
        }
    }
}

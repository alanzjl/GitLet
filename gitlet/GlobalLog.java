package gitlet;

import java.util.ArrayList;


/**
 * global logs
 * @author Jialiang
 */
public class GlobalLog implements GitBody {

    public boolean check(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return false;
        }
        return true;
    }

    @Override
    public void run(DataBase db) {
        ArrayList<DataCommits> cmts = db.getcommits();
        for (int i = cmts.size() - 1; i >= 0; i--) {
            DataCommits cmt = cmts.get(i);
            System.out.println("===");
            System.out.println("commit " + cmt.index());
            if (cmt.getparentSecond() != null) {
                System.out.print("Merge: ");
                System.out.print(cmt.getparent().substring(0, 7) + " ");
                System.out.println(cmt.getparentSecond().substring(0, 7));
            }
            System.out.println("Date: " + cmt.gettimestamp());
            System.out.println(cmt.getlog());
            System.out.println();
        }
    }
}

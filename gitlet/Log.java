package gitlet;

import java.io.File;

/**
 * local logs
 * @author Jialiang
 */
public class Log implements GitBody {

    public boolean check(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return false;
        }
        return true;
    }

    @Override
    public void run(DataBase db) {
        DataCommits cmt = db.getcurrentCm();
        while (cmt != null) {
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

            if (cmt.getparent() == null) {
                break;
            }
            File par = new File(Utils.COMMITSDIR + "/" + cmt.getparent());
            cmt = (DataCommits) Utils.readObject(par);
        }
    }
}

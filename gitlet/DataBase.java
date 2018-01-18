package gitlet;

import java.io.File;
import java.util.ArrayList;

import static java.lang.System.exit;

/**
 * database for gitlet
 * @author Jialiang
 */
public class DataBase {
    private ArrayList<DataCommits> _commits;
    private ArrayList<DataBlobs> _blobs;
    private ArrayList<DataBranch> _branches;

    /**
     * Indicates the current commit. */
    private DataCommits _currentCm;

    /**
     * Pointers to hash string of commits. */
    private DataBranch _curBranch;

    public DataCommits getcurrentCm() {
        return _currentCm;
    }

    public DataBranch getcurBranch() {
        return _curBranch;
    }

    public void setcurBranch(DataBranch curBranch) {
        this._curBranch = curBranch;
    }

    public void setcurrentCm(DataCommits currentCm) {
        this._currentCm = currentCm;
    }

    /**
     * Load the database from the working directory. */
    public void load() {
        _currentCm = null;
        _commits = new ArrayList<>();
        _blobs = new ArrayList<>();
        _branches = new ArrayList<>();

        if (!Utils.dbExists()) {
            System.out.println("[ERROR] Database not exists, existing.");
            exit(0);
        }

        File tmp = new File(Utils.CURBRANCH);
        _curBranch = (DataBranch) Utils.readObject(tmp);

        File commitDir = new File(Utils.COMMITSDIR);
        File blobDir = new File(Utils.BLOBSDIR);
        File[] commits = commitDir.listFiles();
        File[] blobs = blobDir.listFiles();
        for (File file : commits) {
            DataCommits commit = (DataCommits) Utils.readObject(file);
            _commits.add(commit);
        }
        for (File file : blobs) {
            DataBlobs blob = (DataBlobs) Utils.readObject(file);
            _blobs.add(blob);
        }

        File branchRec = new File(Utils.BRANCHREC);
        _branches = (ArrayList<DataBranch>) Utils.readObject(branchRec);

        File curCmtDir = new File(Utils.CURRENTCM);
        DataCommits cmRead = (DataCommits) Utils.readObject(curCmtDir);
        _currentCm = cmRead;
        if (_currentCm == null || _commits == null || _blobs == null
                || _branches == null || _curBranch == null) {
            System.out.println("[ERROR] Database loading failed");
            exit(0);
        }
    }

    /**
     * Initilize with an empty database. */
    public void init() {
        _currentCm = null;
        _commits = new ArrayList<>();
        _blobs = new ArrayList<>();
        _branches = new ArrayList<>();
        _curBranch = null;

        if (Utils.dbExists()) {
            System.out.println("[ERROR] Database already exists, existing.");
            exit(0);
        }

        File database = new File(Utils.DATABASE);
        try {
            database.mkdir();
        } catch (SecurityException se) {
            System.out.println("[ERROR] Creating directory failed. #01");
            exit(0);
        }

        File commits = new File(Utils.COMMITSDIR);
        File blobs = new File(Utils.BLOBSDIR);
        try {
            commits.mkdir();
            blobs.mkdir();
        } catch (SecurityException se) {
            System.out.println("[ERROR] Creating directory failed. #01");
            exit(0);
        }

        File staging = new File(Utils.STAGINGDIR);
        try {
            staging.mkdir();
        } catch (SecurityException se) {
            System.out.println("[ERROR] Creating directory failed. #01");
            exit(0);
        }

        File branches = new File(Utils.BRANCHREC);
        Utils.writeObject(branches, _branches);
        File branchPointers = new File(Utils.CURBRANCH);
        Utils.writeObject(branchPointers, _curBranch);

    }

    public void newBranch(String name, DataCommits cmt) {
        DataBranch br = new DataBranch(name, cmt);
        _branches.add(br);
    }

    public void updateBranch(String name, DataCommits cmt) {
        for (DataBranch br : _branches) {
            if (br.getName().equals(name)) {
                br.setPointer(cmt);
                _curBranch = br;
                return;
            }
        }
        DataBranch br = new DataBranch(name, cmt);
        _branches.add(br);
        _curBranch = br;
    }

    public void removeBranch(String name) {
        for (int i = 0; i < _branches.size(); i++) {
            if (_branches.get(i).getName().equals(name)) {
                _branches.remove(i);
                return;
            }
        }
    }

    public boolean containBranch(String name) {
        for (DataBranch br : _branches) {
            if (br.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void writeCmt() {
        File cmt = new File(Utils.CURRENTCM);
        Utils.writeObject(cmt, _currentCm);

        File br = new File(Utils.CURBRANCH);
        Utils.writeObject(br, _curBranch);

        File brs = new File(Utils.BRANCHREC);
        Utils.writeObject(brs, _branches);
    }

    public ArrayList<DataCommits> getcommits() {
        return _commits;
    }

    public ArrayList<DataBranch> getbranches() {
        return _branches;
    }

    public void setbranches(ArrayList<DataBranch> bbranches) {
        this._branches = bbranches;
    }
}

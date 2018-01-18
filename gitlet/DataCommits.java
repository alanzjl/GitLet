package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.List;

/**
 * Commits for gitlet
 * @author Jialiang
 */
public class DataCommits implements Serializable {
    private String _timestamp;
    private ArrayList<MyFiles> _blobs;
    private String _log;
    private String _parent;
    private String _parentSecond;
    private String _branch;
    private String _index;

    public DataCommits() {
        _blobs = new ArrayList<>();
        _parentSecond = null;
    }

    public String getbranch() {
        return _branch;
    }

    public DataCommits(String log, DataCommits par) {
        _blobs = new ArrayList<>(par.getblobs());
        _log = log;
        _branch = par.getbranch();
        _parent = par.index();
        _timestamp = Utils.timeGen();
        _parentSecond = null;
    }

    public ArrayList<MyFiles> getblobs() {
        return _blobs;
    }

    public void setBlobs(DataBlobs... blobs) {
        for (DataBlobs blob : blobs) {
            _blobs.add(new MyFiles(blob.getfilename(), blob.index()));
        }
    }

    public void setBlobs(ArrayList<MyFiles> blobs) {
        _blobs = blobs;
    }

    public void initCommit() {
        _log = "initial commit";
        _parent = null;
        _branch = "master";
        _timestamp = Utils.initialTime();
    }

    public String index() {
        indexUpdate();
        return _index;
    }

    private void indexUpdate() {
        List<Object> indexer = new ArrayList<>();
        if (_log != null) {
            indexer.add(_log);
        } else {
            indexer.add("");
        }
        if (_timestamp != null) {
            indexer.add(_timestamp);
        } else {
            indexer.add("");
        }
        if (_parent != null) {
            indexer.add(_parent);
        } else {
            indexer.add("");
        }
        if (_blobs != null) {
            for (int i = 0; i < _blobs.size(); i++) {
                indexer.add(_blobs.get(i).index());
            }
        }
        _index = Utils.sha1(indexer);
    }

    public void setparentSecond(String parentSecond) {
        this._parentSecond = parentSecond;
    }

    public void write() {
        File tmp = new File(Utils.COMMITSDIR + "/" + index());
        Utils.writeObject(tmp, this);
    }

    @Override
    public boolean equals(Object obj) {
        DataCommits tmp = (DataCommits) obj;
        return tmp.index().equals(index());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getlog() {
        return _log;
    }

    public String gettimestamp() {
        return _timestamp;
    }

    public String getparentSecond() {
        return _parentSecond;
    }

    public String getparent() {
        return _parent;
    }

    public void setbranch(String branch) {
        this._branch = branch;
    }

    @Override
    public String toString() {
        return _index;
    }
}

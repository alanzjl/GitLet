package gitlet;

import java.io.Serializable;

/**
 * Branches for gitlet
 * @author Jialiang
 */

public class DataBranch implements Serializable {

    private String _name;
    private String _pointer;

    public DataBranch(String name, String pointer) {
        this._name = name;
        this._pointer = pointer;
    }

    public DataBranch(String name, DataCommits commit) {
        this._name = name;
        this._pointer = commit.index();
    }

    @Override
    public boolean equals(Object obj) {
        DataBranch db = (DataBranch) obj;
        return db.getName().equals(_name) && db.getPointer().equals(_pointer);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setPointer(String pointer) {
        this._pointer = pointer;
    }

    public void setPointer(DataCommits commit) {
        this._pointer = commit.index();
    }

    public String getPointer() {
        return _pointer;
    }

    public String getName() {
        return _name;
    }
}

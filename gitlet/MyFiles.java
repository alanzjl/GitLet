package gitlet;

import java.io.Serializable;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Jialiang
 */
public class MyFiles implements Serializable {
    private String _filename;
    private String _index;
    private int _version;

    public MyFiles(String name, String id) {
        _index = id;
        _filename = name;
        _version = 1;
    }

    public MyFiles(String name, String id, int version) {
        _index = id;
        _filename = name;
        this._version = version;
    }

    public MyFiles(DataBlobs a, int ver) {
        _index = a.index();
        _filename = a.getfilename();
        _version = ver;
    }

    public String filename() {
        return _filename;
    }
    public String index() {
        return _index;
    }
    public int version() {
        return _version;
    }


    @Override
    public String toString() {
        return _filename + " v" + String.valueOf(_version);
    }
}

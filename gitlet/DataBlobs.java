package gitlet;

import java.io.File;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Datablobs for gitlet.
 * @author Jialiang
 */
public class DataBlobs implements Serializable {

    private String _filename;
    private byte[] _file;
    private boolean _staged;

    private String _index;

    /**
     * Add: 1, remove: 2.
     */
    private int _stageState;

    public DataBlobs copy() {
        DataBlobs res = new DataBlobs();
        res.setfile(_filename, _file);
        res.setStaged(isstaged(), _stageState);
        return res;
    }

    public DataBlobs() {
        _staged = false;
    }

    public void setStaged(boolean staged, int state) {
        _staged = staged;
        _stageState = state;
    }

    public void setfile(String name, File src) {
        _filename = name;
        _index = Utils.sha1(Utils.readContents(src));
        _file = Utils.readContents(src);
    }

    public void setfile(String name, byte[] src) {
        _filename = name;
        _file = Arrays.copyOf(src, src.length);
        _index = Utils.sha1(_file);
    }

    @Override
    public boolean equals(Object obj) {
        DataBlobs tmp = (DataBlobs) obj;
        return tmp.index().equals(index());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getfilename() {
        return _filename;
    }

    public byte[] getfile() {
        return _file;
    }

    public String index() {
        _index = Utils.sha1(_file, _filename);
        return _index;
    }

    public void clearStage() {
        _staged = false;
        _stageState = 0;
    }

    public boolean isstaged() {
        return _staged;
    }

    public int getstageState() {
        return _stageState;
    }
}

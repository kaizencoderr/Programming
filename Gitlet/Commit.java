package gitlet;
import java.io.Serializable;
import java.util.HashMap;

/** This class creates a COMMIT object with associated features.
 * Each commit has a message, a unique timestamp, a parent commit,
 * a unique SHA1 id, and some tracked files.
 *  @author Tyler Freund
 */
public class Commit implements Serializable {

    /** The string message associated with a commit instance. */
    private String _message;

    /** The timestamp (converted to a string) associated with
     * a commit instance. */
    private String _timestamp;

    /** The parent of a commit instance in the form of a SHA1 id. */
    private String _parent;

    /** A hashmap containing the tracked files in a commit instance.
     * The key (of string form) is the name of the file and the
     * values are the blobs associated with that file. */
    private HashMap<String, Blobs> _trackedfiles;

    /** A unique SHA1 hash id that identifies each commit object. */
    private String _commitid;

    /** Commit constructor. MESSAGE is the commit message, TIMESTAMP
     *  is the commit timestamp, PARENT is the parent of the commit,
     *  and TRACKEDFILES is the trackedfiles in this commit. */
    public Commit(String message, String parent, String timestamp,
                  HashMap<String, Blobs> trackedfiles) {
        this._message = message;
        this._parent = parent;
        this._timestamp = timestamp;
        this._trackedfiles = trackedfiles;
    }

    /** Returns the message of this commit. */
    public String getMessage() {
        return this._message;
    }

    /** Returns the timestamp of this commit. */
    public String getTimestamp() {
        return this._timestamp;
    }

    /** Returns the parent of this commit. */
    public String getParent() {
        return this._parent;
    }

    /** Returns the id of this commit. */
    public String getID() {
        return this._commitid;
    }

    /** Returns the tracked files of this commit. */
    public HashMap<String, Blobs> getTrackedFiles() {
        return this._trackedfiles;
    }

    /** Sets the commit ID of this commit. */
    public void setID() {
        this._commitid = Utils.sha1(_message, _timestamp);
    }

}

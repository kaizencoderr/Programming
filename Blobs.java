package gitlet;

import java.io.Serializable;

/** Blobs are essentially files, they have a name, unique id to
 * differentiate between same named files with differing contents,
 * and associated contents of that file.
 *  @author Tyler Freund
 */
public class Blobs implements Serializable {

    /** The name of the file in our blob. */
    private String _name;

    /** The SHA1 id of the contents of the file in our blob. */
    private String _id;

    /** The contents of the file in our blob. */
    private String _contents;

    /** Blob Constructor initiating this blobs NAME, ID,
     * and CONTENTS. */
    public Blobs(String name, String id, String contents) {
        this._name = name;
        this._id = id;
        this._contents = contents;
    }

    /** Getter method to return the NAME of this blob. */
    public String getName() {
        return this._name;
    }

    /** Getter method to return the ID of this blob. */
    public String getID() {
        return this._id;
    }

    /** Getter method to return the CONTENTS of this blob. */
    public String getContents() {
        return this._contents;
    }

}

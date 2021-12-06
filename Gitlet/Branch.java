package gitlet;

import java.io.Serializable;
import java.util.LinkedList;

/** A class for BRANCH objects. Each branch has a name, a
 * commit history, and specific commit it points to. One of
 * these branches is the head (active branch).
 *  @author Tyler Freund */

public class Branch implements Serializable {

    /** The name of the branch as a String; for instance "master". */
    private String _name;

    /** The SHA1 id of the commit that this branch points to. */
    private String _id;

    /** The history of this branch of previous commits.  Each String in the
     * linked list is a commit. */
    private LinkedList<String> _history;

    /** Boolean returning true if the branch is the active branch/HEAD. */
    private boolean _isActiveBranch;

    /** Branch constructor. NAME is the name of the branch, ID
     * is the id of the branch, HISTORY is the commit history
     * of the branch, and ISACTIVEBRANCH is a boolean that
     * tells us whether the branch is the HEAD. */
    public Branch(String name, String id, LinkedList<String> history,
                  boolean isActiveBranch) {
        this._name = name;
        this._id = id;
        this._history = history;
        this._isActiveBranch = isActiveBranch;
    }

    /** Returns the name of this branch. */
    public String getname() {
        return this._name;
    }

    /** Returns the id of this branch. */
    public String getid() {
        return this._id;
    }

    /** Returns the history of this branch. */
    public LinkedList<String> gethistory() {
        return this._history;
    }

    /** Updates the history of this branch to NEWHISTORY, used when
     * we make a new commit to add to the active branch's history. */
    public void updatehistory(LinkedList<String> newhistory) {
        this._history = newhistory;
    }

    /** Moves the ID of the active branch when a new
     * commit happens. */
    public void setid(String id) {
        this._id = id;
    }

}

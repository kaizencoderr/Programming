package gitlet;


import java.io.File;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Formatter;
import java.util.ArrayList;
import java.util.Collections;


/** Commandclass executes the calls from Main.  This is where
 * all of our Git commands are housed.
 *  @author Tyler Freund*/

public class Commandclass implements Serializable {

    /**
     * Hashmap of all commits for this repo. The keys are strings of the
     * commitID and the values are commit objects themselves.
     */
    private static HashMap<String, Commit> _allcommits;

    /**
     * The additions in the staging area for this repo.
     */
    private static HashMap<String, Blobs> additions;

    /**
     * The removals in the staging area for this repo.
     */
    private static HashMap<String, Blobs> removals;

    /**
     * The history of commits as a linked list of commit ids.
     */
    private static LinkedList<String> _commithistory;

    /**
     * The HEAD pointer to the head of our commits_tree.
     */
    private static String _head;

    /**
     * All branch pointers.  The keys are the name of the branches
     * and the values are the branch objects.
     */
    private static HashMap<String, Branch> _allbranches;

    /**
     * Current Working Directory.
     */
    static final File CWD = new File(".");

    /**
     * Main metadata folder.
     */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");

    /**
     * Commits directory.
     */
    static final File COMMIT_DIR = Utils.join(GITLET_FOLDER, "commits");

    /**
     * Staging directory.
     */
    static final File STAGING_DIR = Utils.join(GITLET_FOLDER, "staging");


    /**
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message initial commit (just like
     * that, with no punctuation). It will have a single branch: master, which
     * initially points to this initial commit, and master will be the current
     * branch. The timestamp for this initial commit will be 00:00:00 UTC,
     * Thursday, 1 January 1970 in whatever format you choose for dates (this
     * is called "The (Unix) Epoch", represented internally by the time 0.)
     * Since the initial commit in all repositories created by Gitlet will have
     * exactly the same content, it follows that all repositories will
     * automatically share this commit (they will all have the same UID) and
     * all commits in all repositories will trace back to it.
     */
    public static void init() {
        if (GITLET_FOLDER.exists() || COMMIT_DIR.exists()
                || STAGING_DIR.exists()) {
            System.out.println("A Gitlet version-control system already"
                    + "exists in the current directory.");
        }
        GITLET_FOLDER.mkdirs();
        COMMIT_DIR.mkdirs();
        STAGING_DIR.mkdir();
        additions = new HashMap<String, Blobs>();
        File additionspath = Utils.join(STAGING_DIR, "additions");
        Utils.writeObject(additionspath, additions);
        removals = new HashMap<String, Blobs>();
        File removalspath = Utils.join(STAGING_DIR, "removals");
        Utils.writeObject(removalspath, removals);
        Date d = new Date();
        d.setTime(0);
        java.sql.Timestamp initialtime = new java.sql.Timestamp(d.getTime());
        HashMap<String, Blobs> emptytracked = new HashMap<>();
        Commit initial = new Commit("initial commit", null,
                initialtime.toString(), emptytracked);
        initial.setID();
        _allcommits = new HashMap<String, Commit>();
        _allcommits.put(initial.getID(), initial);
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        Utils.writeObject(commitpath, _allcommits);
        _commithistory = new LinkedList<>();
        _commithistory.add(initial.getID());
        File commithistorypath = Utils.join(COMMIT_DIR, "commit_history");
        Utils.writeObject(commithistorypath, _commithistory);
        _allbranches = new HashMap<>();
        LinkedList<String> branchhistory = new LinkedList<>();
        branchhistory.add(initial.getID());
        Branch master = new Branch("master", initial.getID(),
                branchhistory, true);
        _allbranches.put("master", master);
        File allbranchespath = Utils.join(COMMIT_DIR, "all_branches");
        Utils.writeObject(allbranchespath, _allbranches);
        _head = "master";
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        Utils.writeObject(headpath, _head);
    }

    /**
     * Adds a copy of the file as it currently exists to the staging
     * area (see the description of the commit command). For this reason,
     * adding a file is also called staging the file for addition.
     * Staging an already-staged file overwrites the previous entry in
     * the staging area with the new contents. The staging area should
     * be somewhere in .gitlet. If the current working version of the
     * file is identical to the version in the current commit, do not
     * stage it to be added, and remove it from the staging area if it
     * is already there (as can happen when a file is changed, added,
     * and then changed back). The file will no longer be staged for
     * removal (see gitlet rm), if it was at the time of the command.
     * The parameter FILENAME is the name of the file to add to the
     * staging area.
     */
    public static void add(String filename) {
        File removalspath = Utils.join(STAGING_DIR, "removals");
        removals = Utils.readObject(removalspath, HashMap.class);
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        File commithistorypath = Utils.join(COMMIT_DIR,
                "commit_history");
        _commithistory = Utils.readObject(commithistorypath,
                LinkedList.class);
        File allbranchespath = Utils.join(COMMIT_DIR,
                "all_branches");
        _allbranches = Utils.readObject(allbranchespath, HashMap.class);
        File filetoadd = Utils.join(CWD, filename);
        if (!filetoadd.exists()) {
            System.out.println("File does not exist.");
        } else {
            String filecontents = Utils.readContentsAsString(filetoadd);
            String fileid = Utils.sha1(filecontents);
            Blobs createdblob = new Blobs(filename, fileid, filecontents);
            File additionspath = Utils.join(STAGING_DIR, "additions");
            additions = Utils.readObject(additionspath, HashMap.class);
            if (additions.containsKey(filename)) {
                additions.remove(filename);
                additions.put(filename, createdblob);
            } else {
                additions.put(filename, createdblob);
                Branch currbranch = _allbranches.get(_head);
                String currbranchid = currbranch.getid();
                Commit currcomm = _allcommits.get(currbranchid);
                HashMap<String, Blobs> currtracked
                        = currcomm.getTrackedFiles();
                if (currtracked.containsKey(filename)) {
                    Blobs blobcurr = currtracked.get(filename);
                    String currcontents = blobcurr.getContents();
                    if (currcontents.equals(filecontents)) {
                        additions.remove(filename);
                        removals.remove(filename);
                    }
                }
            }
            Utils.writeObject(additionspath, additions);
            Utils.writeObject(removalspath, removals);
        }
    }

    /**
     * Saves a snapshot of tracked files in the current commit and
     * staging area so they can be restored at a later time, creating
     * a new commit. The commit is said to be tracking the saved files.
     * By default, each commit's snapshot of files will be exactly the
     * same as its parent commit's snapshot of files; it will keep
     * versions of files exactly as they are, and not update them. A
     * commit will only update the contents of files it is tracking
     * that have been staged for addition at the time of commit, in
     * which case the commit will now include the version of the file
     * that was staged instead of the version it got from its parent.
     * A commit will save and start tracking any files that were staged
     * for addition but weren't tracked by its parent. Finally, files
     * tracked in the current commit may be untracked in the new commit
     * as a result being staged for removal by the rm command.  The
     * parameter COMMITMESSAGE is the message with our new commit.
     */
    public static void commit(String commitmessage) throws GitletException {
        File additionspath = Utils.join(STAGING_DIR, "additions");
        additions = Utils.readObject(additionspath, HashMap.class);
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        File removalspath = Utils.join(STAGING_DIR, "removals");
        removals = Utils.readObject(removalspath, HashMap.class);
        File commithistorypath = Utils.join(COMMIT_DIR, "commit_history");
        _commithistory = Utils.readObject(commithistorypath, LinkedList.class);
        File allbranchespath = Utils.join(COMMIT_DIR, "all_branches");
        _allbranches = Utils.readObject(allbranchespath, HashMap.class);
        if (additions.isEmpty() && removals.isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else {
            Branch parentidbranch = _allbranches.get(_head);
            String parentid = parentidbranch.getid();
            Commit prevcomm = _allcommits.get(parentid);
            HashMap<String, Blobs> prevtracked = prevcomm.getTrackedFiles();
            for (String key : prevtracked.keySet()) {
                if (!additions.containsKey(key)) {
                    Blobs addblob = prevtracked.get(key);
                    additions.put(key, addblob);
                }
            }
            for (String key : removals.keySet()) {
                if (additions.containsKey(key)) {
                    additions.remove(key);
                }
            }
            Date d = new Date();
            java.sql.Timestamp timestamp = new java.sql.Timestamp(d.getTime());
            Commit newcommit = new Commit(commitmessage, parentid,
                    timestamp.toString(), additions);
            newcommit.setID();
            Branch currentbranch = _allbranches.get(_head);
            currentbranch.setid(newcommit.getID());
            LinkedList<String> bhist = currentbranch.gethistory();
            bhist.add(newcommit.getID());
            currentbranch.updatehistory(bhist);
            Utils.writeObject(headpath, _head);
            Utils.writeObject(allbranchespath, _allbranches);
            _allcommits.put(newcommit.getID(), newcommit);
            Utils.writeObject(commitpath, _allcommits);
            additions.clear();
            Utils.writeObject(additionspath, additions);
            removals.clear();
            Utils.writeObject(removalspath, removals);
            _commithistory = Utils.readObject(commithistorypath,
                    LinkedList.class);
            _commithistory.add(newcommit.getID());
            Utils.writeObject(commithistorypath, _commithistory);
        }
    }

    /**
     * Takes the version of the file as it exists in the head commit,
     * the front of the current branch, and puts it in the working
     * directory, overwriting the version of the file that's already
     * there if there is one. The new version of the file is not staged.
     * CHECKOUTFILENAME is the file name we are checking out.
     */
    public static void checkout1(String checkoutfilename) {
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        _head = Utils.readObject(headpath, String.class);
        File allbranchespath = Utils.join(COMMIT_DIR, "all_branches");
        _allbranches = Utils.readObject(allbranchespath, HashMap.class);
        Branch headbranch = _allbranches.get(_head);
        String headid = headbranch.getid();
        Commit headcommit = _allcommits.get(headid);
        HashMap<String, Blobs> trackedfiles = headcommit.getTrackedFiles();
        if (!trackedfiles.containsKey(checkoutfilename)) {
            System.out.println("File does not exist in that commit.");
        }
        Blobs blobwanted = trackedfiles.get(checkoutfilename);
        String newcontents = blobwanted.getContents();
        File checkoutfilepath = Utils.join(CWD, checkoutfilename);
        Utils.writeContents(checkoutfilepath, newcontents);
    }

    /**
     * Takes the version of the file as it exists in the commit
     * with the given id, and puts it in the working directory,
     * overwriting the version of the file that's already there if
     * there is one. The new version of the file is not staged.
     * The first param COMMITID is the id of the commit the
     * user wants to checkout, the CHECKOUTFILENAME is the name
     * of the file they want to checkout.
     */
    public static void checkout2(String commitID, String checkoutfilename) {
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        if (commitID.length() == 8) {
            for (String id : _allcommits.keySet()) {
                String partial = id.substring(0, 8);
                if (partial.equals(commitID)) {
                    commitID = id;
                }
            }
        }
        if (!_allcommits.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit desiredcommit = _allcommits.get(commitID);
        HashMap<String, Blobs> trackedfiles = desiredcommit.getTrackedFiles();
        if (!trackedfiles.containsKey(checkoutfilename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Blobs blobwanted = trackedfiles.get(checkoutfilename);
        String newcontents = blobwanted.getContents();
        File checkoutfilepath = Utils.join(CWD, checkoutfilename);
        Utils.writeContents(checkoutfilepath, newcontents);
        Utils.writeObject(commitpath, _allcommits);
    }

    /**
     * Takes all files in the commit at the head of
     * the given branch, and puts them in the working directory,
     * overwriting the versions of the files that are already there if
     * they exist. Also, at the end of this command, the given branch
     * will now be considered the current branch (HEAD). Any files that
     * are tracked in the current branch but are not present in the
     * checked-out branch are deleted. The staging area is cleared,
     * unless the checked-out branch is the current branch. Our
     * param NAME is name of the branch we are checking out.
     */
    public static void checkout3(String name) {
        File additionspath = Utils.join(STAGING_DIR, "additions");
        additions = Utils.readObject(additionspath, HashMap.class);
        File removalspath = Utils.join(STAGING_DIR, "removals");
        removals = Utils.readObject(removalspath, HashMap.class);
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        File allbranchespath = Utils.join(COMMIT_DIR, "all_branches");
        _allbranches = Utils.readObject(allbranchespath, HashMap.class);
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        if (!_allbranches.containsKey(name)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (_head.equals(name)) {
            System.out.println("No need to checkout the current branch.");
        }
        String currentid = _allbranches.get(_head).getid();
        Commit currentcommit = _allcommits.get(currentid);
        HashMap<String, Blobs> currenttracked = currentcommit.getTrackedFiles();
        Commit desiredcommit = _allcommits.get(_allbranches.get(name).getid());
        HashMap<String, Blobs> tracked = desiredcommit.getTrackedFiles();
        List<String> allfilesCWD = Utils.plainFilenamesIn(CWD);
        for (String filename : allfilesCWD) {
            File onepath = Utils.join(CWD, filename);
            String cwdcontents = Utils.readContentsAsString(onepath);
            if (!currenttracked.containsKey(filename)) {
                if (tracked.containsKey(filename)) {
                    String newcontents = tracked.get(filename).getContents();
                    if (!newcontents.equals(cwdcontents)) {
                        System.out.println("There is an untracked file in the"
                                + "way; delete it, or add and commit it"
                                + "first.");
                        return;
                    }
                }
            }
        }
        for (Blobs blob : tracked.values()) {
            String newcontents = blob.getContents();
            String checkoutfilename = blob.getName();
            File checkoutfilepath = Utils.join(CWD, checkoutfilename);
            Utils.writeContents(checkoutfilepath, newcontents);
        }
        for (Blobs blob : currenttracked.values()) {
            if (!tracked.containsKey(blob.getName())) {
                File untrackedpath = Utils.join(CWD, blob.getName());
                untrackedpath.delete();
            }
        }
        if (!_head.equals(name)) {
            removals.clear();
            additions.clear();
        }
        _head = name;
        Utils.writeObject(headpath, _head);
        Utils.writeObject(removalspath, removals);
        Utils.writeObject(additionspath, additions);
    }

    /**
     * Starting at the current head commit, display information
     * about each commit backwards along the commit tree until the
     * initial commit, following the first parent commit links,
     * ignoring any second parents found in merge commits. (In
     * regular Git, this is what you get with git log --first-parent).
     * This set of commit nodes is called the commit's history.
     * For every node in this history, the information it should
     * display is the commit id, the time the commit was made,
     * and the commit message.
     */
    public static void log() {
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        File commithistorypath = Utils.join(COMMIT_DIR,
                "commit_history");
        _commithistory = Utils.readObject(commithistorypath,
                LinkedList.class);
        File allbranchespath = Utils.join(COMMIT_DIR,
                "all_branches");
        _allbranches = Utils.readObject(allbranchespath, HashMap.class);
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        Branch headbranch = _allbranches.get(_head);
        for (int i = headbranch.gethistory().size() - 1; i >= 0; i--) {
            String currcom = headbranch.gethistory().get(i);
            Commit x = _allcommits.get(currcom);
            System.out.println("===");
            System.out.println("commit " + x.getID());
            String stringtime = x.getTimestamp();
            Timestamp time = Timestamp.valueOf(stringtime);
            Formatter f = new Formatter();
            f.format("%tc", time);
            String date = f.toString();
            date = date.replace("PST ", "");
            System.out.println("Date: " + date + " -0800");
            System.out.println(x.getMessage() + "\n");
        }
    }

    /**
     * Unstage the file if it is currently staged for addition. If
     * the file is tracked in the current commit, stage it for
     * removal and remove the file from the working directory
     * if the user has not already done so (do not remove it unless
     * it is tracked in the current commit). FILETOREMOVE is the file
     * the user wants to remove
     */
    public static void rm(String filetoremove) {
        File additionspath = Utils.join(STAGING_DIR, "additions");
        additions = Utils.readObject(additionspath, HashMap.class);
        File removalspath = Utils.join(STAGING_DIR, "removals");
        removals = Utils.readObject(removalspath, HashMap.class);
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        File commithistorypath = Utils.join(COMMIT_DIR,
                "commit_history");
        _commithistory = Utils.readObject(commithistorypath, LinkedList.class);
        File allbranchespath = Utils.join(COMMIT_DIR, "all_branches");
        _allbranches = Utils.readObject(allbranchespath, HashMap.class);
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        File removepath = Utils.join(CWD, filetoremove);
        String commitid = _allbranches.get(_head).getid();
        Commit currentcommit = _allcommits.get(commitid);
        HashMap<String, Blobs> currenttracked = currentcommit.getTrackedFiles();
        if (!additions.containsKey(filetoremove)
                && !currenttracked.containsKey(filetoremove)) {
            System.out.println("No reason to remove the file.");
        }
        if (additions.containsKey(filetoremove)) {
            additions.remove(filetoremove);
        }
        if (!currenttracked.isEmpty()) {
            if (currenttracked.containsKey(filetoremove)) {
                Blobs blobtoremove = currenttracked.get(filetoremove);
                removals.put(filetoremove, blobtoremove);
                if (removepath.exists()) {
                    removepath.delete();
                }
            }
        }
        Utils.writeObject(additionspath, additions);
        Utils.writeObject(commitpath, _allcommits);
        Utils.writeObject(commithistorypath, _commithistory);
        Utils.writeObject(removalspath, removals);
    }

    /**
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.
     */
    public static void globallog() {
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        File commithistorypath = Utils.join(COMMIT_DIR,
                "commit_history");
        _commithistory = Utils.readObject(commithistorypath,
                LinkedList.class);
        for (int i = _commithistory.size() - 1; i >= 0; i--) {
            String currcom = _commithistory.get(i);
            Commit x = _allcommits.get(currcom);
            System.out.println("===");
            System.out.println("commit " + x.getID());
            String stringtime = x.getTimestamp();
            Timestamp time = Timestamp.valueOf(stringtime);
            Formatter f = new Formatter();
            f.format("%tc", time);
            String date = f.toString();
            date = date.replace("PST ", "");
            System.out.println("Date: " + date + " -0800");
            System.out.println(x.getMessage() + "\n");
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit
     * message, one per line. If there are multiple such commits,
     * it prints the ids out on separate lines.  COMMITMESSAGE param
     * is the message of the commit the user is trying to find.
     */
    public static void find(String commitmessage) {
        File commitpath = Utils.join(COMMIT_DIR,
                "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        boolean messageexists = false;
        for (Commit c : _allcommits.values()) {
            if (c.getMessage().equals(commitmessage)) {
                messageexists = true;
            }
        }
        if (!messageexists) {
            System.out.println("Found no commit with that message.");
        }
        for (Commit c : _allcommits.values()) {
            if (c.getMessage().equals(commitmessage)) {
                System.out.println(c.getID());
            }
        }
        Utils.writeObject(commitpath, _allcommits);
    }

    /**
     * Creates a new branch with the given name, and points it at the
     * current head node. A branch is nothing more than a name for a
     * reference (a SHA-1 identifier) to a commit node. This command
     * does NOT immediately switch to the newly created branch (just
     * as in real Git). NEWBRANCHNAME is the name of
     * the new branch being created.
     */
    public static void branch(String newbranchname) {
        File allbranchespath = Utils.join(COMMIT_DIR,
                "all_branches");
        _allbranches = Utils.readObject(allbranchespath, HashMap.class);
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        if (_allbranches.containsKey(newbranchname)) {
            System.out.println("A branch with that name already exists.");
        }
        Branch old = _allbranches.get(_head);
        LinkedList<String> newhistory = new LinkedList<>();
        for (String i : old.gethistory()) {
            newhistory.add(i);
        }
        Branch newbranch = new Branch(newbranchname, old.getid(),
                newhistory, false);
        _allbranches.put(newbranchname, newbranch);
        Utils.writeObject(allbranchespath, _allbranches);
    }

    /**
     * Displays what branches currently exist, and marks the current
     * branch with a *. Also displays what files have been staged for
     * addition or removal. There is an empty line between sections.
     * Entries should be listed in lexicographic order, using the
     * Java string-comparison order (the asterisk doesn't count).
     */
    public static void status() {
        if (!GITLET_FOLDER.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        File additionspath = Utils.join(STAGING_DIR, "additions");
        additions = Utils.readObject(additionspath, HashMap.class);
        File removalspath = Utils.join(STAGING_DIR, "removals");
        removals = Utils.readObject(removalspath, HashMap.class);
        File allbranchespath = Utils.join(COMMIT_DIR,
                "all_branches");
        _allbranches = Utils.readObject(allbranchespath, HashMap.class);
        System.out.println("=== Branches ===");
        List<String> unorderedlist = new ArrayList<String>();
        for (String branch : _allbranches.keySet()) {
            unorderedlist.add(branch);
        }
        List<String> ordered =
                Commandclass.lexicographicsort(unorderedlist);
        for (String branch : ordered) {
            if (branch.equals(_head)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println("\n" + "=== Staged Files ===");
        unorderedlist.clear();
        ordered.clear();
        for (String filename : additions.keySet()) {
            unorderedlist.add(filename);
        }
        ordered = Commandclass.lexicographicsort(unorderedlist);
        for (String filestaged : ordered) {
            System.out.println(filestaged);
        }
        System.out.println("\n" + "=== Removed Files ===");
        unorderedlist.clear();
        ordered.clear();
        for (String filename : removals.keySet()) {
            unorderedlist.add(filename);
        }
        ordered = Commandclass.lexicographicsort(unorderedlist);
        for (String fileremoved : ordered) {
            System.out.println(fileremoved);
        }
        status2();
    }

    /**
     * Displays what branches currently exist, and marks the current
     * branch with a *. Also displays what files have been staged for
     * addition or removal. There is an empty line between sections.
     * Entries should be listed in lexicographic order, using the
     * Java string-comparison order (the asterisk doesn't count).
     */
    public static void status2() {
        List<String> allfilesCWD = Utils.plainFilenamesIn(CWD);
        boolean mod = false;
        boolean del = false;
        System.out.println("\n"
                + "=== Modifications Not Staged For Commit ===");
        String x = _allbranches.get(_head).getid();
        HashMap<String, Blobs> tracked = _allcommits.get(x).getTrackedFiles();
        for (String f : allfilesCWD) {
            File fpath = Utils.join(CWD, f);
            String filecon = Utils.readContentsAsString(fpath);
            if (tracked.containsKey(f)) {
                if (!tracked.get(f).getContents().equals(filecon)
                        && !additions.containsKey(f)) {
                    mod = true;
                }
            }
            if (additions.containsKey(f)) {
                String addicon = additions.get(f).getContents();
                if (!addicon.equals(filecon)) {
                    mod = true;
                }
            }
            if (additions.containsKey(f) && !allfilesCWD.contains(f)) {
                del = true;
            }
            if (mod) {
                System.out.println(f + " (modified)");
            }
            if (del) {
                System.out.println(f + " (deleted)");
            }
        }
        for (String file : tracked.keySet()) {
            if (!removals.containsKey(file) && !allfilesCWD.contains(file)) {
                System.out.println(file + " (deleted)");
            }
        }
        System.out.println("\n" + "=== Untracked Files ===");
        boolean untrack1 = true;
        for (String f : allfilesCWD) {
            for (Commit c : _allcommits.values()) {
                if (additions.containsKey(f)
                        || c.getTrackedFiles().containsKey(f)) {
                    untrack1 = false;
                }
            }
            if (untrack1) {
                System.out.println(f);

            }
        }
        System.out.println("\n");
    }

    /**
     * A method to help lexicographically sort a list of strings.
     * Directly helps in our git status command to sort the branches,
     * stage area, and other objects. UNORDEREDLIST is the list
     * that we need to sort. Returns a sorted LIST of strings.
     */
    public static List<String> lexicographicsort(List<String> unorderedlist) {
        Collections.sort(unorderedlist);
        return unorderedlist;
    }

    /**
     * Deletes the branch with the given name. This only means to
     * delete the pointer associated with the branch; it does not
     * mean to delete all commits that were created under the branch,
     * or anything like that.  BRANCHTOREMOVE is the branch our user
     * wants to remove.
     */
    public static void rmbranch(String branchtoremove) {
        File allbranchespath = Utils.join(COMMIT_DIR,
                "all_branches");
        _allbranches = Utils.readObject(allbranchespath,
                HashMap.class);
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        if (!_allbranches.containsKey(branchtoremove)) {
            System.out.println("branch with that name does not exist.");
            return;
        } else if (_head.equals(branchtoremove)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else {
            _allbranches.remove(branchtoremove);
            Utils.writeObject(allbranchespath, _allbranches);
        }
    }

    /**
     * Checks out all the files tracked by the given commit. Removes
     * tracked files that are not present in that commit. Also moves
     * the current branch's head to that commit node. See the intro
     * for an example of what happens to the head pointer after using
     * reset. The [commit id] may be abbreviated as for checkout. The
     * staging area is cleared. The command is essentially checkout
     * of an arbitrary commit that also changes the current branch
     * head. COMMITID is the id of the commit the user wants to reset.
     */
    public static void reset(String commitid) {
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        if (!_allcommits.containsKey(commitid)) {
            System.out.println("No commit with that id exists.");
            return;
        } else {
            Commit desiredcommit = _allcommits.get(commitid);
            HashMap<String, Blobs> tracked = desiredcommit.getTrackedFiles();
            List<String> allfilesCWD = Utils.plainFilenamesIn(CWD);
            for (String filename : allfilesCWD) {
                File curfile = Utils.join(CWD, filename);
                String curcon = Utils.readContentsAsString(curfile);
                if (tracked.containsKey(filename)) {
                    String trackedcontents =
                            tracked.get(filename).getContents();
                    if (!trackedcontents.equals(curcon)) {
                        System.out.println("There is an untracked file in the"
                                + "way; delete it, or add and commit it"
                                + "first.");
                        return;
                    }
                }
            }
            for (String file : tracked.keySet()) {
                Commandclass.checkout2(commitid, file);
            }
        }
        File allbranchespath = Utils.join(COMMIT_DIR,
                "all_branches");
        _allbranches = Utils.readObject(allbranchespath,
                HashMap.class);
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        Branch headbranch = _allbranches.get(_head);
        if (headbranch.gethistory().contains(commitid)) {
            headbranch.gethistory().remove(
                    headbranch.gethistory().size() - 1);
        } else {
            headbranch.gethistory().add(commitid);
        }
        for (Branch y : _allbranches.values()) {
            if (y.getid().equals(commitid)) {
                _allbranches.replace(_head, y);
            }
        }
        Utils.writeObject(allbranchespath, _allbranches);
        File additionspath = Utils.join(STAGING_DIR, "additions");
        additions = Utils.readObject(additionspath, HashMap.class);
        File removalspath = Utils.join(STAGING_DIR, "removals");
        removals = Utils.readObject(removalspath, HashMap.class);
        additions.clear();
        removals.clear();
        Utils.writeObject(additionspath, additions);
        Utils.writeObject(removalspath, removals);
    }

    /**
     * Merges files from the given branch into the current branch.
     * The parameter NAME is the name of the branch we are merging
     * with our current/active branch.
     */
    public static void merge(String name) {
        File additionspath = Utils.join(STAGING_DIR, "additions");
        additions = Utils.readObject(additionspath, HashMap.class);
        File removalspath = Utils.join(STAGING_DIR, "removals");
        removals = Utils.readObject(removalspath, HashMap.class);
        File commitpath = Utils.join(COMMIT_DIR, "commits_tree");
        _allcommits = Utils.readObject(commitpath, HashMap.class);
        File allbranchespath = Utils.join(COMMIT_DIR, "all_branches");
        _allbranches = Utils.readObject(allbranchespath, HashMap.class);
        File headpath = Utils.join(COMMIT_DIR, "HEAD");
        _head = Utils.readObject(headpath, String.class);
        if (!additions.isEmpty() || !removals.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!_allbranches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (_head.equals(name)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Branch active = _allbranches.get(_head);
        Branch m = _allbranches.get(name);
        String activeID = active.getid();
        String mID = m.getid();
        LinkedList<String> mhistory = m.gethistory();
        LinkedList<String> activehistory = active.gethistory();
        HashMap<String, Blobs> mtrack = _allcommits.get(mID).getTrackedFiles();
        HashMap<String, Blobs> activetrack =
                _allcommits.get(activeID).getTrackedFiles();
        List<String> allfilesCWD = Utils.plainFilenamesIn(CWD);
        for (String f : allfilesCWD) {
            File onepath = Utils.join(CWD, f);
            String cwdcontents = Utils.readContentsAsString(onepath);
            if (!activetrack.containsKey(f)) {
                if (mtrack.containsKey(f)) {
                    String newcontents = mtrack.get(f).getContents();
                    if (!newcontents.equals(cwdcontents)) {
                        System.out.println("There is an untracked file in the"
                                + "way; delete it, or add and commit it"
                                + "first.");
                        return;
                    }
                }
            }
        }
        moremerge(name, mID, activeID, mhistory,
                activehistory, mtrack, activetrack);
    }

    /**
     * An extension of merge. NAME is the name of the branch we
     * are merging, MID is the id of the given branch. ACTIVEID is
     * the id of the active/current branch. MHISTORY is the
     * history of the given branch we are merging.  ACTIVEHISTORY
     * is the history of the active/current branch. MTRACK is a
     * map with the tracked files in our given branch that we are
     * merging. ACTIVETRACK is a map with the tracked files in
     * our active/current branch.
     */
    public static void moremerge(String name, String mID,
                                 String activeID,
                                 LinkedList<String> mhistory,
                                 LinkedList<String> activehistory,
                                 HashMap<String, Blobs> mtrack,
                                 HashMap<String, Blobs> activetrack) {
        String splitpt = mhistory.get(0);
        boolean found = false;
        for (int i = mhistory.size() - 1; i >= 0; i--) {
            for (int b = activehistory.size() - 1; b >= 0; b--) {
                if (mhistory.get(i).equals(activehistory.get(b))) {
                    if (!found) {
                        splitpt = mhistory.get(i);
                        found = true;
                    }
                }
                Commit mergedcom = _allcommits.get(activehistory.get(b));
                String cmes = mergedcom.getMessage();
                if (cmes.startsWith("Merged") && !found && b > i) {
                    String[] wrds = cmes.split(" ");
                    String bname = wrds[1];
                    splitpt = _allbranches.get(wrds[1]).getid();
                    found = true;
                }
            }
        }
        if (splitpt.equals(mID)) {
            System.out.println("Given branch is an"
                    + "ancestor of the current branch.");
            return;
        }
        if (splitpt.equals(activeID)) {
            System.out.println("Current branch fast-forwarded.");
            checkout3(name);
            return;
        }
        HashMap<String, Blobs> splittrack =
                _allcommits.get(splitpt).getTrackedFiles();
        LinkedList<String> modfs = new LinkedList<>();
        for (String f : mtrack.keySet()) {
            String mcon = mtrack.get(f).getContents();
            if (splittrack.containsKey(f)) {
                String splitcon = splittrack.get(f).getContents();
                if (!mcon.equals(splitcon)) {
                    if (activetrack.containsKey(f)) {
                        String activecon = activetrack.get(f).getContents();
                        if (activecon.equals(splitcon)) {
                            modfs.add(f);
                        }
                    }
                }
            }
        }
        for (String f : modfs) {
            checkout2(mID, f);
            add(f);
        }
        evenmoremerge(name, mID, splitpt, activeID,
                mtrack, activetrack, splittrack);
    }

    /**
     * Another extension of merge. NAME is the name of the branch we
     * are merging, MID is the id of the given branch. SPLITPT is
     * the splitpoint where both branches share the most recent
     * history.  ACTIVEID is the id of the active/current branch.
     * MTRACK is a map with the tracked files in our given branch
     * that we are merging. ACTIVETRACK is a map with the tracked
     * files in our active/current branch. SPLITTRACK is the map
     * of tracked files at the splitpoint of this merge. AT.
     */
    public static void evenmoremerge(String name, String mID,
                                     String splitpt, String activeID, HashMap<String, Blobs> mtrack,
                                     HashMap<String, Blobs> at, HashMap<String, Blobs> splittrack) {
        for (String f : mtrack.keySet()) {
            if (!splittrack.containsKey(f)) {
                String fcon = mtrack.get(f).getContents();
                File fpath = Utils.join(CWD, f);
                Utils.writeContents(fpath, fcon);
                add(f);
            }
        }
        for (String f : splittrack.keySet()) {
            String splitcon = splittrack.get(f).getContents();
            if (at.containsKey(f)) {
                String activecon = at.get(f).getContents();
                if (splitcon.equals(activecon) && !mtrack.containsKey(f)) {
                    rm(f);
                }
            }
        }
        boolean conflict = false;
        for (String f : at.keySet()) {
            String activecon = at.get(f).getContents();
            if (mtrack.containsKey(f)) {
                String mcon = mtrack.get(f).getContents();
                if (!mcon.equals(activecon) && splittrack.containsKey(f)) {
                    String splitcon = splittrack.get(f).getContents();
                    if (!splitcon.equals(mcon) && !splitcon.equals(activecon)) {
                        conflict = true;
                        String newcon = "<<<<<<< HEAD\n" + activecon
                                + "=======\n" + mcon + ">>>>>>>\n";
                        File fpath = Utils.join(CWD, f);
                        Utils.writeContents(fpath, newcon);
                        add(f);
                    }
                }
            }
            if (!mtrack.containsKey(f) && splittrack.containsKey(f)) {
                String splitcon = splittrack.get(f).getContents();
                if (!splitcon.equals(activecon)) {
                    conflict = true;
                    String newcon = "<<<<<<< HEAD\n" + activecon
                            + "=======\n" + ">>>>>>>\n";
                    File fpath = Utils.join(CWD, f);
                    Utils.writeContents(fpath, newcon);
                    add(f);
                }
            }
            if (!splittrack.containsKey(f) && mtrack.containsKey(f)) {
                String mcon = mtrack.get(f).getContents();
                if (!mcon.equals(activecon)) {
                    conflict = true;
                    String newcon = "<<<<<<< HEAD\n" + activecon
                            + "=======\n" + mcon + ">>>>>>>\n";
                    File fpath = Utils.join(CWD, f);
                    Utils.writeContents(fpath, newcon);
                    add(f);
                }
            }
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        commit("Merged " + name + " into " + _head + ".");
    }
}
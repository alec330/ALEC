package gitlet;

import java.io.File;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Alec Luk
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /** The timestamp of this commit. */
    private String timeStamp;
    /** the first parent reference of the commit */
    private String parent1;
    /** the second parent reference of the commit */
    private String parent2;
    /** a mapping of file names to blob references */
    private HashMap<String, String> blobs;
    /** commit's corresponding branch name. */
    private String branchName;

    /** make a commit for add in repository */
    public Commit(String message, String parent1, String parent2, String branchName) {
        this.message = message;
        this.timeStamp = setTimeStamp();
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.blobs = readFilesFromParent1();  
        this.branchName = branchName;
    }

    public String setTimeStamp() {
        if (this.message.equals("initial commit")) {
            return new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy -0800").format(new Date(0));
        }
        return new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy -0800").format(new Date());
    }

    public HashMap<String, String> readFilesFromParent1() {  
        if (this.message.equals("initial commit")) {
            return new HashMap<String, String>();
        } else {
            File parentCommitFile = join(COMMITS_DIR, parent1); 
            Commit parentCommit = readObject(parentCommitFile, Commit.class);
            return parentCommit.blobs; /** return the blobs HashMap of the parent. */
        }
    }

    public HashMap<String, String> readFilesFromParent2() {
        return this.blobs;
    }

    public String getMessage() {
        return this.message;
    }

    public String gettimeStamp() {
        return this.timeStamp;
    }

    public String getParent1() {
        return this.parent1;
    }

    public String getParent2() {
        return this.parent2;
    }

    public HashMap<String, String> getBlobs() {
        return this.blobs;
    }

    public String getbranchName() {
        return this.branchName;
    }

}

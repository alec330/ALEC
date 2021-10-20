package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import static gitlet.Utils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Alec Luk
 */
public class Repository implements Serializable {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * The REPO file.
     */
    public static final File REPO_DIR = join(GITLET_DIR, "repo");
    /**
     * The file that stores the commits, inside is a serializable hashmap
     */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    /**
     * The file that stores temporarily stores the Staged Files.
     */
    public static final File STAGEDFILES_DIR = join(GITLET_DIR, "staged");
    /**
     * The file that stores the getBlobs().
     */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /**
     * The HashMap for stageAdd
     */
    private HashMap<String, String> stageAdd;
    /**
     * The HashMap for stageRemove.
     */
    private HashMap<String, String> stageRemove;
    /**
     * the name of the current active branch.
     */
    private String currBranchName;
    /**
     * The HashMap for the branch names. [currBranchName, leadCommitsHash]
     */
    private HashMap<String, String> leadCommits;

    public Repository() { /** also for the purpose of setting up persistence. */
        stageAdd = new HashMap<>();
        stageRemove = new HashMap<>();
        currBranchName = "master";
        leadCommits = new HashMap<>();
    }

    /**
     * start of init.
     */
    public static void setupPersistence() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        } else {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        if (!COMMITS_DIR.exists()) {
            COMMITS_DIR.mkdir();
        }
        if (!STAGEDFILES_DIR.exists()) {
            STAGEDFILES_DIR.mkdir();
        }
        if (!BLOBS_DIR.exists()) {
            BLOBS_DIR.mkdir();
        }
    }

    /**
     * helper method to get variables from the repo folder.
     */
    private static Repository fromRepo() {
        return readObject(REPO_DIR, Repository.class);
    }

    /**
     * helper method to write the variables to the repo folder.
     */
    private void toRepo() {
        writeObject(REPO_DIR, this);
    }

    public static void init() {
        /** create initial commit. */
        Commit initialCommit = new Commit("initial commit", null, null, "master");
        byte[] serializedContent = serialize(initialCommit); /** serialize the content.*/
        String commitHash = sha1(serializedContent); /** produces the hashcode for the content. */
        File initialcommitFile = join(COMMITS_DIR, commitHash);
        writeObject(initialcommitFile, initialCommit);
        /** create new repository. */
        Repository repo = new Repository();
        repo.leadCommits.put(repo.currBranchName, commitHash);
        repo.toRepo();
    }

    /**
     * helper method to get the Commit given the hashcode.
     */
    private static Commit getCommit(String commitHash) {
        File commitFile = join(COMMITS_DIR, commitHash); /** goes into the commits folder. */
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }

    public static void add(String filename) {
        Repository repo = fromRepo();
        File f = join(CWD, filename);
        if (!f.exists()) {
            gitlet.Utils.message("File does not exist.");  /** first case: file does not exist. */
            System.exit(0);
        }
        byte[] newContents = readContents(f); /** new content of the to be added file. */
        String fileContentHash = sha1(newContents);
        if (repo.stageRemove.containsKey(filename)) {
            repo.stageRemove.remove(filename);
            repo.toRepo();
        }
        String headCommitHash = repo.leadCommits.get(repo.currBranchName);
        Commit headCommit = repo.getCommit(headCommitHash);
        if (headCommit.getBlobs().containsKey(filename)) {
            if (headCommit.getBlobs().get(filename).equals(fileContentHash)) {
                System.exit(0);
            }
        }
        if (repo.stageAdd.containsKey(filename)) { /** if stage add already has this file. **/
            if (repo.stageAdd.get(filename) != fileContentHash) {
                repo.stageAdd.put(filename, fileContentHash);
                File newBlob = join(BLOBS_DIR, fileContentHash); /** write it in the blobs map. */
                writeObject(newBlob, newContents);
                repo.toRepo();
            } else {
                System.exit(0); /** Abort if the same content. */
            }
        } else {
            repo.stageAdd.put(filename, fileContentHash);
            File newBlob = join(BLOBS_DIR, fileContentHash); /** write it in the blobs map. */
            writeObject(newBlob, newContents);
            repo.toRepo();
        }
    }

    public static void commit(String message) {
        /** new commits parent 1 is HEAD; parent 2 is HEAD.parent1. */
        Repository repo = fromRepo();
        if (message.isBlank()) {
            message("Please enter a commit message.");
            System.exit(0);
        } else if (repo.stageAdd.isEmpty() && repo.stageRemove.isEmpty()) {
            message("No changes added to the commit.");
            System.exit(0);
        } else {
            String headCommitHash = repo.leadCommits.get(repo.currBranchName);
            Commit headCommit = repo.getCommit(headCommitHash);
            Commit nextCommit = new Commit(message, headCommitHash, null, repo.currBranchName);
            if (!repo.stageAdd.isEmpty()) { /** if the add hashMap is null or not. */
                while (!repo.stageAdd.isEmpty()) { /** for every file in the add staging area. */
                    String filename = (String) repo.stageAdd.keySet().toArray()[0];
                    if (headCommit.getParent1() != null
                            && headCommit.getBlobs().containsKey(filename)) {
                        nextCommit.getBlobs().remove(filename);
                        String newFileBlobHash = repo.stageAdd.get(filename);
                        nextCommit.getBlobs().put(filename, newFileBlobHash);
                    } else {
                        String newFileBlobHash = repo.stageAdd.get(filename);
                        nextCommit.getBlobs().put(filename, newFileBlobHash);
                    }
                    repo.stageAdd.remove(filename); /** remove it from the add staging area. */
                }
            }
            if (!repo.stageRemove.isEmpty()) {
                while (!repo.stageRemove.isEmpty()) { /** for every file in the add staging area. */
                    String filenameRM = (String) repo.stageRemove.keySet().toArray()[0];
                    if (headCommit.getBlobs().containsKey(filenameRM)) {
                        nextCommit.getBlobs().remove(filenameRM);
                        repo.stageRemove.remove(filenameRM); /** remove it from staging remove. */
                    }
                }
            }
            /** serializing the commit */
            byte[] serializedCommit = serialize(nextCommit); /** serialize the commit content. */
            String commitHash = sha1(serializedCommit);
            File newcommitFile = join(COMMITS_DIR, commitHash);
            writeObject(newcommitFile, nextCommit); /** writing the content inside the file. */
            repo.leadCommits.put(repo.currBranchName, commitHash);
            repo.toRepo();
        }
    }

    public static void rm(String filename) {
        Repository repo = fromRepo();
        String headCommitHash = repo.leadCommits.get(repo.currBranchName);
        Commit headCommit = repo.getCommit(headCommitHash);
        if (!repo.stageAdd.containsKey(filename) && !headCommit.getBlobs().containsKey(filename)) {
            gitlet.Utils.message("No reason to remove the file.");
            System.exit(0);
        }
        if (repo.stageAdd.containsKey(filename)) {
            repo.stageAdd.remove(filename);
        }
        if (headCommit.getBlobs().containsKey(filename)) {
            File oldFile = join(CWD, filename);
            if (oldFile.exists()) {
                String fileSha = sha1(readContents(oldFile));
                repo.stageRemove.put(filename, fileSha);
                gitlet.Utils.restrictedDelete(oldFile);
            } else {
                String oldSha = headCommit.getBlobs().get(filename);
                repo.stageRemove.put(filename, oldSha);
            }
        }
        repo.toRepo();
    }

    public static void log() {
        Repository repo = fromRepo();
        String headCommitHash = repo.leadCommits.get(repo.currBranchName);
        Commit headCommit = repo.getCommit(headCommitHash);
        while (headCommit != null) {
            System.out.println("===");
            System.out.println("commit " + headCommitHash);
            System.out.println("Date: " + headCommit.gettimeStamp());
            System.out.println(headCommit.getMessage());
            System.out.print("\n");
            if (headCommit.getParent1() != null) {
                headCommitHash = headCommit.getParent1();
                File parentCommit = join(COMMITS_DIR, headCommit.getParent1());
                headCommit = readObject(parentCommit, Commit.class);
            } else {
                headCommit = null;
            }
        }
    }

    public static void globalLog() {
        for (String commitHash : gitlet.Utils.plainFilenamesIn(COMMITS_DIR)) {
            File commitFile = join(COMMITS_DIR, commitHash);
            Commit commit = readObject(commitFile, Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitHash);
            System.out.println("Date: " + commit.gettimeStamp());
            System.out.println(commit.getMessage());
            System.out.print("\n");
        }
    }

    public static void find(String message) {
        ArrayList<String> allCommits = new ArrayList<>();
        for (int i = 0; i < gitlet.Utils.plainFilenamesIn(COMMITS_DIR).size(); i++) {
            String commitHash = (String) gitlet.Utils.plainFilenamesIn(COMMITS_DIR).toArray()[i];
            File commitFile = join(COMMITS_DIR, commitHash);
            Commit commit = readObject(commitFile, Commit.class);
            String commitMessage = commit.getMessage();
            if (commitMessage.equals(message)) {
                allCommits.add(commitHash);
            } else {
                continue;
            }
        }
        if (allCommits.isEmpty()) {
            gitlet.Utils.message("Found no commit with that message.");
        } else {
            for (String hash : allCommits) {
                System.out.println(hash);
            }
        }
    }

    public static void status() {
        Repository repo = Repository.fromRepo();
        System.out.println("=== Branches ===");
        for (int i = 0; i < repo.leadCommits.size(); i++) {
            String leadName = (String) repo.leadCommits.keySet().stream().sorted().toArray()[i];
            if (leadName.equals(repo.currBranchName)) {
                System.out.println("*" + leadName);
            } else {
                System.out.println(leadName);
            }
        }
        System.out.print("\n");
        System.out.println("=== Staged Files ===");
        if (!repo.stageAdd.isEmpty()) {
            for (int i = 0; i < repo.stageAdd.keySet().size(); i++) {
                String filename = (String) repo.stageAdd.keySet().stream().sorted().toArray()[i];
                System.out.println(filename);
            }
        }
        System.out.print("\n");
        System.out.println("=== Removed Files ===");
        if (!repo.stageRemove.isEmpty()) {
            for (int i = 0; i < repo.stageRemove.keySet().size(); i++) {
                String filename = (String) repo.stageRemove.keySet().stream().sorted().toArray()[i];
                System.out.println(filename);
            }
        }
        System.out.print("\n");
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.print("\n");
        System.out.println("=== Untracked Files ===");
        System.out.print("\n");
    }

    public static void checkoutHelper(String commitID, String filename) {
        String commitHash = commitID; /** get the commit hash */
        File commitFile = join(COMMITS_DIR, commitHash);
        if (!commitFile.exists()) {
            message("No commit with that id exists.");
            System.exit(0);
        } else {
            Commit thisCommit = readObject(commitFile, Commit.class);
            if (!thisCommit.getBlobs().containsKey(filename)) {
                message("File does not exist in that commit.");
                System.exit(0);
            } else {
                String fileHash = thisCommit.getBlobs().get(filename);
                File newFileinBlobs = join(BLOBS_DIR, fileHash);
                byte[] newContents = readObject(newFileinBlobs, byte[].class);
                File oldFileInCWD = join(CWD, filename);
                if (oldFileInCWD.exists()) {
                    byte[] oldContent = readContents(oldFileInCWD);
                    if (oldContent.equals(newContents)) { /** if contents are the same. */
                        System.exit(0);
                    } else {
                        writeContents(oldFileInCWD, newContents); /** overwrite if not the same. */
                    }
                } else {
                    File newfileToCWD = join(CWD, filename);
                    writeContents(newfileToCWD, newContents);
                }
            }
        }
    }

    public static void checkoutcommitFile(String commitID, String filename) {
        if (commitID.length() == 8) {
            ArrayList<String> allCommits = new ArrayList<>(1);
            for (int i = 0; i < plainFilenamesIn(COMMITS_DIR).size(); i++) {
                String longId = (String) plainFilenamesIn(COMMITS_DIR).toArray()[i];
                if (longId.substring(0, 8).equals(commitID)) {
                    allCommits.add(longId);
                } else {
                    continue;
                }
            }
            if (allCommits.isEmpty()) {
                gitlet.Utils.message("No commit with that id exists.");
                System.exit(0);
            } else {
                checkoutHelper(allCommits.get(0), filename);
            }
        } else {
            checkoutHelper(commitID, filename);
        }
    }

    public static void checkoutOnlyFile(String filename) {
        Repository repo = fromRepo();
        String headCommitHash = repo.leadCommits.get(repo.currBranchName);
        checkoutcommitFile(headCommitHash, filename);
    }

    public static void checkoutBranch(String branchName) {
        Repository repo = fromRepo();
        /** no such branchName. */
        if (!repo.leadCommits.containsKey(branchName)) {
            gitlet.Utils.message("No such branch exists.");
            System.exit(0);
        }
        /** same branch name as current branch. */
        if (branchName.equals(repo.currBranchName)) {
            gitlet.Utils.message("No need to checkout the current branch.");
            System.exit(0);
        }
        String currBranchHash = repo.leadCommits.get(repo.currBranchName); /** curr branch hash. */
        Commit currheadCommit = repo.getCommit(currBranchHash); /** curr branch head commit. */
        String newBranchHash = repo.leadCommits.get(branchName);
        Commit newheadCommit = repo.getCommit(newBranchHash); /** new branch head commit!. */
        /** checking for untracked files (both in CWD and new branch, but not in old branch.) */
        for (String fileNames : plainFilenamesIn(CWD)) { /** all files in working directory.*/
            if (!currheadCommit.getBlobs().containsKey(fileNames)
                    && newheadCommit.getBlobs().containsKey(fileNames)
                    && !repo.stageAdd.containsKey(fileNames)) {
                message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            } else {
                continue;
            }
        }
        for (String fileNames : plainFilenamesIn(CWD)) {
            if (!newheadCommit.getBlobs().containsKey(fileNames)
                    && currheadCommit.getBlobs().containsKey(fileNames)) {
                File fileDEL = join(CWD, fileNames);
                gitlet.Utils.restrictedDelete(fileDEL);
            }
        }
        for (int i = 0; i < newheadCommit.getBlobs().size(); i++) {
            String filename = (String) newheadCommit.getBlobs().keySet().toArray()[i];
            checkoutcommitFile(newBranchHash, filename);
        }
        repo.currBranchName = branchName;
        repo.stageAdd.clear();
        repo.stageRemove.clear();
        repo.toRepo();
    }

    public static void branch(String branchName) {
        Repository repo = Repository.fromRepo();
        if (repo.leadCommits.containsKey(branchName)) {
            gitlet.Utils.message("A branch with that name already exists.");
            System.exit(0);
        }
        String headCommitHash = repo.leadCommits.get(repo.currBranchName);
        repo.leadCommits.put(branchName, headCommitHash);
        repo.toRepo();
    }

    public static void rmBranch(String branchName) {
        Repository repo = Repository.fromRepo();
        if (!repo.leadCommits.containsKey(branchName)) {
            gitlet.Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(repo.currBranchName)) {
            gitlet.Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }
        repo.leadCommits.remove(branchName);
        repo.toRepo();
    }

    public static void reset(String commitHash) {
        File commitFile = join(COMMITS_DIR, commitHash);
        if (!commitFile.exists()) {
            message("No commit with that id exists.");
            System.exit(0);
        }
        Repository repo = fromRepo();
        String currBranchHash = repo.leadCommits.get(repo.currBranchName); /** curr branch hash. */
        Commit currheadCommit = repo.getCommit(currBranchHash); /** curr branch head commit. */
        Commit newheadCommit = repo.getCommit(commitHash); /** new branch head commit!. */
        /** checking for untracked files (both in CWD and new commit, but not in old branch.) */
        for (String fileNames : plainFilenamesIn(CWD)) {
            if (!currheadCommit.getBlobs().containsKey(fileNames)
                    && newheadCommit.getBlobs().containsKey(fileNames)
                    && !repo.stageAdd.containsKey(fileNames)) {
                message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            } else {
                continue;
            }
        }
        for (String fileNames : plainFilenamesIn(CWD)) {
            if (!newheadCommit.getBlobs().containsKey(fileNames)
                    && currheadCommit.getBlobs().containsKey(fileNames)) {
                File fileDEL = join(CWD, fileNames);
                gitlet.Utils.restrictedDelete(fileDEL);
            }
        }
        for (int i = 0; i < newheadCommit.getBlobs().size(); i++) {
            String fileName = (String) newheadCommit.getBlobs().keySet().toArray()[i];
            checkoutcommitFile(commitHash, fileName);
        }
        String currName = newheadCommit.getbranchName();
        repo.leadCommits.put(currName, commitHash);
        repo.currBranchName = currName;
        repo.stageRemove.clear();
        repo.stageAdd.clear();
        repo.toRepo();
    }

    public static void merge(String branchName) {
        Repository repo = fromRepo();
        /** uncommited changes. */
        if (!repo.stageAdd.isEmpty() || !repo.stageRemove.isEmpty()) {
            gitlet.Utils.message("You have uncommitted changes.");
            System.exit(0);
        }
        /** no such branch name. */
        if (!repo.leadCommits.containsKey(branchName)) {
            gitlet.Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        /** branch name is the same as the current branch.*/
        if (repo.currBranchName.equals(branchName)) {
            gitlet.Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String currBranchHash = repo.leadCommits.get(repo.currBranchName); /** curr branch hash. */
        Commit currheadCommit = repo.getCommit(currBranchHash); /** curr branch head commit. */
        String otherBranchHash = repo.leadCommits.get(branchName);
        Commit otherheadCommit = repo.getCommit(otherBranchHash); /** new branch head commit!. */
        /** untracked file in current commit and would be overwritten. */
        for (String fileNames : plainFilenamesIn(CWD)) { /** all files in working directory.*/
            if (!currheadCommit.getBlobs().containsKey(fileNames)
                    && otherheadCommit.getBlobs().containsKey(fileNames)
                    && !repo.stageAdd.containsKey(fileNames)) {
                gitlet.Utils.message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            } else {
                continue;
            }
        }
        /** finding split point commit. */

        String splitPointHash = splitPoint(currheadCommit, otherheadCommit);
        /** if split point is the same commit as the given branch. */
        if (splitPointHash.equals(otherBranchHash)) {
            gitlet.Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
            /** if split point is the same commit as the current branch. */
        } else if (splitPointHash.equals(currBranchHash)) {
            checkoutBranch(branchName);
            gitlet.Utils.message("Current branch fast-forwarded.");
            System.exit(0);
        }
        Commit splitPointCommit = getCommit(splitPointHash);
        /** the real deal starts now. */
        /** for every file in split. */
        splitmergeCheck(splitPointCommit, currheadCommit, otherheadCommit, branchName);
        /** not in split, but in either curr or other. */
        notinsplitmergeCheck(splitPointCommit, otherheadCommit, currheadCommit, branchName);
        /** in split and curr, but not in given. */
        notingivensplitCheck(splitPointCommit, currheadCommit, otherheadCommit);
        mergeCommit("Merged" + " " + branchName + " "
                        + "into" + " " + repo.currBranchName + ".",
                currBranchHash, otherBranchHash);
    }

    public static void mergeCommit(String message, String currBranchHash, String otherBranchHash) {
        Repository repo = fromRepo();
        Commit mergeCommit = new Commit(message,
                currBranchHash, otherBranchHash, repo.currBranchName);
        if (!repo.stageAdd.isEmpty()) { /** if the add hashMap is null or not. */
            while (!repo.stageAdd.isEmpty()) { /** for every file in the add staging area. */
                String filename = (String) repo.stageAdd.keySet().toArray()[0];
                String newFileBlobHash = repo.stageAdd.get(filename);
                mergeCommit.getBlobs().put(filename, newFileBlobHash);
                repo.stageAdd.remove(filename); /** remove it from the add staging area. */
            }
        }
        if (!repo.stageRemove.isEmpty()) {
            while (!repo.stageRemove.isEmpty()) { /** for every file in the add staging area. */
                String filenameRM = (String) repo.stageRemove.keySet().toArray()[0];
                mergeCommit.getBlobs().remove(filenameRM);
                repo.stageRemove.remove(filenameRM); /** remove it from staging remove. */
            }
        }
        byte[] serializedCommit = serialize(mergeCommit); /** serialize the commit content. */
        String commitHash = sha1(serializedCommit);
        File mergecommitFile = join(COMMITS_DIR, commitHash);
        writeObject(mergecommitFile, mergeCommit); /** writing the content inside the file. */
        repo.leadCommits.put(repo.currBranchName, commitHash);
        repo.toRepo();
    }


    private static String splitPoint(Commit currheadCommit, Commit otherheadCommit) {
        String splitPoint = null;
        Integer currbranchLength = branchLength(currheadCommit);
        Integer otherbranchLength = branchLength(otherheadCommit);
        Commit currHead = currheadCommit;
        Commit otherHead = otherheadCommit;
        Integer longer;
        if (currbranchLength > otherbranchLength) {
            longer = currbranchLength - otherbranchLength;
            currHead = movebranchHead(currheadCommit, longer);
            if (sha1(serialize(currHead)).equals(sha1(serialize(otherHead)))) {
                return sha1(serialize(otherheadCommit));
            }
        }
        if (currbranchLength < otherbranchLength) {
            longer = otherbranchLength - currbranchLength;
            otherHead = movebranchHead(otherheadCommit, longer);
            if (sha1(serialize(otherHead)).equals(sha1(serialize(currHead)))) {
                return sha1(serialize(currheadCommit));
            }
        }
        if (currHead.getParent2() != null || otherHead.getParent2() != null) {
            if (currHead.getParent2() != null
                    && otherHead.getParent2() == null
                    && currHead.getParent2().equals(otherHead.getParent1())) {
                splitPoint = currHead.getParent2();
            } else if (otherHead.getParent2() != null
                    && currHead.getParent2() == null
                    && currHead.getParent2() == null
                    && otherHead.getParent2().equals(currHead.getParent1())) {
                splitPoint = otherHead.getParent2();
            } else if (currHead.getParent2() != null
                    && otherHead.getParent2() != null
                    && otherHead.getParent2().equals(currHead.getParent2())) {
                splitPoint = currHead.getParent2();
            } else {
                splitPoint = splitPoint(getCommit(currHead.getParent2()),
                        getCommit(otherHead.getParent2()));
            }
        } else if (currHead.getParent1() != null && otherHead.getParent1() != null) {
            if (currHead.getParent1().equals(otherHead.getParent1())) {
                splitPoint = currHead.getParent1();
            } else if (currHead.getParent1().equals(otherHead)) {
                splitPoint = sha1(serialize(otherHead));
            } else if (otherHead.getParent1().equals(currHead)) {
                splitPoint = sha1(serialize(currHead));
            } else {
                splitPoint = splitPoint(getCommit(currHead.getParent1()),
                        getCommit(otherHead.getParent1()));
            }
        }
        return splitPoint;
    }

    private static Commit movebranchHead(Commit branchHead, Integer longer) {
        Commit movedCommit = branchHead;
        while (longer != 0) {
            movedCommit = getCommit(movedCommit.getParent1());
            longer -= 1;
        }
        return movedCommit;
    }

    private static Integer branchLength(Commit branchHead) {
        int length = 0;
        Commit temp = branchHead;
        while (!temp.getMessage().equals("initial commit")) {
            length += 1;
            temp = getCommit(temp.getParent1());
        }
        return length;
    }


    private static String getInitialCommit() {
        for (String commitHash : plainFilenamesIn(COMMITS_DIR)) {
            File commitFile = join(CWD, commitHash);
            Commit thisCommit = readObject(commitFile, Commit.class);
            if (thisCommit.getMessage().equals("initial commit")) {
                return commitHash;
            }
            continue;
        }
        return null;
    }

    private static void mergebothexistConflict(Commit currheadCommit,
                                               Commit otherheadCommit,
                                               String fileName) {
        Repository repo = fromRepo();
        File currFileBlob = join(BLOBS_DIR, currheadCommit.getBlobs().get(fileName));
        byte[] currFileContent = readObject(currFileBlob, byte[].class);
        File otherFileBlob = join(BLOBS_DIR, otherheadCommit.getBlobs().get(fileName));
        byte[] otherFileContent = readObject(otherFileBlob, byte[].class);
        String newString1 = "<<<<<<< HEAD\n";
        byte[] string1Byte = newString1.getBytes();
        String newString2 = "=======\n";
        byte[] string2Byte = newString2.getBytes();
        String newString3 = ">>>>>>>\n";
        byte[] string3Byte = newString3.getBytes();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.writeBytes(string1Byte);
        outputStream.writeBytes(currFileContent);
        outputStream.writeBytes(string2Byte);
        outputStream.writeBytes(otherFileContent);
        outputStream.writeBytes(string3Byte);
        byte[] newBytes = outputStream.toByteArray();
        File newFile = join(CWD, fileName);
        writeContents(newFile, newBytes);
        gitlet.Repository.add(fileName);
        gitlet.Utils.message("Encountered a merge conflict.");
        repo.toRepo();
    }

    private static void mergeotherexistsConflict(Commit otherheadCommit, String fileName) {
        Repository repo = fromRepo();
        File otherFileBlob = join(BLOBS_DIR, otherheadCommit.getBlobs().get(fileName));
        byte[] otherFileContent = readObject(otherFileBlob, byte[].class);
        String currFileString = "";
        byte[] currFileContent = currFileString.getBytes();
        String newString1 = "<<<<<<< HEAD\n";
        byte[] string1Byte = newString1.getBytes();
        String newString2 = "=======\n";
        byte[] string2Byte = newString2.getBytes();
        String newString3 = ">>>>>>>\n";
        byte[] string3Byte = newString3.getBytes();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.writeBytes(string1Byte);
        outputStream.writeBytes(currFileContent);
        outputStream.writeBytes(string2Byte);
        outputStream.writeBytes(otherFileContent);
        outputStream.writeBytes(string3Byte);
        byte[] newBytes = outputStream.toByteArray();
        File newFile = join(CWD, fileName);
        writeContents(newFile, newBytes);
        gitlet.Repository.add(fileName);
        gitlet.Utils.message("Encountered a merge conflict.");
        repo.toRepo();
    }

    private static void mergecurrentexistsConflict(Commit currheadCommit, String fileName) {
        Repository repo = fromRepo();
        File currFileBlob = join(BLOBS_DIR, currheadCommit.getBlobs().get(fileName));
        byte[] currFileContent = readObject(currFileBlob, byte[].class);
        String otherFileString = "";
        byte[] otherFileContent = otherFileString.getBytes();
        String newString1 = "<<<<<<< HEAD\n";
        byte[] string1Byte = newString1.getBytes();
        String newString2 = "=======\n";
        byte[] string2Byte = newString2.getBytes();
        String newString3 = ">>>>>>>\n";
        byte[] string3Byte = newString3.getBytes();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.writeBytes(string1Byte);
        outputStream.writeBytes(currFileContent);
        outputStream.writeBytes(string2Byte);
        outputStream.writeBytes(otherFileContent);
        outputStream.writeBytes(string3Byte);
        byte[] newBytes = outputStream.toByteArray();
        File newFile = join(CWD, fileName);
        writeContents(newFile, newBytes);
        gitlet.Repository.add(fileName);
        gitlet.Utils.message("Encountered a merge conflict.");
        repo.toRepo();
    }

    private static void splitmergeCheck(Commit splitPointCommit,
                                        Commit currheadCommit,
                                        Commit otherheadCommit,
                                        String branchName) {
        /** all three commits tracks the file. */
        Repository repo = fromRepo();
        String otherBranchHash = repo.leadCommits.get(branchName);
        for (int i = 0; i < splitPointCommit.getBlobs().size(); i++) {
            String fileName = (String) splitPointCommit.getBlobs().keySet().toArray()[i];
            if (splitPointCommit.getBlobs().containsKey(fileName)
                    && otherheadCommit.getBlobs().containsKey(fileName)
                    && currheadCommit.getBlobs().containsKey(fileName)) {
                /** if split and curr has the same content, other has different content.*/
                if (!splitPointCommit.getBlobs().get(fileName).
                        equals(otherheadCommit.getBlobs().get(fileName))
                        && splitPointCommit.getBlobs().get(fileName).
                        equals(currheadCommit.getBlobs().get(fileName))) {
                    /** checkout from the commit at given branch. */
                    checkoutcommitFile(otherBranchHash, fileName);
                    /** stage the file. */
                    gitlet.Repository.add(fileName);
                    /** content of curr and other are not the same, and diff from split. */
                } else if (!splitPointCommit.getBlobs().get(fileName).
                        equals(currheadCommit.getBlobs().get(fileName))
                        && !splitPointCommit.getBlobs().get(fileName).
                        equals(otherheadCommit.getBlobs().get(fileName))
                        && !currheadCommit.getBlobs().get(fileName).
                        equals(otherheadCommit.getBlobs().get(fileName))) {
                    mergebothexistConflict(currheadCommit, otherheadCommit, fileName);
                }
                /** merge conflict. exist in other */
            } else if (splitPointCommit.getBlobs().containsKey(fileName)
                    && (otherheadCommit.getBlobs().containsKey(fileName))
                    && !splitPointCommit.getBlobs().get(fileName).
                    equals(otherheadCommit.getBlobs().get(fileName))) {
                mergeotherexistsConflict(otherheadCommit, fileName);
            } else if (splitPointCommit.getBlobs().containsKey(fileName)
                    && (currheadCommit.getBlobs().containsKey(fileName))
                    && !splitPointCommit.getBlobs().get(fileName).
                    equals(currheadCommit.getBlobs().get(fileName))) {
                mergecurrentexistsConflict(currheadCommit, fileName);
            }
        }
    }

    private static void notinsplitmergeCheck(Commit splitPointCommit,
                                             Commit otherheadCommit,
                                             Commit currheadCommit,
                                             String branchName) {
        Repository repo = fromRepo();
        String otherBranchHash = repo.leadCommits.get(branchName);
        for (int i = 0; i < otherheadCommit.getBlobs().size(); i++) {
            String fileName = (String) otherheadCommit.getBlobs().keySet().toArray()[i];
            if (!splitPointCommit.getBlobs().containsKey(fileName)
                    && otherheadCommit.getBlobs().containsKey(fileName)
                    && !currheadCommit.getBlobs().containsKey(fileName)) {
                checkoutcommitFile(otherBranchHash, fileName);
                gitlet.Repository.add(fileName);
                /** merge conflict. */
            } else if (!splitPointCommit.getBlobs().containsKey(fileName)
                    && otherheadCommit.getBlobs().containsKey(fileName)
                    && currheadCommit.getBlobs().containsKey(fileName)) {
                mergebothexistConflict(currheadCommit, otherheadCommit, fileName);
            }
        }
    }

    private static void notingivensplitCheck(Commit splitPointCommit,
                                             Commit currheadCommit,
                                             Commit otherheadCommit) {
        for (int i = 0; i < currheadCommit.getBlobs().size(); i++) {
            String fileName = (String) currheadCommit.getBlobs().keySet().toArray()[i];
            if (splitPointCommit.getBlobs().containsKey(fileName)
                    && !otherheadCommit.getBlobs().containsKey(fileName)
                    && currheadCommit.getBlobs().containsKey(fileName)) {
                if (splitPointCommit.getBlobs().get(fileName).
                        equals(currheadCommit.getBlobs().get(fileName))) {
                    gitlet.Repository.rm(fileName);
                }
            }
        }
    }
}

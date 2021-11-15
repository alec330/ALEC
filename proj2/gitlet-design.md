# Gitlet Design Document

**Alec Luk**:

## Classes and Data Structures
the commit class will be taking care of the serialization and deserialization of 
the commit

### Class 1
Repository
- will handle all the actual gitlet commands by 
  reading/writing from/to the correct file, 
  setting up persistence, 
  and additional error checking.
- Main logic of the program will live
- responsible for setting up persistence:
1. checking if .gitlet exists, if not then create directory
2. 

#### Fields
1. /** The current working directory. */
   public static final File CWD = new File(System.getProperty("user.dir"));

2. /** The .gitlet directory. */ 
   public static final File GITLET_DIR = join(CWD, ".gitlet");

#### Instance variables
1. The master branch. (?)
2. The head branch. (?)
3. staging area addition. (should be a directory)
4. staging area removal. (should be a directory)



### Class 2
Commit
- this class represents a commit that will be stored in the gitlet directory
- A commit has its metadata(message, time), a mapping of file names to blob references, 
  a parent reference, and (for merges) a second parent reference

#### Fields

1. 


#### Instance variables
1. The message of this Commit. 
2. The hash of this commit. (AKA the version number)
3. The timestamp of this commit.
4. the array of blobs, which (should be a bunches of SHA1-code?)

## Algorithms

## Persistence
- 

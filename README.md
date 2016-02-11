# SourcererCC
## What is it?
SourcererCC is Sourcerer's Code Clone project. It  detects similar code in very large code bases and repositories.

## How to use it?
In order to use it one needs to follow three steps: i) parsing; ii) indexing and; iii) searching
We explaing each of these steps below:

### Step 1: Parsing

Unfortunately we have not yet published our parser, but here are the specifications of the parser in case you want to build your own. 

#### Parser
For SourcererCC to be able to find sourcecode clones in a given project, the first step is to parse the source files of the given project into the the format which is understandable by SourcererCC. 

Clone Granularities
SourcererCC can find clones at different granularity levels. The granularity levels could be file-level, method-level, block level, or statement level. SourcererCC will find clones on the granularity level at which the source files of a project are parsed. So, if the source files are parsed at method level granularity, then SourcererCC will also find clones at the method level granularity. 

Example: Parsing a java method into SourcereCC understandable format
In order to understand the parsing, please consider the following example. This example is based on a java method snippet. Please note that this example is based on method-level granularity. 

##### Java Method Snippet
``` /**
     * Execute all nestedTasks.
     */
    public void execute() throws BuildException {
        if (fileset == null || fileset.getDir(getProject()) == null) {
            throw new BuildException("Fileset was not configured");
        }
        for (Enumeration e = nestedTasks.elements(); e.hasMoreElements();) {
            Task nestedTask = (Task) e.nextElement();
            nestedTask.perform();
        }
        nestedEcho.reconfigure();
        nestedEcho.perform();
    }
   ``` 
##### Parsed Output:
```
1,2,@#@for@@::@@1,"Fileset@@::@@1,perform@@::@@2,was@@::@@1,configured"@@::@@1,throw@@::@@1,if@@::@@1,elements@@::@@1,null@@::@@2,nextElement@@::@@1,nestedTask@@::@@2,execute@@::@@1,e@@::@@3,nestedTasks@@::@@1,throws@@::@@1,getDir@@::@@1,void@@::@@1,Enumeration@@::@@1,nestedEcho@@::@@2,not@@::@@1,new@@::@@1,getProject@@::@@1,fileset@@::@@2,hasMoreElements@@::@@1,Task@@::@@2,public@@::@@1,reconfigure@@::@@1,BuildException@@::@@2
```

#### Explanation of the Parsed Output:

In the parsed output file, each method is represented in a newline. Had we be parsing the source files at file level granularity, each line in the parsed output line would represent one source file. In a line, 3 delimiters are used which should be applied in the following order
1. @#@  (this only occurs once)
2. , (read comma)
3. @@::@@

So first when we split on @#@, we get two strings (LHS and RHS of
@#@ delimiter). The LHS string is the <parentId, blockId> used to represent the method.
In the above case this is 2 is the parent id and 1 is the block id. We explain <parentId, blockId> in detail later. 

Now, we split the remaining string (RHS) using ',' (comma). And we get
all the tokens (and their frequency) of the method body (including
the method name). For example, in the above case, we'd get the following
strings after splitting on ',' (comma)
```
for@@::@@1
"Fileset@@::@@1
perform@@::@@2
…
```
the token perform@@::@@2 in the above example means that the term “perform” is present 2 times in the given method. 

#### What is <parentId, blockId> pair?

##### blockId:
A blockId is a unique id that identifies a line in the parsed output file. A piece of code could be at any granularity level - file, method, block, or segment. For the above example “2” uniquely identifies the entire method. SourcererCC will report the clones using these blockIds. For example, if there are two duplicate methods with blockId 31 and 89, SourcererCC will report them as clones (31, 89) using their blockIds separated with a “,”. 
There are three requirement for these blockIds.
they should be positive integers. (including Java long type)
they should be unique
they should be in increasing order. (In order to not compare two blocks more than once, SourcererCC only compares a block with those blocks that have higher blockIds)

##### parentId:

Unlike blockIds, parentIds are not unique. More than one line in the parsed output file can have same parentIds. ParentId is used to tell SourcererCC, that a line in the parsed output file belongs to a group. SourcererCC does not compute clones between two lines if they both have same parentIds. 
To understand how parentId is useful, let’s consider a scenario. 
Suppose we have a big repository of say 10 java projects. We want to find file level clones and we do not want to find intra-project clones. A user would then use a file level parser to create a parsed output file. Please note, because the user is using a file level parser, every line in this parsed output file will represent a source file. Every line in the parsed output file will have a unique blockId, and the lines that came from same project will have same parentId. This way SourcererCC would only compute clones from the lines that have different parentIds.
In case users doesn’t want to create any groups, they should specify the parentId in all lines as negative 1 (-1).

There are 2 requirements for the parentIds.
they should be positive integers (including Java long type). In case a user does not want to create groups, specify -1 as parentId for all blocks. 
each group should have a unique parentId. More than one lines in the parsed output file, however, can have same parentId. 


#### Tracking code-snippets from blockIds
SourcererCC reports clone pairs in the following format: blockId,blockId. In order to be able to track the code snippets of the clone pairs, the parser should generate a bookkeeping file containing following information
parentId, blockId, filesystem path to the file where the code snippet exists, starting line number of the code snippet, ending line number of the code snippet. 
Currently SourcererCC doesn’t support reading this bookkeeping file. It will be done in the future releases. 

### How to run SourcererCC
Before we move further, I recommend creating the following directory structure:
SourcererCC
├── LICENSE
├── README.md
├── dist
│   └── indexbased.SearchManager.jar
├── input
│   ├── bookkeping
│   ├── dataset
│   └── query
├── sourcerer-cc.properties

The first step is to configure some necessary properties in the sourcerer-cc.properties file. Below are the properties that a user must specify
```
DATASET_DIR_PATH=input/dataset
```
This is where the parsed output files (output files created by the parser) should be kept. 
```
QUERY_DIR_PATH=input/query
```
This is where the query files should be kept. Query files are created exactly the way the dataset files are created. They have exactly same format. In case you want to find intra-dataset clones, we suggest you provide the location to the dataset folder, i.e., input/dataset.

```
IS_STATUS_REPORTER_ON=true
```
While SourcererCC is running in search mode, it can print how many queries it has processed on the outstream. This could be turned off by setting IS_STATUS_REPORTER_ON=false
```
PRINT_STATUS_AFTER_EVERY_X_QUERIES_ARE_PROCESSED=250
```
User can configure, after how many queries should SourcererCC print the status report on the outstream. The above setting would mean that SourcererCC will print the status report after every 250 queries are processed. 

### Step 2: Index
The next step is to index the dataset. Use the following command to create the index. We will explain the parameter to jar later.
```
java -jar dist/indexbased.SearchManager.jar index 8
```
### Step 3: Search
Now, to detect clones, execute the following command
```
java -jar dist/indexbased.SearchManager.jar search 8
```
Explaining arguments in the index and search 
the jar expects two arguments:
action : index/search and,
similarity threshold : an integer between 1-10 (both inclusive)

The action “index” is to notify SourcererCC that we want to create fresh indexes of the dataset. The action “search” is to notify SourcererCC that we want to detect the clones. The second argument, similarity threshold, tells SourcererCC  to detect clones with a minimum similarity threshold. For example, a similarity threshold of 7 would mean we want to detect clone pairs that are 70% similar. 
Please note that the similarity threshold for both actions, index and search, should be same. That is, if you are using similarity threshold of 7 while indexing, then you should use the same similarity threshold while detecting clones. 




 

# SourcererCC
## What is it?
SourcererCC is Sourcerer's Code Clone project. It  detects similar code in very large code bases and repositories.

## How to use it?
In order to use it one needs to follow three steps: 
 1. parsing
 2. indexing 
 3. searching
We explain each of these steps below:

### Step 1: Parsing

#### for Java, C, and C++ projects only
**Make sure you have Java 8 installed.** 

Follow the following steps to parse a project.
 1. Download and install TXL from [Here](http://www.txl.ca "txl")
 2. Click [Here](http://mondego.ics.uci.edu/projects/clonedetection/files/dist/tool.zip "SourcererCC tool") to download the zip containing executable jars of SourcererCC and InputBuilderClassic.jar.
 3. Unzip the tool.zip.
 4. Using terminal, change directory to SourcererCC/parser/java. 
 5. Execute the following command:

```
java -jar InputBuilderClassic.jar /input/path/src/ /path/blocks.file /path/headers.file functions java 0 0 10 0 false false false 8
```

##### Explaining the parameters to the above command:
 1. Path to the folder containing source files. (will search recursively)
 2. Path where the parsed output file should get generated. Make sure that the path you enter exists on the file system. The file will be created automatically by the InputBuilderClassic.jar. 
 3. Path where the bookkeeping files should get generated. This file contains the mapping of code snippet ids and their path in the filesystem. 
 4. Granularity (functions or blocks(only for Java)). A function is a Java method or a C function. A block is  a code snippet within curly braces -`{}`. 
 5. Language of the source files. Choose one of *cpp*, *java*, or *c*
 6. minTokens: A code snippet should have at least these many tokens to be considered for parsing. Setting the minTokens = 0 means no bottom limit
 7. maxTokens: A code snipper should have at most these many tokens to be considered for parsing. Setting the maxTokens = 0 means no upper limit
 8. minLines: A code snippet should have at least these many lines to be considered for parsing. Setting the minLines = 0 means no bottom limit
 9. maxLines: A code snippet should have at most these many lines to be considered for parsing. Setting the maxLines = 0 means no upper limit
 10. leave it as false
 11. leave it as false
 12. leave it as false 
 13. # of threads. If you are not sure, set it to 8.  

setting the minTokens/minLines = 0 means no bottom limit, setting maxTokens/maxLines = 0 means no upper limit.
 

#### Parser specifications, in case you want to build your own. 

For SourcererCC to be able to find source-code clones in a given project, the first step is to parse the source files of the given project into the the format which is understandable by SourcererCC. 

##### Clone Granularity
SourcererCC can find clones at different granularity levels. The granularity levels could be 
 1. file-level, 
 2. method-level, 
 3. block level, or 
 4. statement level. 

SourcererCC will find clones on the granularity level at which the source files of a project are parsed. So, if the source files are parsed at the method level granularity, then SourcererCC will also find clones at the method level granularity. 

##### Example: Parsing a java method into SourcereCC understandable format
In order to understand the parsing, please consider the following example. This example is based on a java method snippet. Please note that this example is based on method-level granularity. 

##### Java Method Snippet
``` 
     /**
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
1,2@#@for@@::@@1,"Fileset@@::@@1,perform@@::@@2,was@@::@@1,configured"@@::@@1,throw@@::@@1,if@@::@@1,elements@@::@@1,null@@::@@2,nextElement@@::@@1,nestedTask@@::@@2,execute@@::@@1,e@@::@@3,nestedTasks@@::@@1,throws@@::@@1,getDir@@::@@1,void@@::@@1,Enumeration@@::@@1,nestedEcho@@::@@2,not@@::@@1,new@@::@@1,getProject@@::@@1,fileset@@::@@2,hasMoreElements@@::@@1,Task@@::@@2,public@@::@@1,reconfigure@@::@@1,BuildException@@::@@2
```

#### Explanation of the Parsed Output:

In the parsed output file, each method is represented in a newline. Had we be parsing the source files at file level granularity, each line in the parsed output line would represent one source file. In a line, 3 delimiters are used which should be applied in the following order:

 1. `@#@`  (this only occurs once)
 2. `,` (read comma)
 3. `@@::@@`

So first when we split on `@#@`, we get two strings (LHS and RHS of
`@#@` delimiter). The LHS string is the `<parentId, blockId>` used to represent the method.
In the above case, 1 is the parent id and 2 is the block id. We explain `<parentId, blockId>` in detail later. 

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
the token `perform@@::@@2` in the above example means that the term “perform” is present 2 times in the given method. 

#### What is `<parentId, blockId>` pair?

##### blockId:
A blockId is a unique id that identifies a line in the parsed output file. A piece of code could be at any granularity level - file, method, block, or segment. For the above example “2” uniquely identifies the entire method. SourcererCC will report the clones using these blockIds. For example, if there are two duplicate methods with blockId 31 and 89, SourcererCC will report them as clones (31, 89) using their blockIds separated with a “,”. 
There are three requirement for these blockIds.

 1. they should be positive integers. (including Java long type)
 2. they should be unique
 3. they should be in increasing order. (In order to not compare two blocks more than once, SourcererCC only compares a block with those blocks that have higher blockIds)

##### parentId:

Unlike blockIds, parentIds are not unique. More than one line in the parsed output file can have same parentIds. ParentId is used to tell SourcererCC, that a line in the parsed output file belongs to a group. SourcererCC does not compute clones between two lines if they both have same parentIds. 
To understand how parentId is useful, let’s consider a scenario. 
Suppose we have a big repository of say 10 java projects. We want to find file level clones and we do not want to find intra-project clones. A user would then use a file level parser to create a parsed output file. Please note, because the user is using a file level parser, every line in this parsed output file will represent a source file. Every line in the parsed output file will have a unique blockId, and the lines that came from same project will have same parentId. This way SourcererCC would only compute clones from the lines that have different parentIds.
In case users doesn’t want to create any groups, they should specify the parentId in all lines as negative 1 (-1).

There are 2 requirements for the parentIds.
 1. they should be positive integers (including Java long type). In case a user does not want to create groups, specify -1 as parentId for all blocks. 
 2. each group should have a unique parentId. More than one lines in the parsed output file, however, can have same parentId. 


#### Tracking code-snippets from blockIds
SourcererCC reports clone pairs in the following format: blockId,blockId. In order to be able to track the code snippets of the clone pairs, the parser should generate a bookkeeping file containing following information
parentId, blockId, filesystem path to the file where the code snippet exists, starting line number of the code snippet, ending line number of the code snippet. 
Currently SourcererCC doesn’t support reading this bookkeeping file. It will be done in the future releases. 

### How to run SourcererCC

Click [Here](http://mondego.ics.uci.edu/projects/clonedetection/files/dist/tool.zip "SourcererCC tool") to download the zip containing executable jar of SourcererCC. Alternatively, you may also clone the SourcererCC project to your workstation and then run the following ant command to build the executable jar.

``` ant clean cdi ```
   
Before we move further, I recommend creating the following directory structure:
```
SourcererCC
├── LICENSE
├── README.md
├── dist
│   └── indexbased.SearchManager.jar
├── input   
│   ├── dataset
│   └── query
├── sourcerer-cc.properties

```
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




 

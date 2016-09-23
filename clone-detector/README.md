# Very Basic Draft

# Create Index Configuration
file: sourcerer-cc.properties
  set  following:
  ```IS_SHARDING=true
  MIN_TOKENS=65
  MAX_TOKENS=500000
  SHARD_MAX_NUM_TOKENS=<comma seperated list of numbers. 75,90,100  would mean we want 4 shards: 65-75, 76-90, 91-100, 101-500000>
  ```
  
  
# Running SourcererCC
execute
`ant cdi`
## Step 1: Init
Modify runnodes.sh, only one for loop should exist there. It should look like as shwn below. Here num_nodes is the number of processes you want to create to do this step. 
```
for i in $(seq 1 1 $num_nodes)
do
    java -Dproperties.location="$rootPATH/NODE_$i/sourcerer-cc.properties" -Xms10g -Xmx10g  -jar dist/indexbased.SearchManager.jar init $threshold &
done
```
Now run, `./runnodes.sh `
if you want to run 100 processes, run `./runnodes.sh 100`

# Step 2: Index
Modify runnodes.sh, only one for loop should exist there. It should look like as shwn below.
```
for i in $(seq 1 1 $num_nodes)
do
    java -Dproperties.location="$rootPATH/NODE_$i/sourcerer-cc.properties" -Xms20g -Xmx20g  -jar dist/indexbased.SearchManager.jar index $threshold &
done
```
# step 3: Merge
`ant cdmerge`
```java -Dproperties.location="$rootPATH/sourcerer-cc.properties" -Xms20g -Xmx20g  -jar dist/indexbased.IndexMerger.jar merge```
 
# step 4: Search

In sourcerer-cc.properties, set the min_tokens and max_tokens values. Files with tokens between min_tokens and max_tokens will be considered, rest will be ignored.

Set
SEARCH_SHARD_ID=<shrad id> 
shard id is 1 for 65-75, 2 for 76-90, and so on. (Yes it is manual right now)

Modify runnodes.sh, only one for loop should exist there. It should look like as shown below.
```
for i in $(seq 1 1 $num_nodes)
do
    java -Dproperties.location="$rootPATH/NODE_$i/sourcerer-cc.properties" -Xms2g -Xmx2g  -jar dist/indexbased.SearchManager.jar search $threshold &
done
```

library(RMySQL)

mydb = dbConnect(MySQL(), user='pribeiro', password='pass', dbname='CPP', host='127.0.0.1')

runQuery <- function(query) {
  return(fetch(dbSendQuery(mydb, query), n=-1))
}

# Listing tables
dbListTables(mydb)

# Number of projects
n_projects = runQuery("SELECT COUNT(*) FROM projects;")

# Number of files
n_files = runQuery("SELECT COUNT(*) FROM files;")

# Empty projects
runQuery("SELECT COUNT(projectId) from projects WHERE projectId NOT IN (SELECT projectId FROM files);")

# Files per project
files_per_project = runQuery("SELECT count(projectId) FROM files GROUP BY projectId;")
summary(files_per_project)

##### 'per file' stats
# Lines per file
  # JOIN necessary to avoid hash-reduction to interfere with distribution of variables
lines_per_file = runQuery("SELECT sts.fileLines FROM files JOIN stats as sts ON files.fileHash=sts.fileHash")
summary(lines_per_file$fileLines)
hist(log10(lines_per_file$fileLines))

# Bytes per file
bytes_per_file = runQuery("SELECT sts.fileBytes FROM files JOIN stats as sts ON files.fileHash=sts.fileHash")
summary(bytes_per_file$fileBytes)
hist(log10(bytes_per_file$fileBytes))

# LOC per file
loc_per_file = runQuery("SELECT fileLOC FROM files JOIN stats as sts ON files.fileHash=sts.fileHash")
summary(loc_per_file$fileLOC)
hist(log10(loc_per_file$fileLOC))

# SLOC per file
sloc_per_file = runQuery("SELECT fileSLOC FROM files JOIN stats as sts ON files.fileHash=sts.fileHash")
summary(sloc_per_file$fileSLOC)
hist(log10(sloc_per_file$fileSLOC))

# Total tokens per file
totaltokens_per_file = runQuery("SELECT totalTokens FROM files JOIN stats as sts ON files.fileHash=sts.fileHash")
summary(totaltokens_per_file$totalTokens)
hist(log10(totaltokens_per_file$totalTokens))

# Unique tokens per file
uniquetokens_per_file = runQuery("SELECT uniqueTokens FROM files JOIN stats as sts ON files.fileHash=sts.fileHash")
summary(uniquetokens_per_file$uniqueTokens)
hist(log10(uniquetokens_per_file$uniqueTokens))

##### 'file cloning stats'
files_with_CC_clones = runQuery("SELECT COUNT(DISTINCT( COALESCE(fileId1,fileId2) )) FROM CCPairs;")

#Top 10 most cloned files by CC (hard query, have to think about it)

n_tokenHashes = runQuery("SELECT COUNT( DISTINCT(tokenHash) ) FROM stats")

##### Headers vs source code as token and unique tokens distributions
headers_tokens = runQuery("SELECT totalTokens FROM files AS fls JOIN stats AS sts ON fls.fileHash=sts.fileHash AND (fls.relativePath LIKE '%.hpp' OR fls.relativePath LIKE '%.h')")
summary(headers_tokens)
hist(log10(headers_tokens$totalTokens))

source_tokens = runQuery("SELECT totalTokens FROM files AS fls JOIN stats AS sts ON fls.fileHash=sts.fileHash AND fls.relativePath NOT LIKE '%.hpp' AND fls.relativePath NOT LIKE '%.h'")
summary(source_tokens)
hist(log10(source_tokens$totalTokens))

headers_unique_tokens = runQuery("SELECT uniqueTokens FROM files AS fls JOIN stats AS sts ON fls.fileHash=sts.fileHash AND (fls.relativePath LIKE '%.hpp' OR fls.relativePath LIKE '%.h')")
summary(headers_unique_tokens)
hist(log10(headers_unique_tokens$uniqueTokens))

source_unique_tokens = runQuery("SELECT uniqueTokens FROM files AS fls JOIN stats AS sts ON fls.fileHash=sts.fileHash AND fls.relativePath NOT LIKE '%.hpp' AND fls.relativePath NOT LIKE '%.h'")
summary(source_unique_tokens)
hist(log10(source_unique_tokens$uniqueTokens))

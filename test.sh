# move to folder with SourcererCC/ and results will be in test/*
# Current output:
# rprtr258@myvm:~$ sudo ./test.sh
# Blocks mode:
# 11,100053000017,11,100043000017
# 11,100053000030,11,100053000011
# 11,100093000047,11,100043000047
# Files mode:
# 1,13,1,6
# 1,15,1,14
# 1,19,1,17
# 1,20,1,19
# 1,20,1,17
# 1,21,1,8
# 1,22,1,19
# 1,22,1,20
# 1,22,1,17
# 1,23,1,19
# 1,23,1,20
# 1,23,1,22
# 1,23,1,17
# 1,24,1,21
# 1,24,1,8
# 1,25,1,10
# 1,26,1,10
# 1,26,1,25
# 1,27,1,21
# 1,27,1,24
# 1,27,1,8
# 1,28,1,21
# 1,28,1,24
# 1,28,1,8
# 1,28,1,27
# 1,29,1,21
# 1,29,1,24
# 1,29,1,8
# 1,29,1,27
# 1,29,1,28
# 1,33,1,14
# 1,33,1,15
# 1,35,1,17
# 1,35,1,19
# 1,35,1,20
# 1,35,1,22
# 1,35,1,23
# 1,52,1,50
# 1,54,1,50
# 1,54,1,52
# 1,57,1,56
# 4,106,4,99
# 4,138,4,133
# 4,153,4,152
rm -rf test/
mkdir test
cp SourcererCC/ test/rprtr258-SourcererCC-copy/ -r
cd test
mkdir t1 t2
cp rprtr258-SourcererCC-copy/ t1/ -r
cp rprtr258-SourcererCC-copy/ t2/ -r
mkdir ./t1/rprtr258-SourcererCC-copy/clone-detector/input/
mkdir ./t1/rprtr258-SourcererCC-copy/clone-detector/input/dataset/
mkdir ./t2/rprtr258-SourcererCC-copy/clone-detector/input/
mkdir ./t2/rprtr258-SourcererCC-copy/clone-detector/input/dataset/
cd ./t1/rprtr258-SourcererCC-copy/
./runSourcererCC-BlocksMode.sh >../../blocks.out
echo "Blocks mode:"
cat results.pairs
cd ../../t2/rprtr258-SourcererCC-copy/
./runSourcererCC-FilesMode.sh >../../files.out
echo "Files mode:"
cat results.pairs

# move to folder with SourcererCC/ and results will be in test/*
set -e
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
./runSourcererCC-BlocksMode.sh
if [ -s "results.pairs" ]
then
   echo "Blocks mode:"
   cat results.pairs
else
   echo "Blocks mode found nothing"
   exit 1
fi
cd ../../t2/rprtr258-SourcererCC-copy/
./runSourcererCC-FilesMode.sh
if [ -s "results.pairs" ]
then
   echo "Files mode:"
   cat results.pairs
else
   echo "Files mode found nothing"
   exit 1
fi

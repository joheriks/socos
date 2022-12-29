#!/bin/bash

TESTDIR1=./acceptancetests/basic
TESTDIR2=./rejectiontests
SOCOS=./socos

# Check that socos can be executed
$SOCOS --help >/dev/null || exit 1

echo "### Acceptance testing commenced, please wait..."
echo "    (this can take several minutes)"

# Create tmp dirs and copy tests
TMPDIR1=$(mktemp -d /tmp/socosaccXXX)
echo "Copying acceptance tests to" $TMPDIR1
cp $TESTDIR1/*.ibp $TMPDIR1
TMPDIR2=$(mktemp -d /tmp/socosrejXXX)
echo "Copying rejection tests to" $TMPDIR2
cp $TESTDIR2/*.ibp $TMPDIR2

before="$(date +%s)"
errorlevel=0

echo "--------------------------"
 
# Check that all files in acceptancetests can be proved 
echo "### Running acceptance tests (errors are bad)..."
$SOCOS $TMPDIR1/*.ibp || errorlevel=1

echo "--------------------------"

# Create tmp dirs and copy tests
TMPTEST1=$(mktemp -d /tmp/socostestXXX)
echo "Copying acceptance tests to" $TMPTEST1
cp $TESTDIR1/*_test.ibp $TMPTEST1


echo "### Running acceptance tests in trace mode (errors are bad)..."

TESTFILES=$TMPTEST1/*_test.ibp

for F in $TESTFILES;
  do $SOCOS --trace $F || errorlevel=1;
done

echo "--------------------------"

# Check that each file in rejectiontests fails
echo "### Running rejection tests (errors are good)..."
for F in $TMPDIR2/*.ibp; do
    echo "R:" $F
    $SOCOS $F && errorlevel=1 && break;
done

echo "--------------------------"

after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"

if [ $errorlevel == 0 ]; then
    echo "### Acceptance tests SUCCEEDED in $elapsed_seconds seconds.";
else
    echo "### Acceptance tests FAILED.";
fi

exit $errorlevel


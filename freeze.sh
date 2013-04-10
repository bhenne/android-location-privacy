#/bin/bash
#
# Get latest commits of all git repositories checked out
# using repo. This exactly is our code base before modification.
#
# usage: ./freeze.sh <path to android source>
# output: git repositories' path and HEAD commit id

CURDIR=`pwd`
cd $1
export XPWD=`pwd`; for F in `find $XPWD -name ".git"`; do cd $XPWD; XDIR=`echo -n $F | sed -e 's/.git//;'`; cd $XDIR; echo -n $(echo $XDIR |sed -e s#$XPWD\/##); echo -n " " ; git rev-parse HEAD; done; cd $XPWD;
cd $CURDIR

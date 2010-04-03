#!/bin/sh
# If a path name is a symbolic link, resolve it.
real_path()
{
  path="$1"
  level=1
  while [ -h "$path" ] ; do
    level=`expr $level + 1`
    if [ $level -gt 10 ] ; then
      echo "$path: too many levels ($level) of symbolic links"
      break;
    fi
    # First, make sure we have an absolute path name. ( begin with "/")
    if [ "`echo "$path" | sed -e 's,^/.*,abs,g'`" != "abs" ] ; then
      path=`pwd`/"$path"
    fi
    # Then determine where the link points (via "ls -l")
    dir=`dirname $path`
    link=`ls -l $path | sed -e 's,.* -> ,,g'`

    if [ `echo "$link" | sed -e 's,^/.*,abs,g'` != "abs" ]; then
      path="$dir"/"$link"
    else
      path="$link"
    fi
  done
  echo "$path"
}
JPERF_DIR=`dirname $(real_path $0)`
cd $JPERF_DIR
java -classpath jperf.jar:lib/forms-1.1.0.jar:lib/jcommon-1.0.10.jar:lib/jfreechart-1.0.6.jar:lib/swingx-0.9.6.jar net.nlanr.jperf.JPerf

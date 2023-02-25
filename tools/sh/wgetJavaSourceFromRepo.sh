#!/usr/bin/env bash

# Script for downloading Java sources for particular classes from a remote repo that provides web-based access to
# raw files.  For example, the OpenJDK mercurial repo (https://hg.openjdk.java.net/)
# or the GWT project GitHub repo (https://raw.githubusercontent.com/${username}/gwt/master/user/src)
#
# Example:
# > wgetJavaSourceFromRepo.sh java.util.ArrayList
# (will download https://hg.openjdk.java.net/jdk8/jdk8/jdk/raw-file/687fd7c7986d/src/share/classes/java/util/ArrayList.java)

# Default settings:
repoBaseUrl="https://hg.openjdk.java.net/jdk8/jdk8/jdk/raw-file/687fd7c7986d/src/share/classes/"
packageName=""
verbose=0

# NOTE:
#   this script can probably be used to download any java code from any repo that provides web-based access to raw files
#   (user would just have to specify a custom URL using the [-u BASE_URL] option to this script)
#   TODO: test whether that works, and update documentation accordingly if so


# Usage info
show_help() {
cat << EOF

USAGE:
    ${0##*/} [-h?v] [-u BASE_URL] [-p SRC_PACKAGE_NAME] [-R DEST_PACKAGE_NAME] [CLASS_NAME]...

DESCRIPTION:
    Downloads the source code of the specified Java classes from a remote VCS repository
    that provides web-based access to raw files, such as the OpenJDK Mercurial repo
    (https://hg.openjdk.java.net/) or GitHub

ARGS:
  CLASS_NAME:
    Fully-qualified name of a class (unless SRC_PACKAGE_NAME is specified with the -p option)

    If SRC_PACKAGE_NAME is specified, each CLASS_NAME will be treated as a simple name and resolved against SRC_PACKAGE_NAME

OPTIONS:
    -h, -?
        Display this help text and exit
    -u BASE_URL
        URL prefix for the (raw) .java files in the repo
        (the resolved portion of the file path will be appended to this prefix)
        default: ${repoBaseUrl}
    -p SRC_PACKAGE_NAME
        Treat each CLASS_NAME as a simple name resolved against this package name
    -R DEST_PACKAGE_NAME
        Rewrite the package declaration inside the file from SRC_PACKAGE_NAME to this value
    -v
        Verbose mode

Examples:

> ${0##*/} java.util.ArrayList
Downloads ${repoBaseUrl}java/util/ArrayList.java

> ${0##*/} -p java.util ArrayList
Same effect as above

> ${0##*/} -p java.util ArrayList TreeMap
Downloads 1. ${repoBaseUrl}java/util/ArrayList.java
      and 2. ${repoBaseUrl}java/util/TreeMap.java

> ${0##*/} -p java.util ArrayList TreeMap -R com.example
Same as above, but replaces every "package java.util;" line in the output
                             with "package com.example;  // copied from java.util"
EOF
}

display_error_and_exit () {
  # prints the given error message along with usage help to stderr (1>&2) and exits with error status 1
  echo "$1" 1>&2
  show_help 1>&2
  exit 1
}

# First we parse the CLI options using getopts; see:
#   - http://mywiki.wooledge.org/BashFAQ/035#getopts
#   - https://stackoverflow.com/questions/192249/how-do-i-parse-command-line-arguments-in-bash
#   - https://sookocheff.com/post/bash/parsing-bash-script-arguments-with-shopts/

# OPTIND is a POSIX variable incremented by getopts for processed option
# (we want to reset it in case getopts has been used previously in the shell)
OPTIND=1

# NOTE: options that take an argument must be suffixed with a colon in the string passed to getopts (the option arg will be stored in special variable $OPTARG)
while getopts "h?vu:p:R:" opt; do
    case $opt in
        h|\?)
            show_help
            exit 0
            ;;
        v)  verbose=1
            ;;
        u)  repoBaseUrl="$OPTARG"
            ;;
        p)  packageName="$OPTARG"
            ;;
        R)  packageNameReplacement="$OPTARG"  # TODO: document this option
            ;;
        :)  display_error_and_exit "Invalid Option: -$OPTARG requires an argument"  # ':' is a special value which indicates that the current option didn't receive the arg it requires
            ;;
        *)  display_error_and_exit "Invalid Option: -$OPTARG"  # ':' is a special value which indicates that the current option didn't receive the arg it requires
            ;;
    esac
done
shift "$((OPTIND-1))"   # Discard the options and sentinel -- (http://tldp.org/LDP/Bash-Beginners-Guide/html/sect_09_07.html)

# NOTE: should validate the options here, if needed

# now the positional args to this script (following the options) will be available as $1 .. $N;
# ($@ refers to all the (remaining) args, and $# is the number of args)
if [ $# -eq 0 ]; then
    (>&2 echo "No class names specified")
    show_help >&2
    exit 1
fi


[ ${verbose} -gt 0 ] && set -x  # start logging all executed commands to console (can call `set +x` later to stop); https://stackoverflow.com/a/2853811/

# regex that will be used for splitting class full name into package and simple name
# NOTE: this regex will be used with the bash regex matching operator `=~`, which supports a very limited regex syntax (e.g. have to use [[:alpha:]] instead of \w)
readonly classNameRegex='(.*)\.([[:alpha:]]+)'  # package name captured in group 1 and simple name in group 2
# NOTE: the above regex doesn't match simple names (without package)  TODO: document this in usage info

# Parse each arg into a package name and class name, generate the corresponding URL, and download the .java file
for classNameArg in $@; do
    if [ ${packageName} ]; then
        # package name was explicitly specified: treat ${classNameArg} as a simple name (to be appended to the package name)
        classPackageName=${packageName}
        classSimpleName=${classNameArg}
        # prefix the className with packageName if specified (otherwise className will be treated as already fully-qualified)
        classFullName="${packageName}.${classNameArg}"
    else
        # package name not explicitly specified: treat ${classNameArg} as a full class name
        classFullName=${classNameArg}
        # parse out the package and simple name from the class name to be able to determine the simple .java output filename
        # using the bash regex matching operator `=~` here (see https://stackoverflow.com/a/35924143/1965404)
        # NOTE: the regex itself is given as a variable here to avoid various quoting issues mentioned on StackOverflow
        [[ ${classFullName} =~ $classNameRegex ]];
        if [ $? -ne 0 ]; then
          # regex didn't match (`$?` provides the status code of the last executed command; 0 means success, otherwise failed)
          (>&2 echo "Unable to parse class name ${classNameArg} (Hint: if it's not fully-qualified, use the -p option to specify a package name)")
          show_help >&2
          exit 1
        fi
        # regex matched (the capturing groups can be obtained from the special array variable ${BASH_REMATCH[@]}
        classPackageName=${BASH_REMATCH[1]}  # group 1
        classSimpleName=${BASH_REMATCH[2]}   # group 2
    fi
    # replace dots with slashes and append the ".java" extension to get the file path for URL
    # (using the simple string "replace all" syntax ${string//substring/replacement} here; see http://tldp.org/LDP/abs/html/string-manipulation.html#SUBSTRREPL01)
    filePathSuffix="${classFullName//.//}.java"
    targetUrl=${repoBaseUrl}${filePathSuffix}
    # download the file with wget (possibly piping it through sed to replace the package name)

    if [ ${packageNameReplacement} ]; then
        # Have to replace the package name in the source code: will pass the wget output through sed and save the result to ${outputFileName}
        outputFileName="${classSimpleName}.java"
        # wget:
        #  - using the "-O -" option to print the download file to stdout instead of saving it
        # sed:
        #  - the given expression replaces the line matching "^package ((\w+.?)+);$" with "package ${packageNameReplacement};" and an EOL comment giving the name of the original package
        #  - using -r option to support extended regex syntax (and avoid having to escape parens for capturing groups)
        #  - using the ':' char instead of the customary '/' as the separator within the expression to avoid having to escape the Java comment start chars ('//')
        #    (see http://www.grymoire.com/Unix/Sed.html#uh-2)
        wget -O - ${targetUrl} \
          | sed -r 's:^package ((\w+.?)+);$:package '"${packageNameReplacement};"'  // copied from \1:' \
          > "${outputFileName}"
    else
        # Not replacing the package name - just run a basic wget with no args
        wget ${targetUrl}
    fi
done


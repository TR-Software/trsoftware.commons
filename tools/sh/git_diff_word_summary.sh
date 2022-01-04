#!/bin/bash

# Parses the output from a `git diff --word-diff --unified=0 <commit1> <commit2>` command
# to get the number of words added/modified/removed between 2 points in history,
# Unlike `git diff --shortstat`, this script identifies modifications (not just insertions/deletions)

# **Usage**: pipe the output of `git diff --word-diff --unified=0` into this script

# **Example** (from this repo):
#  > git diff --word-diff --unified=0 29172df9 88cdcfad | ./git_diff_word_summary.sh
#      450 added
#      238 modified
#       67 removed
#
# In contrast with `git diff --shortstat`:
#  > git diff --shortstat 29172df9 88cdcfad
#   34 files changed, 733 insertions(+), 233 deletions(-)


# See Also:
# - `git diff --shortstat <commit1> <commit2>` (see http://git-scm.com/docs/git-diff and https://stackoverflow.com/a/2528129)
# - https://stackoverflow.com/a/9933440 (this script is based on that answer)

MOD_PATTERN='^.+(\[-|\{\+).*$'
ADD_PATTERN='^\{\+.*\+\}$'
REM_PATTERN='^\[-.*-\]$'

sed -nr \
    -e "s/$MOD_PATTERN/modified/p" \
    -e "s/$ADD_PATTERN/added/p" \
    -e "s/$REM_PATTERN/removed/p" \
    | sort | uniq -c

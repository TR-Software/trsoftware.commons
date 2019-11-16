# Shell Scripting Examples

This file gives some examples of useful shell commands that I have found useful.

## 1. File size statistics

Using `find`, `du`, and `awk`:
 
```
$ find . -iname 'S02E*.m4v' -exec du -b {} \; | awk '{print $0; sum+=$1; count++} END {printf "\ntotal files: %d\n total size: %5.2f GB\n   avg size: %5.2f GB", count, sum/1073741824, (sum/count)/1073741824;}'
346544568       ./Season 2 (mp4)/S02E01.m4v
298197081       ./Season 2 (mp4)/S02E02.m4v
311348712       ./Season 2 (mp4)/S02E03.m4v
328497681       ./Season 2 (mp4)/S02E04.m4v
327351881       ./Season 2 (mp4)/S02E05.m4v
351123740       ./Season 2 (mp4)/S02E06.m4v
348628354       ./Season 2 (mp4)/S02E07.m4v
324508366       ./Season 2 (mp4)/S02E08.m4v
342373059       ./Season 2 (mp4)/S02E09.m4v
334734686       ./Season 2 (mp4)/S02E10.m4v
345481471       ./Season 2 (mp4)/S02E11.m4v
345065532       ./Season 2 (mp4)/S02E12.m4v

total files: 12
 total size:  3.73 GB
   avg size:  0.31 GB
```

Breakdown:
1. apply `du -b` recursively to all files matching the pattern _`'S02E*.m4v'`_:  
**```find . -iname 'S02E*.m4v' -exec du -b {} \;```**  

1. **```awk '{print $0; sum+=$1; count++} END {printf "\ntotal files: %d\n total size: %5.2f GB\n   avg size: %5.2f GB", count, sum/1073741824, (sum/count)/1073741824;}'```**:  
    1. for each line (the _first bracketed expression_: `{print $0; sum+=$1; count++}`):
        1. print the entire line (`$0`)
        1. increment `sum` by the value of the first column (`$1`)
        1. increment `count`
    1. the bracketed expression after `END` will execute after all lines have been processed:
       contains just 1 long `printf` statement to print all the stats 

        
    
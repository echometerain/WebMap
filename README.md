# WebMap

A program that takes a webpage, takes all of its embeded links, and recursively derives more webpages with BFS.

## How to run

Compiled .jar:

    java -jar WebCrawler.jar (SessionName) (Mode) (RecursionAmount) (URLs)

Compiled .exe:

    crawl (SessionName) (Mode) (RecursionAmount) (URLs)
  
A session can be named whatever you want, and inputting the name of a previous session would use that session (if it exists in /Data).

RecursionAmount must be integer. It determines how many sites the program will process.

Multiple URLs could be placed at the end with a space seperator. It adds urls to a new or existing queue.
 
## Modes
 - -i index (this will autosave files in Data directory)
 - -e export .json (stored in (session name).json in \Data)
 - -j import .json
 - -q query (not implemented)
 - -g graph (not implemented)
 
# How to read files

All sessions are stored in (program location)\Data

There would be 3 files named (session name).index, .map, and .q

.index is a giant list of all the URLs you computed.

.map is a graph showing the relationship between each.

.q is where the program last left off (aka the number of lines in .map)

Lets say the fifth line of .map is "8 2 10 12". This means that the fifth URL in .index links to the 8th, 2nd, 10th, and 12th URL in .index

# Libraries used:
 - https://github.com/jhy/jsoup/ MIT License
 - https://github.com/apache/commons-collections Apache 2.0 Licence
 - Requires JDK 14 / Java SE 14 to run

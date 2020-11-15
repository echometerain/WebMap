# WebMap

A program that takes a webpage, takes all of its embeded links, and recursively derives more webpages with BFS.

## How to run

java -jar WebCrawler.jar (Mode) (RecursionAmount) (URL)
  
 - --index is the only Mode currently
 - RecursionAmount must be integer
 - Multiple URLs could be placed with a space seperator
 
Requires JRE 14 to run

Libraries used:
 - https://github.com/jhy/jsoup/

Under the MIT license because I think jsoup said I had to.

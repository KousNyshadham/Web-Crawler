# Web Crawler and Search Engine
I designed and implemented an application capable of crawling and searching through a subset of the web.

## General Design
Web Crawler
* Scrapes data and links to crawl with Attoparser library
* Stores data in an inverted index 

Search Engine
* Custom query language supports combination of `!`, `&`, `|`, `()`, `""`, and implicit `&`'s.
* Shunting-yard algorithm parses queries and searches through the generated index

## Results
Here is an example of searching through `https://www.netfunny.com/rhf/`
<table><tr><td><img src='Search.png'></td><td><img src='Result.png'></td></tr></table>

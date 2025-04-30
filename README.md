## Extract from zip and write to zip in Java

A project where I test extracting entries from a zip and refining the result in various ways.
There is also an api for streaming a zip using Springs StreamingResponseBody, to avoid storing the file in memory and on disc.

The part where zip contents are written and read, is inspired by Venkat Subramaniam's book Functional Programming in Java,
using "Execute around pattern". The folder "exceptions" is also inspired by Venkat Subramaniam's way of handling 
exceptions inside a stream. I can strongly recommend that book.

The way checked exceptions should be handled inside a stream is still hard to figure out in Java, but the solution
Venkat has provided is the best one I have seen so far, to simply create your own functional interfaces declared with throws. 
If Java will solve this in another manner in the future is still unclear.

More on the topic here: https://mail.openjdk.org/pipermail/jdk-dev/2019-October/003463.html

### Requirements
Java 17 and Maven
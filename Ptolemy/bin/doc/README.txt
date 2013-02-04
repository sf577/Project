$Id: README.txt,v 1.2 2006/03/08 23:09:10 cxh Exp $

This directory contains documentation for Ptolemy II.

Documentation describing Ptolemy II can be found in index.htm

To build the html documentation for the Ptolemy II Java classes,
run:
  make

To index the demonstrations and build the actor indexes, run:
  make indexActors

If you have problems, try:
  rm -rf codeDoc
  make
  make indexActors

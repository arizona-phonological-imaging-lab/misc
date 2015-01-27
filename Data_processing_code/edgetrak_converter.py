#!/usr/bin/python
# Concatenates .con files from EdgeTrak to a csv or tsv format that
# is the input of the SSANOVA script used at NYU.
# Daniel Szeredi 2014

import argparse
import os
import re
import string

def readCon(fileName):
	"""Reads in a .con file, returns..."""
	f = open(fileName,"rt")
	numFiles = ((len(f.readline().strip().split()))/2)
	splitCoords = [[]]*numFiles

	for line in f:
		coords = line.strip().split()
		for i in range(numFiles):
			splitCoords[i].append((coords[(2*i)], coords[(2*i)+1]))	
	f.close()

	print len(splitCoords)
	outFile= open('output.txt', 'w')
	for item in splitCoords[0]:
		outFile.write("%s\n" % str(item))


def main(fileName):
	readCon(fileName)

if  __name__ == '__main__':
	main(fileName='foo.con')
	

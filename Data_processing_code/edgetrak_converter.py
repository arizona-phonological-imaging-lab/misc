#!/usr/bin/python
# Converts .con files from EdgeTrak

import os
import re
import string

def readCon(fileName):
	"""Reads in a .con file, returns..."""
	f = open(fileName, 'r')
	numFiles = ((len(f.readline().strip().split()))/2)
	splitCoords = [[] for i in range(numFiles)]

	for line in f:
		i=0
		coords = line.strip().split()
		for sublist in splitCoords:
			sublist.append((coords[(2*i)], coords[(2*i)+1]))
			i+=1
	f.close()

	for frameNum in range(numFiles):
		outFile= open('output_' + str(frameNum) + '.txt' , 'w')
		i=0
		for item in splitCoords[frameNum]:
			i+=1
			outFile.write(str(i) + '\t'  + str(item[0]) + '\t' + str(item[1]) + '\n')


def main(fileName):
	readCon(fileName)

if  __name__ == '__main__':
	main(fileName='foo.con')
	

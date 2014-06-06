#!/usr/bin/python

################################
#Author: Gus Hahn-Powell
#Date: 1/29/2014
#
################################
from time import localtime, strftime
import os
import sys
import subprocess as sp
import shlex

#script to backup APIL Big Mac stuff...

backup_loc = "/Volumes/BigMacBack/backups/APIL-data"

to_backup = ["/Users/apiladmin/Desktop/Sampleharvard3",
"/Volumes/Second HD (3TB)/ScotsGaelic2013",
"/Volumes/Second HD (3TB)/ScotsGaelic_Old",
"/Volumes/Second HD (3TB)/HT_Backup",
"/Volumes/Second HD (3TB)/JHS_EnglishPalatalization",
"/Volumes/Second HD (3TB)/JHS_KoreanPalatalization",
"/Users/apiladmin/Desktop/Images_to_Trace",
"/Volumes/Second HD (3TB)/Ultraspeech",
"/Users/apiladmin/SubversionRepository",
"/Users/apiladmin/Palatoglossatron",
"/Users/apiladmin/Gus"]

def backup():
	"""

	"""
	print "Starting backup..."
	for f in to_backup:
		try:
			os.path.join("/Volumes/BigMacBack")
			command = "rsync -rtvazl --progress \"{0}\" {1}".format(f, backup_loc)
			print "Backing up \"{0}\"".format(f)
			sp.Popen(shlex.split(command)).communicate()
		except:
			print "{0} shiznat failed to backup!".format(f)
	print "Backup completed!"

def commit():
	print "Tracking new files..."
	os.chdir(backup_loc)
	sp.Popen(shlex.split("git add -vA .")).communicate()
	print "Cleaning repository..."
	sp.Popen(shlex.split("git gc")).communicate()
	commit_msg = "\"backup for {0}\"".format(strftime("%Y-%m-%d %H:%M:%S", localtime()))
	print "Committing changes..."
	sp.Popen(shlex.split("git commit -am {0}".format(commit_msg))).communicate()

if __name__ == "__main__":
	backup()
	commit()

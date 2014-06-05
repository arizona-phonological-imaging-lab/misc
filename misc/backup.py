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
	print "Backing up stuff..."
	for f in to_backup:
		try:
			destination = f[f.rfind('/')+1:]
			destination = '/'.join([backup_loc, destination])

			os.path.join("/Volumes/BigMacBack")
			command = "rsync -rtvazl \"{0}\"/* {1}".format(f, destination)
			print "Backing up \"{0}\"".format(destination)
			os.system(command)
		except:
			print "{0} shiznat failed to backup!".format(f)
	print "Finished backing up stuff..."

def commit():
	print "Committing changes"
	os.chdir(backup_loc)
	sp.Popen(shlex.split("git add *")).wait()
	commit_msg = "backup for {0}".format(strftime("%Y-%m-%d %H:%M:%S", localtime()))
	o, e = sp.Popen(shlex.split("git commit -am {0}".format(commit_msg)), stdout=sp.PIPE, stderr=sp.PIPE).communicate()
	print o
	print e

if __name__ == "__main__":
	backup()
	commit()

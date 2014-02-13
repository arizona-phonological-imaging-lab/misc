#!/usr/bin/python

################################
#Author: Gus Hahn-Powell
#Date: 1/29/2014
#
################################
import os
import sys

#script to backup APIL Big Mac stuff...

to_backup = ["/Users/apiladmin/Desktop/Sampleharvard3",
"/Volumes/Second HD (3TB)/ScotsGaelic2013",
"/Volumes/Second HD (3TB)/ScotsGaelic_Old",
"/Volumes/Second HD (3TB)/HT_Backup",
"/Volumes/Second HD (3TB)/JHS_EnglishPalatalization",
"/Volumes/Second HD (3TB)/JHS_KoreanPalatalization",
"/Users/apiladmin/Desktop/Images_to_Trace",
"/Volumes/Second HD (3TB)/Ultraspeech",
"/Users/apiladmin/SubversionRepository",
"/Users/apiladmin/Palatoglossatron"]

def backup():
	"""

	"""
	print "Backing up stuff..."
	for f in to_backup:
		try:
			destination = f[f.rfind('/')+1:]
			destination = '/'.join(["/Volumes/BigMacBack/backups", destination])

			os.path.join("/Volumes/BigMacBack")
			command = "rsync -rtv \"{0}\" {1}".format(f, destination)
			print "Backing up \"{0}\"".format(destination)
			os.system(command)
		except:
			print "{0} shiznat failed to backup!".format(f)
	print "Finished backing up stuff..."


if __name__ == "__main__":
	backup()

# -*- coding: utf-8 -*-
# with unicode support

from Tkinter import *
from random import shuffle
from datetime import datetime
import os, platform, csv, time, codecs

#set default font size
f_size = 90
#wraplength default
w_l = 1100
#concluding message
conclusion = "This concludes the experiment.  Thank you for your participation."
basedir = os.getcwd()
stuff = os.listdir(basedir)
#create a dictionary of available texts in current directory
possible = dict((os.path.splitext(f)[0], f) for f in stuff if os.path.splitext(f)[1] == '.txt')

def parameters():
    """
    Select stimuli set
    """
    #select source text
    print "Available Stimuli files: ", 
    for key in possible: print key,
    print
    selection = raw_input("Specify a stimuli source: ")
    while selection not in possible:
        print "Specified stimuli source not recognized."
        print "Options are: ",
        for key in possible: print key,
        print
        selection = raw_input("Specify a stimuli source: ")
    stim_source = possible[selection]
    #solicit a carrier phrase
    print
    selection = raw_input("Use a carrier phrase? (y/n) ")
    carrier = False
    if selection == "y":
        print
        print "Carrier phrase example: 'I like to eat {0}.'"
        carrier = raw_input("Specify a carrier phrase: ")
        print
        while '{0}' not in carrier:
        	print "Improper format."
        	print
        	print "Carrier phrase example: 'I like to eat {0}.'"
        	carrier = raw_input("Please specify a carrier phrase: ")
        	print
    return stim_source, carrier
 
#get stimuli source
stim_source, carrier = parameters()

f = codecs.open(str(stim_source), 'r', 'utf-8')
lines = f.readlines()
stuff = [s.encode('utf-8').rstrip() for s in lines]
csv_file = 'stimulus_response.csv'
if os.path.exists(csv_file):
	#change csv file name to time last modified
	new_name = str(int(os.path.getctime(csv_file))) + csv_file
	os.rename(csv_file, new_name)
c = open('stimulus_response.csv', 'w')
stim = csv.writer(c)

set_length = len(stuff)

def randomize(stuff, reps=2):
	"""
	create N random repetitions of the stimuli
	"""
	stimuli = []
	shuffle(stuff)
	first = stuff
	stimuli+=first
	#
	shuffle(stuff)
	next = stuff
	last = stimuli[-1]
	while reps > 1:
		if (last == next[0]):
			#print "stimuli overlapped! Randomizing again..."
			shuffle(next)
		else:
			#print "no overlap!"
			stimuli+=next
			reps -= 1
			last = stimuli[-1]
			shuffle(next)
	return stimuli


#Get the number of desired repetitions
reps = raw_input("Specify the number of repetitions: ")
try: reps = int(reps)
except: ValueError
while type(reps) is not int:
	print
	print "Please specify an integer"
	reps = raw_input("Specify the number of repetitions: ")
	try: reps = int(reps)
	except: ValueError


stimuli = randomize(stuff, reps)

#iterate through stimuli
stim_num = (i for i in xrange(len(stimuli)))

#create dictionary of index to set number
stim_set = 0
set_seq = dict()
for i in xrange(len(stimuli)):
	if i % set_length == 0:
		stim_set += 1
	set_seq[i] = stim_set


def key(event):
	try:
		i = stim_num.next()
		current_set = set_seq[i]
		current = stimuli[i]
		response_time = datetime.now()
		stim.writerow([current_set, current, response_time])
		if carrier:
			current = carrier.format(current)
		stimulus.set(current)
	except: 
		StopIteration
		#stimulus.set("Finished!")
		response_time = datetime.now()
		stim.writerow(['--', 'END', response_time])
		w.destroy()

def start(event):
	response_time = datetime.now()
	stim.writerow(['--', 'PALATE_IMAGE', response_time])
	s.destroy()

def fin(event):
	master.destroy()

stim.writerow(["SET", "STIMULUS", "TIME"])
master = Tk()
stimulus = StringVar()
s = Label(master, height=40, width=50, text="Please wait for instructions...", wraplength= w_l, font=("Helvetica", f_size), background='green')
w = Label(master, height=40, width=50, textvariable=stimulus, wraplength= w_l, font=("Helvetica", f_size))
finish = Label(master, height=40, width=50, text=conclusion, wraplength= w_l, font=("Helvetica", 80, "bold italic"), background='LightBlue')
s.pack()
w.pack()
finish.pack()
s.bind('<Button-1>', start)
w.bind('<Button-1>', key)
finish.bind('<Button-1>', fin)

response_time = datetime.now()
stim.writerow(['--', 'PALATE_IMAGE', response_time])
start = "Click to begin"
stimulus.set(start)
response_time = datetime.now()
stim.writerow(['--', 'START', response_time])


w.mainloop()

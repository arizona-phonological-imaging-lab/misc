#!/usr/bin/env python
from __future__ import unicode_literals, division
from Tkinter import *
from random import shuffle
from datetime import datetime
import tkFileDialog, tkMessageBox
import os, platform, csv, time, codecs, shutil 

# -*- coding: utf-8 -*-
# with unicode support

#default tkinter stuff
master = Tk()
master.geometry("800x600")
h_val, w_val = 40, 50
master.withdraw()
#set default font size
f_size = 70
#wraplength default
w_l = 1100
#concluding message
conclusion = "This concludes the experiment.  Thank you for your participation."
#variables
carrier_phrase = None
reps = 1

def parameters():
	"""
	Select stimuli set
	"""
	#select source text
	f_name = None
	while not f_name:
		#initialdir="/",
		f_name = tkFileDialog.askopenfilename(title='Stimuli Source Selection', filetypes=[("Text files", ".txt")])
	#solicit a carrier phrase
	carrier = tkMessageBox.askyesno(title="Carrier Phrase", icon="question", message="Use a carrier phrase?")
	if carrier:
		#carrier_phrase = None
		def validate_cp_message():
			global carrier_phrase
			#cp = carrier_disp.get().strip()
			test_carrier = carrier_disp.get()
			if '{0}' not in test_carrier: 
				carrier_disp.delete(0, END)
				carrier_disp.insert(0, "EX: 'I like to eat {0} on Thursdays.'")
			else:
				carrier_phrase = test_carrier
				cp_root.quit()
				cp_root.destroy()

		def confirm_close():
			if tkMessageBox.askokcancel("Quit", "Don't use a carrier phrase?"):
				cp_root.quit()
				cp_root.destroy()


		cp_root = Tk()
		cp_root.protocol("WM_DELETE_WINDOW", confirm_close)
		cp_root.wm_title("Carrier Phrase")
		carrier_disp = Entry(cp_root, width=50, bd=2)
		cp_label = Label(cp_root, text="Carrier Phrase:")
		#cp_label.pack(side = LEFT)
		button = Button(cp_root, text='Submit', command=validate_cp_message)
		#button.pack()
		#carrier_disp.pack()
		cp_label.grid(row=0, column=0)
		carrier_disp.grid(row=0, column=1)
		button.grid(row=0, column=2)
		carrier_disp.insert(0, "EX: 'I like to eat {0} on Thursdays.'")

		carrier_disp.mainloop()
	return f_name

#get unicode-friendly stimuli source
stim_source = parameters()

f = codecs.open(str(stim_source), 'r', 'utf-8')
lines = f.readlines()
unicode_lines = [s.encode('utf-8').rstrip() for s in lines]
csv_file = 'stimulus_response.csv'

#preserve and move csv file if someone forgot to move it...
def on_fail():
	if os.path.exists(csv_file):
		#change csv file name to time last modified
		new_name = str(int(os.path.getctime(csv_file))) + csv_file
		os.rename(csv_file, new_name)
		return new_name

_ = on_fail()
c = open('stimulus_response.csv', 'w')
stim = csv.writer(c)

set_length = len(unicode_lines)

def randomize(stuff, reps=2):
	"""
	create N random repetitions of the stimuli
	"""
	stimuli = []
	shuffle(stuff)
	first = stuff
	stimuli+=first

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

def validate_rep_message():
	global reps
	#cp = carrier_disp.get().strip()
	try: test_reps = int(repetitions.get())
	except: ValueError
	if type(reps) is not int:
		repetitions.delete(0, END)
		repetitions.insert(0, "1")
	else:
		reps = test_reps
		reps_root.quit()
		reps_root.destroy()

def resist_close():
	tkMessageBox.showwarning(title="Don't close me", icon="warning", message="Please specify the number of repetitions")

reps_root = Tk()
reps_root.wm_title("Repetitions")
repetitions = Entry(reps_root, width=10, bd=2)
reps_label = Label(reps_root, text="Repetitions:")
button = Button(reps_root, text='Submit', command=validate_rep_message)
reps_label.grid(row=0, column=0)
repetitions.grid(row=0, column=1)
button.grid(row=0, column=2)
repetitions.insert(0, "1")

reps_root.protocol("WM_DELETE_WINDOW", resist_close)
repetitions.mainloop()


"""
print "File: %s" % stim_source
print "Reps: %i" % reps
if carrier_phrase:
	print "carrier_phrase: %s" % carrier_phrase
print "Lines: ", lines
print "Stuff: ",stuff
raw_input("OK?")
"""

stimuli = randomize(unicode_lines, reps)

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
		if carrier_phrase:
			current = carrier_phrase.format(current)
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
stimulus = StringVar()
s = Label(master, height=h_val, width=w_val, text="Please wait for instructions...", wraplength= w_l, font=("Helvetica", f_size), background='green')
w = Label(master, height=h_val, width=w_val, textvariable=stimulus, wraplength= w_l, font=("Helvetica", f_size))
finish = Label(master, height=h_val, width=w_val, text=conclusion, wraplength= w_l, font=("Helvetica", 80, "bold italic"), background='LightBlue')
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

master.deiconify()
w.mainloop()

#move csv file to subject folder...
c.close()
all_subdirs = [d for d in os.listdir('.') if os.path.isdir(d)]
latest_subdir = max(all_subdirs, key=os.path.getmtime)

src = os.path.abspath('stimulus_response.csv')
dst = os.path.abspath(latest_subdir)
root = Tk()
root.withdraw()
try: shutil.move(src, dst)
except:
	dirSelected = tkFileDialog.askdirectory(title="Select CSV file's Destination Folder",parent=root)
	try: shutil.move(src, dirSelected)
	except: 
		n_name = on_fail()
		warning_message = "Oops!  Something went wrong...\nI had to rename the file to '{0}'\nbefore moving it".format(n_name)
		tkMessageBox.showwarning(title="Duplicate Encountered", icon="warning",message=warning_message)
		try: 
			shutil.move(n_name, dirSelected)
		except:
			error_message = "Something is wrong.  I left the file '{0}' in {1}".format(n_name, os.getcwd())
			tkMessageBox.showerror(title="Duplicate Encountered", icon="error",message=error_message)	
		else: 
			rename_move_success = "File successfully moved!\nFile: '{0}'\n Destination: '{1}'".format(n_name, dirSelected)
			tkMessageBox.showinfo(title="Success", message=rename_move_success)
	else:
		norename_success_message = "File successfully moved!\nFile: '{0}'\nDestination: '{1}'".format(src, dirSelected) 
		tkMessageBox.showinfo(title="Success", message=norename_success_message)
else:
	complete_success_message = "File successfully moved!\nFile: '{0}'\nDestination: '{1}'".format(src, dst) 
	tkMessageBox.showinfo(title="Success", message=complete_success_message)


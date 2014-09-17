#!/usr/bin/env python
from __future__ import unicode_literals, division
from Tkinter import *
from random import shuffle
from datetime import datetime
import tkFileDialog, tkMessageBox
import os, platform, csv, time, codecs, shutil

h_val, w_val = 40, 50
#set default font size
f_size = 70
#wraplength default
w_l = 1100
#concluding message
conclusion = "This concludes the experiment.  Thank you for your participation."
#variables
carrier_phrase = None
reps = 1

class StimulusPresenter(Tk):
    def __init__(self, *args, **kwargs):
        Tk.__init__(self, *args, **kwargs)
        #Tkinter settings
        self.geometry("800x600")
        
        #attributes
        self.stimulus_file = None
        self.carrier_phrase = None
        self.csv_file = 'stimulus_response.csv'
        
        #create buttons
        self.stimulus_selection = Button(self, text="Select stimulus file", command=self.select_stimulus_file)
        self.select_carrier_phrase()
        self.specify_repetitions()
        self.display_stimuli_button = Button(self, text="Present Stimuli", command=self.display_stimuli)
        
        #arrange buttons
        self.stimulus_selection.grid(row=2, column=0)
        self.display_stimuli_button.grid(row=3, column=0)

    def select_stimulus_file(self):
        self.stimulus_file = tkFileDialog.askopenfilename(title='Stimuli Source Selection', filetypes=[("Text files", ".txt")])
        print self.stimulus_file

    def select_carrier_phrase(self):
        Label(self, text="Carrier Phrase: ").grid(row=0)
        self.carrier_disp = Entry(self, width=50, bd=2)
        self.carrier_disp.grid(row=0, column=1)
        self.carrier_disp.insert(0, 'I like to eat {0} on Thursdays.')

    def specify_repetitions(self):
        Label(self, text="Repetitions: ").grid(row=1)
        self.repetitions = Entry(self, width=5, bd=2)
        self.repetitions.grid(row=1, column=1)
        self.repetitions.insert(0, '1')

    def validate_cp(self):
        if self.carrier_disp:
            if "{0}" not in self.carrier_disp:
                #show warning and return
                pass
    
    def read_stimulus_file(self):
        f = codecs.open(str(stim_source), 'r', 'utf-8')
        lines = f.readlines()
        unicode_lines = [s.encode('utf-8').rstrip() for s in lines]

    def display_stimuli(self):
        result = tkMessageBox.askokcancel(title="File already exists", 
                                       message="File already exists. Overwrite?")
        if result is True:
            print "User clicked Ok"
        else:
            print "User clicked Cancel"


if __name__ == "__main__":
    app = StimulusPresenter()
    app.mainloop()
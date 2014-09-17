from __future__ import division
import os, sys, re, unicodedata
reload(sys)
sys.setdefaultencoding("utf-8")
from collections import defaultdict
from datetime import timedelta
import datetime

#The purpose of this code is to create textgrids with boundaries in the first tier for words,
#and create boundaries on the second tier for the sounds in those words that we're interested
#in.  The sounds are identified via a separate document (Words_Sounds5.txt for SG).  The words
#are identified with the stimulus response file, and using the times associated with them.
#Sam Johnston
#Sometime Fall/Spring 2014 
#EDIT 5/6/14:
#Now marks 'neutral tongue' boundaries on either side of each word 

def findBoundaryNum(cell, wordsUsed, textcells_Ordered):
    newSounds = []
    for cell in textcells_Ordered:
        if cell[0] not in wordsUsed:
            continue

        sounds = wordsUsed[cell[0]]
        dur = cell[1][1] - cell[1][0]
        edge = dur * 0.1
        for i in range(len(sounds)):
            if i == 0:
                newSounds.append(sounds[i])

            if i > 0:                           #tries to determine if two sounds are not adjacent in order to make an extra boundary; these conditions critically rely on the labeling to be the orthography
                if sounds[i][-1] == 'h':        #in the weird affrication cases, just ignore them
                    newSounds.append(sounds[i])
                    continue

                if sounds[i][-1] == '2':        #finds cases of vowel sequences in which the intervening C is not being identified
                    if sounds[i-1][-1] == '1':
                        newSounds.append('0')
                        newSounds.append(sounds[i])
                        continue

                    else:
                        newSounds.append(sounds[i])
                        continue

                secondSound = cell[0].find(sounds[i][:-1])
                firstSound = cell[0].find(sounds[i-1][:-1])
                if newSounds[-1][-1] == '2':
                    newSounds.append('0')
                    newSounds.append(sounds[i])

                elif cell[0][firstSound+len(sounds[i-1][:-1]):(firstSound+len(sounds[i-1][:-1]))+len(sounds[i][:-1])] == sounds[i][:-1]:
                    newSounds.append(sounds[i])

                else:
                    newSounds.append('0')
                    newSounds.append(sounds[i])

    return len(newSounds)+(len(wordsUsed)*2)    #adding the length of 'textcells_Ordered', ie. the num of words, allows for 1 neutral boundary set per word


def write_Sound(textgrid, cell, x, wordsUsed, last_boundary):
    sounds = wordsUsed[cell[0]]
    dur = cell[1][1] - cell[1][0]
    edge = dur * 0.1
    newSounds = []
    for i in range(-1,len(sounds)):
        if i == -1:
            newSounds.append('neutral')
            newSounds.append('0')
        
        if i == 0:
            newSounds.append(sounds[i])

        if i > 0:                           #tries to determine if two sounds are not adjacent in order to make an extra boundary; these conditions critically rely on the labeling to be the orthography
            if sounds[i][-1] == 'h':        #in the weird affrication cases, just ignore them
                newSounds.append(sounds[i])
                continue

            if sounds[i][-1] == '2':        #finds cases of vowel sequences in which the intervening C is not being identified
                if sounds[i-1][-1] == '1':
                    newSounds.append('0')
                    newSounds.append(sounds[i])
                    continue

                else:
                    newSounds.append(sounds[i])
                    continue

            secondSound = cell[0].find(sounds[i][:-1])
            firstSound = cell[0].find(sounds[i-1][:-1])
            if newSounds[-1][-1] == '2':
                    newSounds.append('0')
                    newSounds.append(sounds[i])

            elif cell[0][firstSound+len(sounds[i-1][:-1]):(firstSound+len(sounds[i-1][:-1]))+len(sounds[i][:-1])] == sounds[i][:-1]:
                newSounds.append(sounds[i])

            else:
                newSounds.append('0')
                newSounds.append(sounds[i])

    indiv_Dur = (dur - (edge*2)) / len(newSounds)
    accum = cell[1][0] + edge
    textgrid.write('\t\tintervals [%s]:\n' %str(x))
    textgrid.write('\t\t\txmin = %s\n\t\t\txmax = %s\n\t\t\ttext = "%s"\n' %(last_boundary, accum, ''))
    x+=1
    for i in range(len(newSounds)):
        if newSounds[i] == '0':
            textgrid.write('\t\tintervals [%s]:\n' %str(x))
            textgrid.write('\t\t\txmin = %s\n\t\t\txmax = %s\n\t\t\ttext = "%s"\n' %(accum, accum+indiv_Dur, ''))
            accum += indiv_Dur
            last_boundary = accum
            x+=1
            continue

        if newSounds[i][:-1] in ['d','t','c','g','p','b']:
            if newSounds[i][-1] == 'h':
                key = 'm {0}'.format(newSounds[i])

            else:
                key = 'p {0}'.format(newSounds[i])
        elif newSounds[i] == 'neutral':
            key = 'neutral'
        else:
            key = 'm {0}'.format(newSounds[i])

        textgrid.write('\t\tintervals [%s]:\n' %str(x))
        textgrid.write('\t\t\txmin = %s\n\t\t\txmax = %s\n\t\t\ttext = "%s"\n' %(accum, accum+indiv_Dur, key))
        accum += indiv_Dur
        last_boundary = accum
        x+=1

    return textgrid, last_boundary, x


def write_Cell(textgrid, cell, x, wordsUsed):
    textgrid.write('\t\tintervals [%s]:\n' %x)
    if cell[0] in wordsUsed:
        textgrid.write('\t\t\txmin = %s\n\t\t\txmax = %s\n\t\t\ttext = "%s"\n' %(str(cell[1][0]), str(cell[1][1]), cell[0]))

    else:       #if you don't want to include a word in the text grid that isn't in the above list, make the 3rd argument below '', instead of cell[0]
        textgrid.write('\t\t\txmin = %s\n\t\t\txmax = %s\n\t\t\ttext = "%s"\n' %(str(cell[1][0]), str(cell[1][1]), cell[0]))

    return textgrid


def make_Textgrid(textcells, textgrid, duration, wordsUsed, tier2size):
    textgrid.write('File type = "ooTextFile"\nObject class = "TextGrid"\n\n')
    textgrid.write('xmin = 0\nxmax = %s\n' %(duration))
    tiers = '2'
    size = str(len(textcells)+2)
    textgrid.write('tiers? <exists>\nsize = %s\nitem []:\n' %tiers)
    textgrid.write('\titem [1]:\n\t\tclass = "IntervalTier"\n\t\tname = "Words"\n\t\txmin = 0\n\t\txmax = %s\n\t\tintervals: size = %s\n' %(duration,size))
    x = 1
    y = 0
    textcells_Ordered = sorted(textcells.items(), key = lambda x: x[1])    #orders the dict of stims based on the time they appeared
    for cell in textcells_Ordered:      #'cell' is the stimulus, or the WORD
        if x == 1:
            end_Cell = ('', (0,cell[1][0]))
            textgrid = write_Cell(textgrid, end_Cell, str(x), wordsUsed)
            x+=1

        textgrid = write_Cell(textgrid, cell, str(x), wordsUsed)
        x+=1
        if x == len(textcells_Ordered)+2:
            end_Cell = ('', (cell[1][1],duration))
            textgrid = write_Cell(textgrid, end_Cell, str(x), wordsUsed)
            x+=1

    tier2size2 = findBoundaryNum(cell, wordsUsed, textcells_Ordered)
    textgrid.write('\titem [2]:\n\t\tclass = "IntervalTier"\n\t\tname = "Segments"\n\t\txmin = 0\n\t\txmax = %s\n\t\tintervals: size = %s\n' %(duration, str(tier2size2 + len(wordsUsed)+1)))
    last_boundary = 0
    x = 1
    for cell in textcells_Ordered:
        if cell[0] not in wordsUsed:
            continue

        textgrid, last_boundary, x = write_Sound(textgrid, cell, x, wordsUsed, last_boundary)
        if x == (len(wordsUsed)+tier2size2+1):
            end_Cell = ('', (last_boundary,duration))
            textgrid = write_Cell(textgrid, end_Cell, str(x), wordsUsed)
            x+=1

    textgrid.close()


def realtime(time):
    minutes = time[2:4]
    colon = time.rfind(':')
    secs = time[colon+1:]
    add_Secs = float(minutes)*60
    realtime = add_Secs+float(secs)
    return realtime


def pair_Times(stim_List, stim_Times):
    textcells = defaultdict(tuple)
    x = 0
    for stim in stim_List:
        # print [stim]
        if 'achlais' in stim:
            stim = 'achlais'

        start = str(stim_Times[x])
        try:
            end = str(stim_Times[x+1])

        except IndexError:
            break

        start_Time = realtime(start)
        end_Time = realtime(end)
        textcells[stim] = (start_Time, end_Time)
        x+=1

    return textcells


def make_Times(vidTimes, stimulus_response):
    vid = open(vidTimes, 'r').readlines()
    x = 4
    stim_List = []
    stim_Times = []
    init_Time = timedelta(hours = int(vid[1][34:36]), minutes = int(vid[1][36:38]), seconds = int(vid[1][38:40]), milliseconds = int(vid[1][40:]))
    final_Time = timedelta(hours = int(vid[3][34:36]), minutes = int(vid[3][36:38]), seconds = int(vid[3][38:40]), milliseconds = int(vid[3][40:]))
    difference = final_Time - init_Time
    duration = realtime(str(difference))
    while x < len(stimulus_response):
        stimline = stimulus_response[x].split(',')
        if stimline[0] == '\r\n':
            x+=1
            continue

        stimulus = stimline[1]
        if stimulus == 'START' or stimulus == 'PALATE_IMAGE':
            x+=1
            continue

        if stimulus == '':
            x+=1
            continue

        stimtime = timedelta(hours = int(stimline[2][11:13]), minutes = int(stimline[2][14:16]), seconds = int(stimline[2][17:19]), milliseconds = int(stimline[2][20:23]))
        stim = "".join(c for c in unicodedata.normalize('NFD', (stimulus).decode('utf-8')) if not unicodedata.combining(c))     #remove any accents
        stim = stim.replace(' ','-')
        if 'achlais' in stim:       #because there is something messed up with achlais in the stimulus_response.csv files
            stim = 'achlais'

        stim_List.append(stim)
        stim_Times.append(stimtime-init_Time)
        x+=1
    textcells = pair_Times(stim_List,stim_Times)    #textcells = a dictionary with the WORD/stimulus as the key, and the start and end time of the word as the vals
    return textcells, duration


def getwords(wordList):
    wordsUsed = defaultdict(list)
    tier2size = 0
    for line in wordList:
        tier2size += 1
        line = line.split('\t')
        wordsUsed[line[0]].append(unicode(line[2].rstrip()))
    return wordsUsed, tier2size


def main():
    #open stim_response and output files
    path = os.getcwd()
    wordList = open('Words_Sounds.txt', 'rU').readlines()
    wordsUsed, tier2size = getwords(wordList)
    all_Files = os.listdir(path)
    for dirs in all_Files:
        if os.path.isfile(os.path.join(path, dirs)) or dirs == 'Old_xls':
            continue
        print 'Subject: ', dirs
        try:
            vidTimes = path+'/'+dirs+'/'+'vidTimes.txt'
            stim_Response = open(path +'/'+dirs+'/'+'stimulus_response.csv', 'r').readlines()
        except:
            print 'No Data Found'
            continue
        #make a dict of the stimulus and their real times
        textcells, duration = make_Times(vidTimes, stim_Response)
        ####use to put in each individual folder; comment out and uncomment below to put all in main folder
        # textgrid = open(path+'/'+dirs+'/'+dirs+'.TextGrid', 'w')
        ####use to put all in main folder, group to make a .zip file to send; comment out and uncomment above to put in individual folders
        textgrid = open(path+'/'+dirs+'.TextGrid', 'w')

        make_Textgrid(textcells, textgrid, duration, wordsUsed, tier2size)

        
main()

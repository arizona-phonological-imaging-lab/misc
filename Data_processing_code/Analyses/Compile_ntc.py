from __future__ import division
import os, shutil
from collections import defaultdict
from os.path import join

#This script compiles the 'NEW'-ly transformed trace files, located in their respective subfolder in the 'Analyses' directory, 
#into the format expected by the R-code that creates the SSANOVA plots.

#The R-code creating the graphs, and thus this code, assumes 3 repetitions of a sound/word/stimulus, an 'a', 'b' and 'c'.  Do
#not run this code without all repetitions, as I cannot guarantee what madness will ensue afterwards.  The structure and
#format of the files and directories expected by this code is established by using the "Get_specific_frames.py" script and the
#"moveTraces.py" script.  Again, while these other scripts allow the movement of individual repetitions of a subject, you
#should not do that here.

#Sam Johnston
#05-12-14



def compile_NTC_files(subjreps):
    path = os.getcwd()

    subjs = set()
    for subjrep in subjreps:    #establishes the set of subject numbers
        subjs.add(subjrep[:-1])

    organizedRepetitions = defaultdict(list)
    dirs = [i for i in os.listdir(path) if os.path.isdir(os.path.join(path,i)) == True]
    for d in dirs:
        try:        #only finds dirs with num as first char
            float(d[0])
        except ValueError:
            continue
        organizedRepetitions[d].append(d)

    iterTrans = {'a':'1','b':'2','c':'3'} #maps the rep-letter to a token num, to be used under 'token' column in final file
    repetitions = ['a','b','c']
    for s in subjs:
        sub_dir = join(join(path, 'SingleFrames'), s)
        masterFile = open(join(sub_dir,'{0}_NTC_compiled.txt'.format(s)), 'w')
        masterFile.write('word\ttoken\tX\tY\n')

        for rep in repetitions: 
            rep_dir = join(sub_dir,rep)
            for word in [i for i in os.listdir(rep_dir) if os.path.isdir(join(rep_dir,i))]:   #find the directory for each individual word's traces
                word_dir = os.path.join(rep_dir,word)

                traces = [i for i in os.listdir(word_dir) if 'NEW' in i]        #get a list of the 'NEW' traces output by Julia's Matlab code
                if traces == []:        #if no traces were move, or no 'NEW' traces were made for the word, print an error, and continue

                    print "ERROR: There are no traces for {0} in {1}{2}; likely not enough datapoints in trace file for MATLAB script to operate".format(word, s, rep)
                    continue

                for trace in traces:    #iterate through existing traces, compiling the files, skipping the transformed C2 (neutral tongue) file
                    if 'C2' in trace:
                        continue

                    underscores = [i for i,j in enumerate(trace) if j == '_']  #obtain the indexes for the underscores within the name
                    word = trace[underscores[2]+1:underscores[3]]       #identify the word and sound from the trace-file name
                    sound = trace[underscores[4]+1:underscores[5]]

                    #open the file, read the lines, and add the relevant info (coordinates) to the "masterFile"
                    traceFile = open(join(word_dir, trace), 'r').readlines()  
                    for line in traceFile:
                        line = line.split('\t')
                        if line[0] == '':
                            continue
                        masterFile.write('{0}_{1}\t{2}\t{3}\t{4}'.format(word,sound,iterTrans[rep],line[1],line[2]))
        masterFile.close()


##############################################################################
def get_Subjs():    #user interface to identify subjects in question
    #Called by main()
    #Asks the user to identify the subjects. For format, see below.  If running multiple iterations on the same subject(s),
    #this can be hard-coded easily to bypass the user interface.  See ######-surrounded regions below for different
    # options.  When done entering subjects, must type "done".  A check will come up, asking the user to review the 
    #current list of subjects.  Here, they can be removed (no longer added); must type "y" to indicate the list is OK.
    ###################################
    # subjects = set(['05a']) #use code if you need/want to preset a given subj(s) several times in a row, without having to reenter info
    # return subjects
    ###################################
    subjects = set()
    print "Identify target subject(s) (e.g '7' will return 7[a,b,c]; 'all' will return 1-26[abc]). Type 'done' when done. "
    while True:
        subject = raw_input("Enter subject number(s): ")
        ###############     comment above and use below to preset input if it is desired to run all subjects
        # subject = 'all'
        ###############
        if subject == 'done':   #break when 'done' is typed when done entering subjects
            break
        if subject == 'all':    #if 'all' entered, add all subjects (in range 26, for the 26 Gaelic subjects
            for i in range(1,27):
                for j in ['a','b','c']:     #include the repetition for each subject
                    if len(str(i)) == 1:
                        subjects.add(('0'+str(i)+j))        #if single digit, add a zero to the beginning
                    else:
                        subjects.add((str(i)+j))
            subjects.remove('04b')       #these are added to avoid error in SCOTTISH GAELIC - these iterations do not exist
            subjects.remove('21b')
            subjects.remove('22c')          #if using code for a different study, this may want to be commented
            break
        if subject[-1] != 'a' and subject[-1] != 'b' and subject[-1] != 'c': #if only a number is entered, add 'a' 'b' and 'c' iterations
            if len(subject) == 1:
                subject = ('0'+str(subject))
            subjects.add((subject+'a'))
            subjects.add((subject+'b'))
            subjects.add((subject+'c'))
            continue
        if len(subject[:-1]) == 1:
            subjects.add(('0'+subject))
        else:
            subjects.add(subject)
    print 'Double check list. Type "y" to continue, or the name of element to remove.'  #due to my inability to type correctly, extra check
    while True:                                                                             #is in place
        print subjects
        element = raw_input('"y" or element to remove: ')
        ############### comment above and uncomment below if you want to preset an input without prompt
        # element = 'y'
        ###############
        if element == 'y':
            print 'Final subjects: ', subjects
            return subjects
        else:
            try:
                subjects.remove(element)
            except:
                print "You must enter valid input."

def main():
    subjreps = get_Subjs()
    compile_NTC_files(subjreps)

main()

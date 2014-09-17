from __future__ import division
import os, shutil, xlrd
from collections import defaultdict
from os.path import join

# This moves (copies) the traces (for SG) from the location where they are placed when completed
# and moves the to the Analyses folder in the SG directory.  They are then placed in dirs
# based on the study numbers they are associated with as well as the subject and iteration they
# belong to
#Sam Johnston; sometime spring 2014
#Edit
#using the neutral tongue contours as the 'CX' (C1 or C2) files (for Julia's matlab code), this module will move
#these as well, negating the need for the "place_CX_files.py" script, which moved the 'CX'
#files when we were only using 1 file per iteration of the subject

def moveNormal(subjsreps):
	path = os.getcwd()
	print "Current head directory: {0}\n".format(path)

	subjs = []
	reps = set()
	for subjrep in subjsreps:		#find the subject/repetition and place in separate structures
		subjs.append(subjrep[:2])
		reps.add(subjrep[-1])

	neutral_trace_paths = defaultdict(lambda: defaultdict(dict))		#holds the file names of the neutral traces - to be moved in 'moveNeutrals'
	bad_neutrals = defaultdict(set)									#holds the pre-identified 'bad' neutral images - so they are not used.
	word_lists = {}										#holds an ordered word list for each subj/rep pair

	dirs = os.listdir(path)
	Analyses = join(path, 'Analyses')		#gets the path for the analyses folder
	newLocale = join(Analyses, 'SingleFrames')		#path for the destination folder - should be changed if using range of frames

	for subj in subjs:
		subjLocale = join(newLocale, subj)		#gets the path for the main subject folder
		if (os.path.exists(subjLocale)) == False:	#if the dir doesn't exist, make it
			os.makedirs(subjLocale)
		
		for rep in reps:	
			repLocale = join(subjLocale, rep)	#gets the path for the rep within the subj folder
			if (os.path.exists(repLocale)) == False:	#if the dir doesn't exist, make it
				os.makedirs(repLocale)
			
			folder = [i for i in dirs if subj+rep in i]  #gets folder name within the main SG folder

			newpath = join(path, folder[0])		#gets the path for the folder

			traceFolder = join(newpath, 'Completed_traces') #gets the folder with the completed traces within the subj/rep folder

			traces = os.listdir(traceFolder)		#gets a list of the files in that folder

			#if wanting to do range of frames, will need to change 'SingleFrames' destination folder above, and change below 'T' to 'R'
			targetTraces = [i for i in traces if 'T' in i and 'traced' in i]	#gets only target trace files out of the list of previously identified files

			word_list = []
			word_set = set()
###################################  getting the bad neutral traces (the word after the neutral image)
			
			excel = xlrd.open_workbook(join(path, "Inadequate_neutrals.xls"))		#check the excel folder where 'bad' neutral images are identified
			for subjrep in subjsreps:
				
				try:
					sheet = excel.sheet_by_name((subjrep))		#obtain the sheet object from the excel workbook object
				except:
					sheet = excel.sheet_by_name((subjrep[1:]))  #if the sheet name is a single digit, and a '0' is not inserted before the single number

				i = 0
				
				try:
					while True:
						i+=1
						#add word specifying the location of the bad neutral image - must extract string by turning cell obj to str, and extracting
						bad_neutrals[(subjrep)].add(str(sheet.cell(i,0))[str(sheet.cell(i,0)).find("'")+1:str(sheet.cell(i,0)).rfind("'")])

				except IndexError:
					continue
####################################
			for trace in targetTraces:		#iterates through the traces to get list of words:frame number tuples
				
				underscores = [i for i,j in enumerate(trace) if j == '_']					#finds the underscores (dividers) within the file name
				
				word = trace[underscores[1]+1:underscores[2]]		#extracts the word from the file names
				if '+' in word or word in word_set:			#only neutral images (occurring in between words) will have a '+' - don't add these
					continue
				word_set.add(word)  #add word to set (set, because there are multiple sounds in each word, multiple traces, only want 1 instance of each word)
				frame_num = int(trace[trace.rfind('-')+1:trace.find('.')])	#get the frame number for the word
				word_list.append((word,frame_num))		#append the word (mult. instances doesn't matter here) with the frame# to a list

			word_list = sorted(word_list, key=lambda x: x[1])		#order the list based on the frame numbers
			new_word_list = []
			for word in word_list:
				new_word_list.append(word[0])			#create a new list (in the same order as the frame# list), but just contains the words
			word_lists[subj+rep] = new_word_list		#add this new list to the wordlist dictionary
####################################
		##############################################################################################

			for trace in targetTraces:		#iterates through the traces a second time to move the trace files, store neutral trace file names

				underscores = [i for i,j in enumerate(trace) if j == '_']

				word = trace[underscores[1]+1:underscores[2]]
				if '+' in word:			#only neutral traces will have a '+'
					between = word.split('+')	#separate the two words that the trace occurs in between
					neutral_trace_paths[subj][rep]['s_{0}'.format(between[0])] = trace 		#add the first word to a 's_' key, referencing this trace
					neutral_trace_paths[subj][rep]['e_{0}'.format(between[1])] = trace 		#add second word to an 'e_' key, referencing the same trace

				else:		#if not neutral trace, go ahead and move the trace file to a dir with the name of the word the sound is in
					wordLocale = join(repLocale, word)
					if (os.path.exists(wordLocale)) == False:	#if the dir doesn't exist, make it
						os.makedirs(wordLocale)
					traceLocale = join(traceFolder, trace)		#get path of trace in the trace folder
					newTraceLocale = join(wordLocale, trace)	#make path of the trace where it would be in new folder
					shutil.copy2(traceLocale, newTraceLocale)			#copy/paste the trace

			#################################################################################################

		# add the C1 trace to all word-level directories within the subject's main folder
		# for rep in os.listdir(subjLocale):		#fore each repetition [abc]
		# 	repLocale = join(subjLocale,repLocale)
		# 	for word in os.listdir(repLocale):		#for each stimulus/word we're using within the repetition
		# 		# print 'inside', wordLocale, C1
		# 		wordLocale = join(repLocale,word)	#get location for the word directory
		# 		shutil.copy2(C1Locale, join(wordLocale, C1))	#copy/paste the C1 (primary) neutral trace file from its location, to all word folders
	
	return word_lists, word_set, bad_neutrals, neutral_trace_paths, newLocale, subjs, reps, subjsreps
##############################################################################



def findLast(neutral_trace_paths, subj,rep,word_folder,traceFolder,word_path):  #gets the frames labeled with an 's_'
	C2 = neutral_trace_paths[subj][rep]['e_{0}'.format(word_folder)]		#extract the name of the neutral frame
	shutil.copy2(join(traceFolder,C2), join(word_path, 'C2_{0}'.format(C2)))	#move the neutral trace to new location, placing C2 in front of the filename
	return True


def findFirst(neutral_trace_paths, subj,rep,word_folder,traceFolder,word_path):  #gets the frames labeled with an 'e_'
	C2 = neutral_trace_paths[subj][rep]['s_{0}'.format(word_folder)]		#extract the name of the neutral frame
	shutil.copy2(join(traceFolder,C2), join(word_path, 'C2_{0}'.format(C2))) #move the neutral trace to new location, placing C2 in front of the filename
	return True

def findAdjacent(neutral_trace_paths, subj,rep,word1,word2,traceFolder,word_path,word_list,bad_neutrals):
	idx1 = word_list.index(word1)-1 	#obtains the index for the previous word (1) & the next word (2)
	idx2 = word_list.index(word2)+1
	try:									#these try: except: statments prevent overreach past beginning and end of recording
		prev_word = word_list[idx1]
	except IndexError:
		prev_word = word_list[0]
	
	try:
		next_word = word_list[idx2]
	except:
		next_word = word_list[len(word_list)-1]
	
	match = False	#used as a check incase a trace is deemed "bad"; the match is returned False

	if prev_word in bad_neutrals[subj+rep]:	#check for goodness of trace
		if next_word in bad_neutrals[subj+rep]: #if prev_word is bad, check for goodness of other trace
			return False, prev_word, next_word	#return no match if all traces bad

		else:	#if the next_word is a good trace
			try:
				match = findLast(neutral_trace_paths, subj,rep,next_word,traceFolder,word_path) #see if trace exists - if so, return it
				return match, prev_word, next_word
			except KeyError as e:
				# print 'word {0} not found; continuing on to next level'.format(e) #if not found, return no match
				return False, prev_word, next_word

	else:	#same structure, function calls as above, checking goodness, checking if exists, if both conditions satisified, return a valid trace (a match)
		try:
			match = findLast(neutral_trace_paths,subj,rep,next_word,traceFolder,word_path)
			return match, prev_word, next_word
		except KeyError as e:
			# print 'word {0} not found; trying next_word word'.format(e)
			if next_word in bad_neutrals[subj+rep]:
				
				return False, prev_word, next_word
			else:
				try:
					
					match = findFirst(neutral_trace_paths,subj,rep,prev_word,traceFolder,word_path)
					return match, prev_word, next_word
				except KeyError as e:
					# print 'word {0} not found; continuing on to next level'.format(e)
					
					return False, prev_word, next_word
				
	return match, prev_word, next_word


def checkC1(traceFolder, word_list, subj, rep, C1, neutral_trace_paths, bad_neutrals):
	i = 0
	available_traces = {}	#holds k/v pairs: trace, percent complete.  If the 90% benchmark isn't found, use best available image
	while True:		#continue until an acceptable image is found
		text = open(join(traceFolder,C1), 'r').readlines()		#open the trace text file
		#the below code in 'for-loop' is to determine how complete a trace is; the fewer data points the worse the trace, and consequently the image
		completeness = [0,0]		#keeps track of [empty-data-points,filled-data-points] within the trace file
		for line in text:
			line = line.split()
			if line[0] == '-1':		#if datapoint is empty, increase the empty data point count
				completeness[0] += 1
			else:					#else, increase full data point count
				completeness[1] += 1
		percentage = (completeness[1] / (completeness[0] + completeness[1])) * 100	#create a percentage-of-completeness (a 'quality-of-trace' value)
		if percentage > 90:		#if the percentage filled is greater than 90%, go ahead and use the trace
			C1 = join(traceFolder,C1)
			return C1
		else:					#otherwise, get the path to the [chronologically] following [neutral] trace
			available_traces[C1] = percentage 	#record the completeness of the trace with
			try:
				word = word_list[i]
				if word_list[i+1] not in bad_neutrals[(subj+rep)]:	#if it is a bad trace image (e.g., not steady), move on to the next image
					try:
						C1 = neutral_trace_paths[subj][rep]['s_{0}'.format(word)]	#assign a new value to C1
					except KeyError:
						pass
				
				i += 1 	#increase the index and continue to next iteration if this trace does not satisfy the 90% requirement

			except IndexError:	#instead of breaking, alert the user, and just use the last trace grabbed
				ordered_available_traces = sorted(available_traces.items(), key=lambda x: [x])
				ordered_available_traces.reverse()
				C1 = ordered_available_traces[0][0]
				
				print "ERROR: There were no neutral traces with 90 percent completion found; the the best available trace has been used at {1} percent completion: {0}\n".format(C1, percentage)
				return join(traceFolder,C1)



def moveNeutrals(word_lists, word_set, bad_neutrals, neutral_trace_paths, newLocale, subjs, reps, subjsreps):
	path = os.getcwd()
	dirs = os.listdir(path)
	
	C1s = {}	#dict to hold the master neutral trace for each subject
	for subjrep in subjsreps: #recall, subjsreps contains a subject-repetition pair
		subj = subjrep[:2]	#breaks the subject from the subj-rep pair
		rep = subjrep[-1]
		word_list = word_lists[subjrep]  #extract the wordlist from the word_lists dictionary
		
		folder = [i for i in dirs if subjrep in i]  #gets folder name within the main SG folder that matches the subjrep

		newpath = join(path, folder[0])		#gets the path for the folder

		traceFolder = join(newpath, 'Completed_traces') #gets the path to folder with the completed traces within the subj/rep folder

		try:
			C1 = C1s[subj]		#see if a neutral frame has already been assigned for this subject
		except KeyError:
			#below, 's_start' refers to the first neutral trace identified in the recording, prior to the elicitation of the first stimuli
			C1 = neutral_trace_paths[subj][rep]['s_start']	#if not, identifies the primary neutral trace (C1) which will be used to transform all traces
			#opens the file, determines how many points are identified - if not at least 95% of the points are identified, continue on to the next trace
			C1 = checkC1(traceFolder, word_list, subj, rep, C1, neutral_trace_paths, bad_neutrals)
			C1s[subj] = C1 #add the new C1 trace file to the subject, so other reps of this subj will use this same trace
		
		#line below : probably a way to do this without getting 'DS.Store' files in listdir - if so, can simplify
		for word_folder in [i for i in os.listdir(join(join(newLocale,subj),rep)) if '.' not in i]:		#don't get any 'DS.Store' directories
			# if word_folder == 'earball':
			# 	print 'EARBALL'
			word_path = join(join(join(newLocale, subj),rep), word_folder)  	#obtain the path of the identified folder
			shutil.copy2(C1, join(word_path, 'C1_{0}'.format(C1[C1.rfind('/')+1:])))	#automatically move the C1 master trace to every word's folder
			prev_word = word_folder		#assign the previous word (this word) and next word to values
			try:
				next_word = word_list[word_list.index(word_folder)+1]
			except IndexError:	#if there is not another word after the current in the recording, keep "next_word" at the same word, without advancing
				next_word = prev_word
				pass
			
			while True:
				#lots of 'IF' conditions below, to make sure that a) trace is not a bad trace, and b), the trace exists
				#if one of the two conditions are violated, an adjacent neutral trace must be found to replace the bad/missing trace
				#similar to the code in the 'findAdjacent' function; enough necessary differences that it was kept separate, however.
				if prev_word in bad_neutrals[subjrep]:  
					
					if next_word in bad_neutrals[subjrep]:
						i = 0
						while i < 2:	#extend the search for a replacement trace to only two outside "layers" of image traces, when both traces are bad
							# print 'EARBALL BAD BAD', prev_word, next_word
							match,prev_word,next_word=findAdjacent(neutral_trace_paths, subj,rep,prev_word,next_word,traceFolder,word_path,word_list,bad_neutrals)
							# print 'BAD BAD', prev_word, next_word
							if match == True:	#if a good image is found, break out of the loop
								break
							if match == False:
								i+=1
						if match == False: 	#if no good trace found within the 2-layer window, report an error
							print 'ERROR: No successful neutral trace found for {0} in {1}; continuing without placing trace for {0}.\n'.format(word_folder, subjrep)
						break
					else:
						# if the next word's image isn't a bad image, as identified in the "inadequate_neutrals.xls" file, assign
						try:
							# print 'EARBALL BAD GOOD', next_word
							match = findFirst(neutral_trace_paths, subj,rep,word_folder,traceFolder,word_path)
							break
						except KeyError as e:
							# print 'end word {0} not found; trying start word'.format(e)
							i = 0
							while i < 2:
								match, prev_word, next_word= findAdjacent(neutral_trace_paths, subj,rep,prev_word,next_word,traceFolder,word_path,word_list,bad_neutrals)
								# print 'BAD GOOD', prev_word, next_word
								if match == True:
									break
								if match == False:
									i+=1
							if match == False:
								print 'ERROR: No successful neutral trace found for {0} in {1}; continuing without placing trace for {0}.\n'.format(word_folder, subjrep)
							break
				else: #if the first image (prev_word) isn't a bad image, see if it exists
					try:
						# print 'EARBALL GOOD ?', next_word
						match = findLast(neutral_trace_paths, subj,rep,word_folder,traceFolder,word_path)
						break
						
					except KeyError as e:
						# print 'end word {0} not found; trying start word'.format(e)
						if next_word in bad_neutrals[subjrep]:
							i = 0
							while i < 2:
								match,prev_word,next_word=findAdjacent(neutral_trace_paths, subj,rep,prev_word,next_word,traceFolder,word_path,word_list,bad_neutrals)
								# print 'GOOD BAD', prev_word, next_word
								if match == True:
									break
								if match == False:
									i+=1
							if match == False:
								print 'ERROR: No successful neutral trace found for {0} in {1}; continuing without placing trace for {0}.\n'.format(word_folder, subjrep)
							break
						else:
							try:
								match = findFirst(neutral_trace_paths, subj,rep,word_folder,traceFolder,word_path)
								break
								
							except KeyError as e:
								# print 'end word {0} not found; trying start word'.format(e)
								i = 0
								while i < 2:
									match, prev_word, next_word = findAdjacent(neutral_trace_paths, subj,rep,prev_word,next_word,traceFolder,word_path,word_list,bad_neutrals)
									# print 'LAST OPTION', prev_word, next_word
									if match == True:
										break
									if match == False:
										i+=1
								if match == False:
									print 'ERROR: No successful neutral trace found for {0} in {1}; continuing without placing trace for {0}.\n'.format(word_folder, subjrep)
								break


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
                for j in ['a','b','c']:		#include the repetition for each subject
                    if len(str(i)) == 1:
                        subjects.add(('0'+str(i)+j))		#if single digit, add a zero to the beginning
                    else:
                        subjects.add((str(i)+j))
            subjects.remove('04b')       #these are added to avoid error in SCOTTISH GAELIC - these iterations do not exist
            subjects.remove('21b')
            subjects.remove('22c')			#if using code for a different study, this may want to be commented or removed
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
    print '\nDouble check list below.\nType "y" to continue, or the name of element to remove.'  #due to my inability to type correctly, extra check
    while True:                                                                             #is in place
        print subjects
        element = raw_input('"y" or element to remove: ')
        ############### comment above and uncomment below if you want to preset an input without prompt
        # element = 'y'
        ###############
        if element == 'y':
            print 'Final subject set: {0}\n'.format(subjects)
            return subjects
        else:
            try:
                subjects.remove(element)
            except:
                print "You must enter valid input."

def main():
	subjsreps = get_Subjs() #returns a set of 'subject-repetition' pairs in the form "1a", "10c", etc
	word_lists, word_set, bad_neutrals, neutral_trace_paths, newLocale, subjs, reps, subjsreps = moveNormal(subjsreps) #moves normal trace files to established directory structure
	moveNeutrals(word_lists, word_set, bad_neutrals, neutral_trace_paths, newLocale, subjs, reps, subjsreps) #moves the neutral trace files to their respective directories

main()



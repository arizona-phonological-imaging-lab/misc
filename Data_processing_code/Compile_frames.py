import os, re, codecs, sys, shutil, math, xlrd, xlwt, xlutils, unicodedata, sys
reload(sys)
sys.setdefaultencoding("utf-8")
from collections import defaultdict
from xlutils.copy import copy
from xlwt import easyxf


###############################################################################################################
### READING THE FRAMELOG (PRAAT SCRIPT OUTPUT) FILE AND EXTRACTING RELEVANT INFORMATION
###############################################################################################################

def getFrameNumbers(frameLogs, words_Sounds, cwd):
    #Called by main(); calls find_sounds()
    #iterates through a framelog file, filling out the 'all_Logs' dictionary with the required information
    #Uses the stimuli list from 'Words_Sounds.txt' to match the words in the framelog file; must match prior
    #to being entered into the dictionary.  Once the framelog file has been read and all_logs filled out,
    #a check is used to see if there are any stimuli in words_Sounds that weren't grabbed by all_Logs. These
    #are then categorized as missing/mislabeled stimuli, and this list is sent to fixinputs()
    
    all_Logs = {}  #primary dictionary in this script; will hold all the frame numbers for each sound, for each word, for each repetition, subject, & reviewer

    noMatches = set()   #will hold all words in which a label was not found within a textgrid
    for filename in frameLogs:
        #separates the subject from the reviewer
        underscore = filename.rfind('_')
        subject_IDer = filename[:underscore]
        underscore = subject_IDer.rfind('_')
        subject = subject_IDer[:underscore]
        IDer = subject_IDer[underscore+1:]
        if '_' in subject:
            subject = subject.replace("_","")
        log = subject+'_'+IDer
        all_Logs[log] = defaultdict(dict)
        #make sure that Praat saves .Textgrid as utf-8 (it defaults to utf-16)
        #if \s between each character in output - file is utf-16 format
        frameLog = open((cwd+'/'+filename), 'r').readlines()
        i = 0
        for line in frameLog:
            all_Logs, i = find_Sounds(line,all_Logs,words_Sounds,log,i) #function to extract data from framelog

        checkSet = set()    #sounds to be checked
        new_Words_Sounds = set()
        for word_sound in words_Sounds:     #this and below is to make sure reviewers didn't mislabel or forget to label a segement
            for entry in all_Logs[log]: 
                if (word_sound[0] in entry and word_sound[1] in entry) or (word_sound[0] in entry and 'X'+word_sound[1][-1] in entry):
                    checkSet.add(word_sound)#compile set of sounds that have been grabbed

        for word_sound in words_Sounds:
            if word_sound not in checkSet:
                noMatches.add((log, word_sound))    #incase a sound is in a study, and the reviewer misspells it to the point it
    if len(noMatches) == len(words_Sounds):              #cant be matched (if not matched) add it (later) to the Ierrors Set to be fixed
        print '!!ERROR!! NO MATCHES WERE FOUND - ENSURE THAT BOTH TEXTGRIDS ARE !UTF-8! ENCODING'

    return all_Logs, noMatches

def find_Sounds(line, all_Logs,words_Sounds, log,i):
    #Called by getFrameNumbers()
    #Actually extracting data from framelog.  Tries to match it with an entry in words_sounds.  If it fails to 
    #match, it simply returns to getFrameNumbers().  This enables users to search for a subset of sounds based
    #on the study, e.g. if looking only for study 11 sounds, a study 2 sound will not raise any error.  The 
    #IndexError below indicates that the textgrid may have been rewritten by the code in a way that makes the 
    #textgrid syntax illegal, ie. praat will not open it.

    if i==0:
        i +=1
        return all_Logs, i
    data = line.split('\t')
    
    #just for neutral frames##
    if 'neutral' in line:
        study = '14'    #I've arbitrarily decided that neutral tongue images will be "study 14" for coding purposes - to keep all 'study' variables as adjacent integers
        soundInfo = (data[2],data[4],data[5])
        # print soundInfo, data[9]
        if data[9] != data[10] or data[9] != data[8]:         #neutral frames need to only have one frame, if the start doesn't equal the end, crash and report location of error
            print "ERROR!! NEUTRAL BOUNDARIES AT {0} FOR ID'ER {1} CONTAINS MORE THAN ONE FRAME - FIX PLEASE :)".format(data[9],log)
            crash  #crashes program so user can fix boundaries
        all_Logs[log][soundInfo][study] = (data[8], data[9], data[10])
        
        i+=1
        return all_Logs, i
    ##########################

    match = False
    #The below commented line indicates what each index in the 'data' varible refers to
    #### data[2] = word #### data[4] = [p]eripheral/[m]edial #### data[5] = sound #### data[8] = targetframe #### data[9/10] = start/end Frame
    for word_sound in words_Sounds:
        if (word_sound[0] == data[2] and word_sound[1] == data[5]) or (word_sound[0] == data[2] and data[5] == ('X'+word_sound[1][-1])):
            study = word_sound[2]
            soundInfo = (data[2],data[4],data[5])
            match = True #match will be true if the sound exists
    if match == False: #not necessarily the case, but if match == False, typically indicates that a label for a sound was ommitted. 
        return all_Logs, i

    #if the sound label/boundaries were found, enter the information into the all_Logs dictionary
    all_Logs[log][soundInfo][study] = (data[8], data[9], data[10])
    i+=1
    return all_Logs, i


#********************************************************************************************************

#########################################################################################################
### EDITING TEXTGRIDS TO FIX INCONSISTENT LABELING
#########################################################################################################

def fix_Inputs(Ierrors, IDer1, IDer2, subject,cwd):
    #Called by findSameIteration(); calls changeLabels(), rewriteTextgrids()
    #This function chain enables the user to automatically fix mislabeled segements within the textgrid, and rewrites
    #the textgrid file to reflect these changes.  As is, it is very primative.  However, due to the advent of the 
    #autoTextgrid.py script, which automatically adds all labels to the textgrid prior to the user identifying the phone
    #boundaries, there are not as many labelling errors, and thus this function chain isn't used as often.  Yet, in the 
    #event of an error, this is necessary to avert the script from crashing.
    #Furthermore, this does not handle missing sounds very well.  Again, the likelyhood of a missing sound has been mitigated
    #due to autoTextgrid.py. Once the user has fixed any mislabeled segments, the Textgrid is rewritten and saved, and the
    #user will need to rerun this code, using the new "clean" textgrid, in order to Move any frames.

    path = os.getcwd()
    TG1 = {}    #dicts to hold [line of error] as keys and [change of sound] as values
    TG2 = {}    #these hold changes to make that will be identified
    reviewer1 = open((cwd+'/'+IDer1+'.TextGrid'), 'r').readlines()  #these are the textgrids for IDer1 and 2
    reviewer2 = open((cwd+'/'+IDer2+'.TextGrid'), 'r').readlines()
    fixes = 0
    errorCheck = set()  #ensures the same error isn't checked twice (once for each reviewer)
    Matchless = True      #If this returns true, then the words with errors were unable to be matched
    i = -1  #keeps track of the line index
    print 'Beginning revision process; unless you simply press enter or type "na", whatever you enter WILL replace the current sound in the textgrid!\n'
    for line in reviewer1:
        i += 1
        line = line.split(' ')
        if 'text' in line:  #finding the line with a word; below will check this word for matches against any mislabelled words or sounds
            for error in Ierrors:
                word = error[1][0]  #this is the word (that was previously identified as) having a mislabeled sound
                try:
                    if line[-2][0] != '"':      #formatting the word from the textgrid so two identical words will register a match
                        if line[-3][0] != '"':          #for three-word stimuli (only Cha do ghnoc) !!! Note: this may have changed, since Cha do ghnoc is now Cha-do-ghnoc
                            tgWord = line[-4][1:]+' '+line[-3]+' '+line[-2][:-1]
                        else:                           #for two-word stimuli
                            tgWord = line[-3][1:]+' '+line[-2][:-1]
                    else:       #for single-word stimuli in the textgrid
                        tgWord = line[-2][1:-1]

                    if tgWord == word:      #if the current word in the textGrid matches the error word, grab the start end endtime of the word boundaries
                        Matchless = False       #use these word boundary times to find the sounds later down in the TG file
                        if error[1] in errorCheck: #this eliminates unnecessary effort - without this, the same word will be checked multiple times
                            continue
                        else:                       #if not already in the set, add it so the word isn't checked again for additional reviewers
                            errorCheck.add(error[1])
                        ##########################
                        #The below four lines of code are used to help identify the sounds within the word, one of which may be missing or misspelled
                        #Because of the nature of the format of textgrids, the start and end times for the word (within the recording) are used to find 
                        #all labeled sounds that occur within that time window (the sounds are located further down in the textgrid).
                        startLine = reviewer1[i-2].split(' ')
                        startTime = float(startLine[-2])
                        endLine = reviewer1[i-1].split(' ')
                        endTime = float(endLine[-2])
                        ##########################
                        boundaries = False      #This acts as a check; if there are no sounds identified for a word, this alerts the user to this
                        TG1, TG2, fixes, boundaries = change_Labels(startTime, endTime, TG1, TG2, reviewer1, reviewer2, error, fixes, boundaries)   #send to fix labels
                        if boundaries == False:
                            print 'ERROR: There are no sounds identified for word: {0} and sound: {1}'.format(error[1][0],error[1][1])
                    else:       #If the current word in the textgrid doesn't match any mislabeled or missing words, continue on to the next word in Textgrid
                        continue
                except IndexError:
                    pass

    if len(TG1) != 0:       #if there were changes identified, rewrite the textgrid with those changes
        rewriteTextgrids(reviewer1, TG1, subject, IDer1,cwd)
    if len(TG2) != 0:
        rewriteTextgrids(reviewer2, TG2, subject, IDer2,cwd)
    return Matchless


def change_Labels(startTime, endTime, TG1, TG2, reviewer1, reviewer2, error, fixes, boundaries):
    #Called by fix_Inputs(); Calls findLabels()
    #This function handles the two different reviewers' textgrids, running through each to find the 
    #sounds associated with the word identified in fix_Inputs(). 
    
    #Since the time within the recording is used to identify the window that the word's sounds appear in,
    #these two 'tierReached' vars make sure the code doesn't begin checking for matches until tier 2 was reached,
    #in other words, it doesn't match the time window with the word (on tier 1), but reserves queries for tier
    #2 (the location of the sounds)
    tier2Reached1 = False   
    tier2Reached2 = False
    done1 = False   #Since different textgrids may be different lengths, these prevent a reviewer's textgrid from being
    done2 = False   #queried after its end has been reached, but before the other TG's end has been reached
    i = 0
    #there are two separate function calls, one for each reviewer - this because each reviewer's sound boundaries may be different
    while True:
        i += 1
        #Reviewer 1
        try:    #try necessary because len of 1 reviewer may be longer, run into index error
            line = reviewer1[i].split(' ')
            #the below function finds the sounds, and prompts the user to type in a replacement for any misspelled sounds
            tier2Reached1, newTG1, done1, fixes, boundaries = findLabels(startTime,endTime,TG1,reviewer1,line,i,error, tier2Reached1, done1, fixes, boundaries) #call function for reviewer

        except IndexError:  #if end of file has been reached, mark done
            done1 = True
        
        #Reviewer 2
        try:
            line = reviewer2[i].split(' ')  #same as above for the second reviewer
            tier2Reached2, newTG2, done2, fixes, boundaries = findLabels(startTime,endTime,TG2,reviewer2,line,i,error, tier2Reached2, done2, fixes, boundaries)

        except IndexError:
            done2 = True
            
        if done1 == True and done2 == True: #if the sounds for both words in each TG have been found, or the end of both TGs have been reached, break
            break
    return TG1, TG2, fixes, boundaries

def findLabels(startTime,endTime,TG,reviewer,line,i,error,tierReached, done, fixes, boundaries):
    #called by changeLabels()
    #This function identifies the sounds located within an identified word, and prompts the user for input to "fix"
    #any misspellings.  The user input is placed into a dictionary (TG) where they will later replace existing
    #sounds in the textgrid

    if 'item' in line and '[2]:\n' in line:     #these to strings are indicators that the second tier of the textgrid has been reached
        tierReached = True
        if tierReached == True: #if the loop has reached the second tier, mark true to begin checking the time stamps
            return tierReached, TG, done, fixes, boundaries
    if tierReached == False:    #if the second tier hasn't been reached, continue by returning to the previous function
        return tierReached, TG, done, fixes, boundaries

    try:    #try necessary because not all lines contain xmin - this is the first line we want to check, the startime
        if float(line[-2]) > startTime and 'xmin' in line:  #When the time of this sound (line[-2]) begins after the startTime of the word, we are in the right place
            wordStart = i   #set other variables equal to i, as to not change the value of i (i is the line number used in changeLabels())
            j = i
            while True:     #This loops through all sounds in the word, once found.  Since the sound is misspelled, the code can't be sure 
                            #which sound was "trying" to be spelled, and so it will loop through all sounds and ask the user to correct whichever
                            #was missing or misspelled.
                j += 1
                nextLine = reviewer[j].split(' ')
                if float(nextLine[-2]) < endTime: #if a sound change is made below, and the loop comes back here, this will 
                    wordEnd = j+1                   #result in a ValueError (nextLine[-2] not an integer) and break
                    #*********************************************************************************
                    print error #This and the below print statement are critical for the revision process
                    #if these print statements aren't here, user will not know what the sound is or what it should be
                    for x in range(wordStart-1,wordEnd+1): #print the starttime, endtime and sound for the given word
                        print x, reviewer[x]
                    #This line prompts the user to either correct the spelling (if misspelled), or hit [enter] or type 'na' if the sound is spelled correctly
                    fix = raw_input('Please enter correct spelling in the form (and optional 2nd argument): sound([space]condition(p or m)); or [enter] or "na" if no change needed.')
                    #********************************************************************************
                    boundaries = True  #mark var as true to indicate that sounds have been found for this word
                    if fix == '' or fix == 'na':     #if sound not needing to be changed, break, and loop will find 
                        break           #the next instance of 'xmin' for the next tier interval/sound

                    try:        #this is in case both the sound, and the [p]eripheral/[m]edial indicator, need to be fixed
                        fix = fix.split(' ')
                        TG[int(wordEnd)] = (fix[0],fix[1])    #placing the new sound (fix[0]) and the new indicator (p/m) (fix[1]) in the respective dict
                        fixes += 1
                    except:  #this is if just the sound needs to be fixed
                        TG[int(wordEnd)] = fix
                        pass
                    
                    done = True
                else:
                    break

    except ValueError:  #pass and return the variables if 'xmin' isn't in the line - the function 'above' this 
                        #one will call this function again to check the following line
        pass
    return tierReached, TG, done, fixes, boundaries

def rewriteTextgrids(oldTG, TG, subject, IDer, cwd):    #rewrite the textgrids to allow for sound changes to take place
    #called by fix_Inputs()
    #The purpose of this function is to rewrite the Textgrid(s) with the new fixed sounds.  With the sounds fixed automatically, it saves
    #the reviewer much time by eliminating the need to do it by hand.

    newdir = os.path.join(cwd,'Old_textgrids')
    originalTG = cwd+'/'+IDer+'.TextGrid'       #find the previous textgrid; this will be moved to another directory as the updated takes its place
    
    try:
        os.mkdir(newdir)
        version = 0
    except OSError:     #find the highest version number within previous textgrids, to know how to label TG to be moved
        previousFiles = os.listdir(newdir)
        versions = []
        if len(previousFiles) >0:   #check to make sure there are previous textgrids
            match = False
            for textGrid in previousFiles:

                if textGrid == '.DS_Store':     #ran into this problem before - not sure why; avoids finding the .DS_Store file
                    continue

                if textGrid[:(textGrid.rfind('.'))-1] != IDer:  #make sure to find the most recent textgrid for the CURRENT reviewer
                    continue
                else:
                    version = int(textGrid[(textGrid.rfind('.')-1):(textGrid.rfind('.'))])
                    versions.append(version)
                    match = True
            if match == False:  #this is here just incase there are no previous textgrids in the directory - though there should be
                shutil.move(originalTG, (newdir+'/'+IDer+'1.TextGrid'))
            else:
                nextVersion = str(max(versions)+1)      #otherwise, mark it with the next version number
                shutil.move(originalTG, (newdir+'/'+IDer+nextVersion+'.TextGrid'))
        else:
            shutil.move(originalTG, (newdir+'/'+IDer+'1.TextGrid'))
        pass
    output = open((cwd+'/'+IDer+'.TextGrid'), 'w')   #create new TG output file
    i = -1
    for line in oldTG:
        i+=1
        try:    #see if an error has been fixed in that line (if there is one present in the TG dict)
            TG[i]
            #formatting and replacing the previous sound (that was already there) with the fixed sound
            newline = ''
            line = line.split(' ')
            try:                    #this try statement replaces the m/p condition
                condition = TG[i][1]    #if it the argument is entered in the input
                for word in line[:-3]:
                    newline += word+" "
                newline += ('"'+TG[i][1]+' '+TG[i][0]+'"\n')#'"\n')
                output.write(newline)
            except:
                if '"m' in line[-2] or '"p' in line[-2]:    #not sure if some lines are formatted the same
                    for word in line[:-1]:                  #this 'if' is here as a check to make sure the "m/p isnt
                        newline += word+" "                 #removed from the line accidentally
                else:
                    for word in line[:-2]:
                        newline += word+" "
                newline += (TG[i][0]+'"\n') #'"\n')
                output.write(newline)

        except KeyError:    #if no error exists (or was fixed) for that line, write the pre-existing line
            output.write(line)
            pass
    output.close()
        
                                
#**********************************************************************************************************

###########################################################################################################
### IDENTIFYING ERRORS BETWEEN ORTHOGRAPHIC LABELING (IERRORS) AND FRAME IDENTIFICATION (MERRORS)
###########################################################################################################
         
def findSameIteration(all_Logs, IerrorReport, MerrorReport, cwd, noMatches):    #currently only set up to handle comparing two reviewers at once
    compared = set()                                                            #@ once; however, iterates through all textgrids to compare all 
    Merrors = set() #holds all frame number discrepancies; M[ismatch]errors     #reviewers, two at a time (ie. a&b, a&c, & b&c)
    hit = False         #determines if the loop was entered - it not, prints error message for easier debugging
    for log in all_Logs:
        hit = True
        #separate subject from reviewer
        underscore = log.rfind('_')
        subject = log[:underscore]
        for log2 in all_Logs: #log(2) refers to the framelog information for a given reviewer; literally, the string containing subject_reviewer
            if (log,log2) in compared or (log2,log) in compared:    #if both logs have been compared with each other, continue
                continue
            else:
                compared.add((log,log2)) #add to set-ensure same comparison not redone; allow comparison of different reviewers for same subj
            
            Ierrors = set() #holds all input (orthographic) discrepencies between reviewers, and between reviewers and the master sound list; I[nput]errors
            if log == log2: #don't compare the same subj-reviewr
                continue
            print '************************\nCOMPARING {0} WITH {1}\n*************************'.format(log, log2)
            underscore = log2.rfind('_')
            subject2 = log2[:underscore] #compare 
            if subject == subject2: #but make sure subj's DO match
                Merror, Ierror, Merrors, Ierrors = compareNumbers(all_Logs,log,log2,Merrors,Ierrors,subject) #comparing the frame numbers
                if len(noMatches)>0:    #this is here incase no discrepencies were found b/w reviewers, but there are missing sounds
                    Ierror = True

                if Ierror == True:  #if there are orthographic discrepencies
                    print Ierrors#, noMatches
                    Ierror_tmp = set()
                    for noMatch in noMatches:
                        match = False
                        for error in Ierrors:
                            if error[1][0] == noMatch[1][0] and error[1][2] == noMatch[1][0]:
                                match == True
                                break
                        if match == False:
                            Ierror_tmp.add(noMatch)
                    Ierrors = Ierrors.union(Ierror_tmp)
                    # print Ierrors
                    Matchless = fix_Inputs(Ierrors, log, log2, subject,cwd) #calls the above functions to relabel the textgrids
                    print 'Refigure framelog files for {0}'.format(subject)
                    if Matchless == True:
                        print 'ERROR!!! ~ SOUND NAMING ERRORS WERE NOT FIXED. MAKE SURE THE WORDS IN THE TEXTGRIDS AND THE WORDS IN Words_Sounds.txt MATCH'
                        print 'FOR SCOTS GAELIC, MAKE SURE TO RETYPE THE WORD "ACHLAIS" WITH "ACHLAIS" IN THE TEXTGRIDS - TO FIX AN ENCODING MISMATCH'
                        print 'Alternatively, make sure all words are present in each textgrid - if a word is missing in the first tier, this error is risen'
                        break
                    IerrorReport[subject] = (log, log2) #put entry in IerrorReport - to be printed once script is finished
                    
                if Merror == True:  #if there are frame mismatch errors b/w reviewers
                    MerrorReport[subject] = MerrorReport[subject]|(Merrors)  #put entry in MerrorReport - to be printed once script is finished

            if len(Ierrors) == 0:   #only if there are no labeling errors, fill out the excel.  If there are, rerun Dan's praat script, and rerun this script
                fillOutExcel(Merrors, all_Logs)     #in order to make sure all sounds match, and all frame numbers are placed in the excel
    if subject not in IerrorReport and subject not in MerrorReport:      #if there are no discrepancies whatsoever b/w reviewers, copy, move, and rename the frame files
        move_RenameFrames(all_Logs, log, subject,cwd)

    if hit == False:
        print 'ERROR: no logs in all_Logs. Make sure there is a .framelog file in the appropriate directory.'
    return IerrorReport, MerrorReport
                
#compares frames ID'd
def compareNumbers(all_Logs,log,log2, Merrors,Ierrors,subject):
    Merror = False  #Mismatch error (wrong frames)
    Ierror = False  #Intput error (wrong spelling)
    for word_sound in all_Logs[log]:
        for study in all_Logs[log][word_sound]:
            #needs try to avoid Keyerror (if letters for sound not the set
            #between reviewrs)                                                      
            try:    #this will succeed only if there are no labeling errors
                frames1 = all_Logs[log][word_sound][study]
                frames2 = all_Logs[log2][word_sound][study]
                if frames1 == frames2 or word_sound[2][:-1] == 'X' or word_sound[2] == 'neutral':  #if the frame numbers are the same, there are no errors
                    continue
                else:   #occurs when frame #s don't quite add up
                    Merror = True
                    Merrors.add((word_sound,log,frames1,log2,frames2))
            except KeyError: #if sound transcription doesn't match, add to Ierrors
                if word_sound[2] == 'neutral':
                    try:
                        frames1 = all_Logs[log][word_sound][study]
                        continue
                    except KeyError:
                        try:
                            frames2 = all_Logs[log2][word_sound][study]
                            continue
                        except:
                            pass
                Ierror = True #if errors are found, the following code will find the textgrid, and take the user through a process to fix the errors
                Ierrors.add((log,word_sound,log2))
    return Merror, Ierror, Merrors, Ierrors


#************************************************************************************************

#################################################################################################
### FILLING OUT THE EXCEL FILE FOR THE GIVEN REVIEWERS/SUBJECTS, MARKING ERRORS
#################################################################################################

def fillOutExcel(Merrors, all_Logs):    #entering the new data into the master excel file
    print '**************************WRITING EXCEL FILE...***************************'
    cwd = os.getcwd()
    filelist = [i for i in os.listdir(cwd) if i.endswith('.xls')]  #in order to work this script must be in the same cwd as the excel file
    newdir = cwd+'/Old_xls'
    try:    #if this is the first time the script is run, it creates a new dir to place the old verisons of the xls
        os.mkdir(os.path.join(cwd, 'Old_xls'))
    except:
        pass
    oldXls = os.path.join(cwd, 'Old_xls')
    for fl in filelist:     #as with the textgrids above, this finds the previous version num of the most recent textgrid
        if 'Identification_log' in fl:
            oldVersion = fl
            newVersionNum = int(fl[fl.rfind('.')-2:fl.rfind('.')])+1
            if len(str(newVersionNum))<2:
                newVersionNum = '0'+str(newVersionNum)
            # the textgrid will be moved after the new one is ready to be written and saved
            
    excelFile = xlrd.open_workbook(os.path.join(cwd,oldVersion))    #open the most recent excel
    sheetList = excelFile.sheet_names()     #get a list of the sheets in the excel
    IDer_Target = {}    #dict to store target frames (corresponding the the 'm' or 'p' designation, as ID'd by Dan's framelog file)
    IDer_InitF = {}     #dict to store initial and final frames within the sound's range
    endOfRow = 17       #current extent of row (# of columns) plus a few; can be changed if more reviewers are added
    oldSubjs = set()
    for log in all_Logs:
        subject = log[:log.rfind('_')].lower()
        if subject in oldSubjs:
            continue
        else:
            oldSubjs.add(subject)
        for sheet in sheetList:
            if int(sheet) == int(subject[:-1]):
                loadedSheet = excelFile.sheet_by_name(sheet)
                try:    #try, otherwise index error occurs when the true end of the row is reached; this loop gathers the IDer columns with their column #s
                    for i in range(6,endOfRow):		#i is the index within the "row"; i.e. the cell/column number
                        if loadedSheet.cell(1,i) != '': #checks to make sure the current cell isn't empty
                            cell = str(loadedSheet.cell(1,i))
                            cell = cell[cell.find("'")+1:cell.rfind("'")] #transfering the xlrd object to a string object and formatting
                            if cell == '':
                                continue
                            try:
                                cell = cell.split(' ')
                            except:
                                pass
                            try:    #store initial and final IDer columns in a different dict
                                if cell[1] == 'initial':
                                    cellLower = cell[0].lower()
                                    IDer_InitF[str(cellLower)] = (i,i+1)
                                if cell[1] == 'final':
                                    continue
                            except Exception, e:    #store target IDer columns in a different dict
                                IDer_Target[str(cell[0].lower())] = i
                except IndexError:
                    pass

                write_Sheet = copy(excelFile)   #use xlutils to copy the existing xlrd excel file so it can be written :(this so existing information stays)
                color = xlwt.easyxf('pattern: pattern solid, fore_colour red')     #this line establishes the color for errors
                i = 1		#here, 'i' corresponds to the row number
                while True:
                    try:
                        i+=1    #i corresponds to row num
                        if str(loadedSheet.cell(i,1))[-4:-1] == subject:
                            
                            cellWord = str(loadedSheet.cell(i,2))   #here (and below) format cell object as string and format
                            cellWord = cellWord[cellWord.find("'")+1:cellWord.rfind("'")]
                            cellSound = str(loadedSheet.cell(i,5))
                            cellSound = cellSound[cellSound.find("'")+1:cellSound.rfind("'")]
                            cellStudy = str(loadedSheet.cell(i,3))
                            cellStudy = cellStudy[cellStudy.find("'")+1:cellStudy.rfind("'")]

                            # BELOW IS **ABSOLUTELY** CRITICAL - NEED TO DECODE FROM UNICODE, REENCODE IN UTF8
                            cellWord = cellWord.decode('unicode-escape').encode('utf-8')
                            ################################################################

                            cellSound = cellSound[:-4]

                            for log in all_Logs:	#get thru all layers of all_Logs to find frame numbers to enter
                                match = False       #Match is outside the outer for-loop (despite the break), because in the inner "study" for-loop, there is only ever 1 study
                                iteration = log[log.rfind('_')-1:log.rfind('_')]
                                IDer = log[log.rfind('_')+1:]
                                for word_Sound in all_Logs[log]:
                                    if match == True:   #break if a line was printed to avoid unnecessary iteration
                                        break
                                    word = word_Sound[0] #.decode('unicode-escape')
                                    sound = word_Sound[2]
                                    if sound == 'neutral':  #this passes neutral frames, and so they aren't added to the excel file
                                        continue
                                    for study in all_Logs[log][word_Sound]:
                                        if word == cellWord:

                                            if sound == cellSound.rstrip() or (sound[:-1] == 'X' and sound[-1] == cellSound.rstrip()[-1]):

                                                if len(study) == 1:
                                                    if str(cellStudy)[0] != '(':
                                                        cellStudy = (cellStudy,)

                                                if '"' in cellStudy:
                                                    cellStudy = tuple(cellStudy[1:-1].split(','))
                                                if study == cellStudy:
                                                    if sound[:-1] == 'X':
                                                        write_Sheet.get_sheet(int(sheet)-1).write(i,IDer_Target[IDer], 'XXX'+all_Logs[log][word_Sound][study][0], easyxf("align: horiz right"))  #, color)  #for some reason, color only worked for 3rd
                                                        write_Sheet.get_sheet(int(sheet)-1).write(i,IDer_InitF[IDer][0], 'XXX'+all_Logs[log][word_Sound][study][1], easyxf("align: horiz right"))  #, color)   #iteration. used !'s instead, but would like
                                                        write_Sheet.get_sheet(int(sheet)-1).write(i,IDer_InitF[IDer][1], 'XXX'+all_Logs[log][word_Sound][study][2], easyxf("align: horiz right"))  #, color)   #to use color at a later date if can figure out
                                                        match = True
                                                    if match == True:
                                                        break
                                                    for error in Merrors:
                                                        Miteration = error[1][error[1].find('_')-1:error[1].find('_')] #compares subject iteration to row label
                                                        if iteration == Miteration:
                                                            #if there is an mismatch (frame#) error associated with the sound/word, fill cells red to indicate
                                                            if error[0][0] == word and error[0][2] == sound:
                                                                write_Sheet.get_sheet(int(sheet)-1).write(i,IDer_Target[IDer], '!!!'+all_Logs[log][word_Sound][study][0], easyxf("align: horiz right"))  #, color)  #for some reason, color only worked for 3rd
                                                                write_Sheet.get_sheet(int(sheet)-1).write(i,IDer_InitF[IDer][0], '!!!'+all_Logs[log][word_Sound][study][1], easyxf("align: horiz right"))  #, color)   #iteration. used !'s instead, but would like
                                                                write_Sheet.get_sheet(int(sheet)-1).write(i,IDer_InitF[IDer][1], '!!!'+all_Logs[log][word_Sound][study][2], easyxf("align: horiz right"))  #, color)   #to use color at a later date if can figure out
                                                                match = True
                                                                break

                                                    if match == True:   #if an error was printed to a cell, break instead of overwriting it
                                                        break           
                                                    
                                                    #if there is no error associated with the current row, write to cells without color indicator
                                                    write_Sheet.get_sheet(int(sheet)-1).write(i,IDer_Target[IDer], all_Logs[log][word_Sound][study][0], easyxf("align: horiz right"))     #for target sound
                                                    write_Sheet.get_sheet(int(sheet)-1).write(i,IDer_InitF[IDer][0], all_Logs[log][word_Sound][study][1], easyxf("align: horiz right"))
                                                    write_Sheet.get_sheet(int(sheet)-1).write(i,IDer_InitF[IDer][1], all_Logs[log][word_Sound][study][2], easyxf("align: horiz right"))
                                                    match = True
                                                    break
                                
                        else:   #elif the subject and sheet/line don't match
                            continue
                    except IndexError:  #if the index overextends
                        break

    shutil.move(os.path.join(cwd,oldVersion),oldXls)    #move the old excel file to the "oldXls"folder
    write_Sheet.save(os.path.join(cwd, 'Identification_log_{0}.xls'.format(newVersionNum)))  # write the new sheet with the new version number in name

#************************************************************************************************

#################################################################################################
### RENAMING AND COPY/MOVING CORRECTLY IDENTIFIED FRAME .PNG FILES TO NEW DIRECTORY
#################################################################################################
             
def move_RenameFrames(all_Logs, log, subject,cwd):  #this only occurs when no Input or frame Mismatch errors were present
    ##########uncomment below, comment out while loop, to hard-set an answer
    # move = 'a'
    ###################################### Don't delete below
    while True:
        move = raw_input('\nAll comparisons yielded total and utter agreement.  Would you like to move all identified frames, [T]arget frames, or none?\n[a/t/n]: ')
        if move == 'n':
            return
        elif move == 'a' or move == 't':
            break
        else:
            print 'You must enter a valid input: a/t/n'
    #######################################

    print 'MOVING FRAMES...\n'
    framedir = os.path.join(cwd, 'frames')
    frames = [i for i in os.listdir(framedir) if '.png' in i or '.jpg' in i]

    newdir = os.path.join(cwd,'Target_frames')
    if os.path.isdir(newdir):
        pass
    else:
        os.mkdir(newdir)
    tracedir = os.path.join(cwd, 'Completed_traces')
    if os.path.isdir(tracedir):
        pass
    else:
        os.mkdir(tracedir)
    tracers = set()
    Completed_traceSet = set([])
    Completed_traceALLFiles = [i for i in os.listdir(tracedir)]  #all files in Completed_traces
    Completed_traces = [i for i in os.listdir(tracedir) if '.traced.txt' in i]   #all trace files in completed Traces

    for fl in Completed_traces:

        try:
            flEnd = fl[fl.rfind('_')-5:fl.rfind('_')-2]+fl[fl.rfind('_'):fl.find('.')]  #set of sounds+frame numbers of the trace files
            Completed_traceSet.add(flEnd)
        
        except IndexError:
            if 'CX' in fl:      #hack
                continue

    for word_Sound in all_Logs[log]:

        if 'X' in word_Sound[2]:        #If the sound is labeled as non-existent, then don't move the frames associated with the label
            continue
        for study in all_Logs[log][word_Sound]:
            multipleFrames = []
            frameNums = all_Logs[log][word_Sound][study]
            #########################################
            ####### the below code is for the target frame only
            zeros = 7-len(frameNums[0])     #get the number of zeros for the target frame
            frameNumTarg = 'frame-'+('0'*zeros)+frameNums[0]
            #########################################
            ####### the below code is for getting multiple frames
            for i in range(int(frameNums[1]), int(frameNums[2])+1):     #get all frames within the range; need '+1' at end to get the last frame
                zeros = 7-len(str(i))    #get the number of zeros required for given frame
                multipleFrames.append(('frame-'+('0'*zeros)+str(i)))
            #########################################

            if '(' in str(study):
                newStudies = ''
                for stdy in str(study)[1:-1].split(','):         #extract the relevant studies and construct into a string
                    if len(stdy) <1:
                        continue

                    newStudies += '{0};'.format(str(stdy).strip()[1:-1])
   
                study = newStudies[:-1]

            try:    #create new folder to place the target frames
                os.mkdir(newdir)
            except OSError:
                pass

            for frame in frames: #find and get info from the frame files within the frames folder
                dot = frame.rfind('.')
                frame2 = frame[:dot]
                newpath = os.path.join(newdir, frame)   #establish new and old paths
                newTracePath = os.path.join(tracedir, frame)

                frameSrc = os.path.join(framedir, frame)
                targetName = subject+'_'+str(study)+'_'+word_Sound[0]+'_'+word_Sound[1]+'_'+word_Sound[2]+'_'+'T'+'_'+frame     #include T to indicate target frame
                rangeName = subject+'_'+str(study)+'_'+word_Sound[0]+'_'+word_Sound[1]+'_'+word_Sound[2]+'_'+'R'+'_'+frame     #include R indicating range
                
                if frame2 in multipleFrames:
                    if frame2 == frameNumTarg:  #if the frame also happens to be the target frame, copy again and rename slightly differently
                        moveANDrename(targetName,Completed_traceSet,Completed_traceALLFiles,Completed_traces, frameSrc,newTracePath,tracedir,newpath,newdir)
                        continue
                    
                    if move == 'a':
                        moveANDrename(rangeName,Completed_traceSet,Completed_traceALLFiles,Completed_traces,frameSrc,newTracePath,tracedir,newpath,newdir)                                            
                        continue

    print 'SUCCESS: {0}; MOVED FRAMES'.format(subject)


def moveANDrename(targetName,Completed_traceSet,Completed_traceALLFiles,Completed_traces,frameSrc,newTracePath,tracedir,newpath,newdir):
    targetEnd = targetName[targetName.rfind('_')-5:targetName.rfind('_')-2]+targetName[targetName.rfind('_'):targetName.find('.')]
    # targetEnd matches the flEnd from above - a combination of the sound+frame number of the trace file
    if targetEnd in Completed_traceSet:      #if either the trace, the image file, both, or more than one copy of each
        if targetName in Completed_traceALLFiles:    #if the trace file matches exactly
            traceMatches = [i for i in Completed_traces if targetEnd[:3] in i and targetEnd[4:] in i]
            fileMatches = [i for i in Completed_traceALLFiles if targetEnd[:3] in i and targetEnd[4:] in i and '.traced' not in i]
            if len(fileMatches) > 1:
                for f in fileMatches:
                    if f != targetName:
                        try:
                            os.remove(os.path.join(tracedir,f))
                        except OSError:
                            print 'File {0} not found!'.format(f)
            # traceMatches = [i for i in Completed_traces if targetEnd in i] #[:3] in i and targetEnd[3:] in i]
            if len(traceMatches) == 1:      #if all is good and well, ignore
                try:
                    os.rename(os.path.join(tracedir,traceMatches[0]), os.path.join(tracedir, (targetName[:targetName.find('.')]+traceMatches[0][traceMatches[0].find('.'):])))
                except OSError:
                    print 'Error: File not found: {0}'.format(traceMatches)
                # if targetEnd[] in os.listdir(newdir):
                imageMatches = [i for i in os.listdir(newdir) if targetEnd[:3] in i and targetEnd[4:] in i]
                for i in imageMatches:
                    os.rename(os.path.join(newdir, i), os.path.join(tracedir,targetName))
                return
            if len(traceMatches) > 1:       #remove excess/duplicate traces
                j = 0
                print 'one of these MAY need to be deleted: {0}'.format(traceMatches)

            return
        elif targetName not in Completed_traceALLFiles:
            # traceFiles = [i for i in Completed_traceALLFiles if targetEnd in i]
            traceFiles = [i for i in Completed_traces if targetEnd[:3] in i and targetEnd[4:] in i]
            
            # traceFiles = [i for i in Completed_traceALLFiles if targetEnd in i]
            for i in traceFiles:

                if '.traced' in i:# and targetName[targetName.rfind('_')-4:targetName.rfind('_')-2] in i:
                    try:
                        os.rename(os.path.join(tracedir, i), os.path.join(tracedir, ( targetName[:targetName.rfind('_')] + i[i.rfind('_'):])))
                    except OSError:
                        print 'File not found!:', targetName, i, targetEnd, targetName[:targetName.rfind('_')], i[i.rfind('_'):], '\n'
                else:
                    try:
                        os.remove(os.path.join(tracedir,i))
                    except:
                        print 'The file: {0} not found in "Completed_traces" for removal\n'.format(os.path.join(tracedir, i))

            shutil.copy2(frameSrc, newTracePath)
            os.rename(newTracePath, os.path.join(tracedir, targetName))
    else:
        removing = [i for i in Completed_traceALLFiles if targetEnd in i and '.traced' not in i]
        for i in removing:
            try:
                os.remove(os.path.join(tracedir,i))
            except OSError:
                print 'Could not find {0} in "Completed_traces"'.format(i)
        shutil.copy2(frameSrc, newpath)
        os.rename(newpath, os.path.join(newdir, targetName))
    return

#*************************************************************************************************



##################################################################################################
### MY VERSION OF DAN'S GETFRAMES.PRAAT SCRIPT THAT RUNS AUTOMATICALLY WHEN IERRORS ARE FOUND
##################################################################################################

def runGetFramesPraat(subject, folder, IerrorReport, path):
    cwd = os.path.join(path, folder)
    try:
        FrameRates = open(path+'/Frame_Rates.txt', 'rU').readlines()
        for xline in FrameRates:         #read from a file to get precalculated framerates
            line = xline.split("\t")      #for future use, could find audio length, div
            if subject[0] == '0':       #by number of frame files
                subjectAdj = subject[1:]
            else:
                subjectAdj = subject

            if subject[0] == '0':           #allows for single-digit, zero-initial subjects (e.g. '07c')
                if line[0] == subject[1:]:
                    frameRate = line[3]
            else:
                if line[0] == subject:
                    frameRate = line[3]     #grab the frame rate for the given subject from Frame_Rates.txt
    except IOError:
        print 'Frame_Rates.txt is not found.'
        frameRate = raw_input('Enter frame rate # for {0}, or any a-z character to break: '.format(subject))
    print 'Frame Rate: ', frameRate
    for IDer in IerrorReport[subject]:
        fl = os.path.join(cwd,'{0}.TextGrid'.format(IDer))
        newTG = open(fl, 'rU').readlines()      #open the just-created textgrid file
        newFrameLog = open(os.path.join(cwd,'{0}_framelog.txt'.format(IDer)), 'w')   #open a new FrameLog file to write
        # firstLine = ['File','Tier2Interval','Word','Label','TargetFrameCriterion','Phone','StartTime','EndTime','TargetFrame','StartFrame','EndFrame','MedialFrame','StopsFrame']
        newFrameLog.write('{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}\t{10}\t{11}\t{12}\n'.format('File','Tier2Interval','Word','Label','TargetFrameCriterion','Phone','StartTime','EndTime','TargetFrame','StartFrame','EndFrame','MedialFrame','StopsFrame'))
        # i =1
        # for item in firstLine:      #write the first header line for the file
        #     if i == len(firstLine):
        #         newFrameLog.write(item+'\n')
        #     else: 
        #         newFrameLog.write(item+'\t')
        #     i+=1
        secondTier = False      #set as false until the second tier has been reached
        j = 0
        # lastword = 'start'  #initialize the 'lastword' variable so it can be used for the first word
        startMatch = False
        for line in newTG:
            if secondTier == True:
                if 'text' in line:
                    # print IDer
                    # print type(lastword)
                    # print type(newFrameLog)
                    # print type(newTG)
                    if startMatch == False:
                        lastword = 'start'
                    lastword, newFrameLog, startMatch = fillOutFramLog(fl, line, newTG, j, frameRate, lastword, newFrameLog, startMatch)   #lastword, newFrameLog = ...
                    # print type(lst)
                    # if type(lst).__name__ != 'file':
                    #     lastword = lst[0]
                    #     newFrameLog = lst[1]
                    # else:
                    #     newFrameLog = lst
                j+=1
                continue
            if 'item' in line and '[2]' in line:        #true when reached the phoneme tier
                secondTier = True
                j+=1
                continue
            j+=1

    newFrameLog.close()

def fillOutFramLog(fl, line, newTG, j, frameRate, lastword, newFrameLog, startMatch):
    if '""' in line or 'xx' in line:
        return lastword, newFrameLog, startMatch

    if '"m' in line.split()[-2] or '"p' in line.split()[-2]:
        condition = line.split()[-2][1:]
        sound = line.split()[-1][:-1]
    elif '"X' in line.split()[-1]:      #include 2 elif conditions incase there is an X (non-existent sound)
        condition = 'x'
        sound = line.split()[-1][1:-1]
    elif 'X' in line.split()[-2]:
        condition = 'x'
        sound = line.split()[-2]
    elif 'neutral' in line.lower():
        condition = 'N'
        sound = 'neutral'
    else:                       #this is here if "'s are separated from sound
        condition = line.split()[-3]
        sound = line.split()[-2]

    intervalLin = newTG[j-3].split(" ")
    interval = intervalLin[-1][1:intervalLin[-1].rfind(']')]
    startTime = float((newTG[j-2].split(" "))[-2])
    endTime = float((newTG[j-1].split(" "))[-2])
    word,newWord,startMatch = findWord(newTG, startTime, endTime, sound, lastword, startMatch)
    frameDur = float(1)/float(frameRate)
    endFrame = int(math.floor(endTime / frameDur) + 1)
    startFrame = int(math.floor(startTime / frameDur) + 1)
    if condition == 'p' and sound[-1] == 'i':   #if an initial stop, automatically choose the startFrame
        startFrame = endFrame - 4

    elif sound[:-1] in fricatives and sound[-1] == 'i':  #if initial fricative, choose startFrame
        new_startFrame = endFrame - 5
        if new_startFrame < startFrame:     # if the user chose a longer sequence than 5 frames, take the longer sequence
            startFrame = new_startFrame

    if sound[:-1] in sonorants and sound[-1] == 'f':     #if final sonorant, choose endframe
        new_endFrame = startFrame + 5
        if new_endFrame > endFrame:
            endFrame = new_endFrame

    medialFrame = int(math.floor((endFrame - startFrame) / 2) + startFrame)
    stopsFrame = int(math.floor(endTime / frameDur))
    writeNewFrameLog(fl,interval,newWord,condition,sound,startTime,endTime,startFrame,endFrame,medialFrame,stopsFrame, newFrameLog)
    
    return word, newFrameLog, startMatch



def findWord(newTG, startTime, endTime, sound, lastword, startMatch):
    k = -1
    firstTier = False       #ensure that the first t
    for line in newTG:
        k+=1
        if 'intervals' in line and '[1]' in line:   #check to see if first tier has been reached
            firstTier = True
        if firstTier == True:            #only enter if the first tier is reached
            line = line.split(" ")
            if 'xmin' in line:
                if startTime > float(line[-2]):                     #make sure the correct word is found; if start & end of sound within word time-boundaries
                    if endTime < float((newTG[k+1].split(" "))[-2]):
                        thisword = "".join(c for c in unicodedata.normalize('NFD', (newTG[k+2][newTG[k+2].find('"')+1:newTG[k+2].rfind('"')]).decode('utf-8')) if not unicodedata.combining(c)) #extract word from line; unicode mess to account for accented chars
                        if sound == 'neutral':
                            if lastword == 'start':
                                nextword = "".join(c for c in unicodedata.normalize('NFD', (newTG[k+6][newTG[k+6].find('"')+1:newTG[k+6].rfind('"')]).decode('utf-8')) if not unicodedata.combining(c))
                                newWord = lastword+'+'+nextword
                                newWord = newWord.replace(' ','-')
                                return nextword, newWord, True
                            elif lastword == thisword: #or lastword == 'start':  #assumes the next word is six rows down in the textgrid - as is the current TG format.
                                nextword = "".join(c for c in unicodedata.normalize('NFD', (newTG[k+6][newTG[k+6].find('"')+1:newTG[k+6].rfind('"')]).decode('utf-8')) if not unicodedata.combining(c))
                                newWord = lastword+'+'+nextword
                                newWord = newWord.replace(' ','-')
                                if startMatch == False:
                                    return nextword, newWord, False
                                else:
                                    return nextword, newWord, True
                            elif lastword != thisword:
                                newWord = lastword+'+'+thisword
                                newWord = newWord.replace(' ','-')
                                if startMatch == False:
                                    return thisword, newWord, False
                                else:
                                    return thisword, newWord, True
                        else:
                            word = thisword 
                            word = word.replace(' ','-')
                            if startMatch == False:
                                return word, word, False
                            else:
                                return word, word, True
        if 'item' in line and '[2]' in line:        #break when the second tier is reached
            break

def writeNewFrameLog(fl,interval,word,condition,sound,startTime,endTime,startFrame,endFrame,medialFrame,stopsFrame, newFrameLog):

    if condition == 'm':
        if sound[:-1] in fricatives and sound[-1] == 'i':   #this to make sure the initial fricatives' target frame is the second to last (like stops)
            targetFrame = stopsFrame
        else:
            targetFrame = medialFrame
    elif condition == 'p':
        targetFrame = stopsFrame
    elif condition == 'N':   #neutral frames
        targetFrame = endFrame
    else:                   #there should not be an else - if TF is 0, something has gone wrong (or I added neutral frames without realizing this was here)
        targetFrame = 0
    newFrameLog.write('{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}\t{10}\t{11}\t{12}\n'.format(fl,interval,word,(condition+' '+sound),condition,sound,str(startTime),str(endTime),str(targetFrame),str(startFrame),str(endFrame),str(medialFrame),str(stopsFrame)))
    return newFrameLog


#*************************************************************************************************

##################################################################################################
### USER INPUT FUNCTIONS: IDENTIFYING DESIRED SUBJECTS AND STUDIES (AND CONSEQUENTLY STIMULI)
##################################################################################################

def get_Subjs():    #user interface to identify subjects in question
    #Called by main()
    #Asks the user to identify the subjects. For format, see below.  If running multiple iterations on the same subject(s),
    #this can be hard-coded easily to bypass the user interface.  See ######-surrounded regions below for different
    # options.  When done entering subjects, must type "done".  A check will come up, asking the user to review the 
    #current list of subjects.  Here, they can be removed (no longer added); must type "y" to indicate the list is OK.
    #
    #Note that below, there are a few lines: 'subject.remove'; these iterations do not exist for the ScotsGaelic data - if using
    #different data, these lines should be commented out or removed.
    #
    #Also note the lines: 'subject.add', which combine the 'subject' variable with a letter [abc].  This assumes there are only 
    #three repetitions.  If a study has more or fewer repetitions, this should be changed accordingly here, and all throughout the code
    #where a, b, and c are assumed to be the repetitions
    ###################################
    # subjects = set(['05a']) #use code if you need/want to preset a given subj(s) several times in a row, without having to reenter info
    # return subjects
    ###################################
    subjects = set()
    print "Identify target subject(s) (e.g '7' will return 7[a,b,c]; 'all' will return 1-26[abc]). Type 'done' when done. "
    while True:
        subject = raw_input("Enter subject number(s): ")
        ###############     comment above and use below to preset input if all
        # subject = 'all'
        ###############
        if subject == 'done':   #break when done enterint subjects
            break
        if subject == 'all':    #if 'all' entered, add all subjects (in range 26, for the 26 Gaelic subjects
            for i in range(1,27):
                for j in ['a','b','c']:
                    if len(str(i)) == 1:
                        subjects.add(('0'+str(i)+j))
                    else:
                        subjects.add((str(i)+j))
            subjects.remove('04b')       #these are added to avoid error - these iterations do not exist
            subjects.remove('21b')
            subjects.remove('22c')
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
        

def get_Studs():    #user interface to identify studies in question
    #Called by main()
    #Very similar to above get_Subjs().  Works in the same way - for a little more detail, see above function.
    ###############################
    # studies = set(['11', '1', '2'])   #use above ### if you need/want to preset a given stud(s) several times in a row, without having to reenter info
    # return studies
    ###############################
    studies = set()
    print "Identify target study, one at a time [1-14], or enter 'all' for all studies. When done, type 'done'. "
    while True:
        # study = raw_input("Enter study number(s): ")
        ################# comment above and use below to preset to all
        study = 'all'
        #################
        if study == 'done': #same as above
            break
        if study == 'all':
            for i in range(1,15): #same as above, except there are no 'abc' studies
                studies.add(str(i))
            # studies.add('?')  #if any current stimuli don't have a specific study (indicated by '?'), add them if 'all' is specified
            break
        studies.add(study)
    print 'Double check list. Type "y" to continue, or the name of element to remove.' #same as above
    print studies
    while True:
        # element = raw_input('"y" or element to remove: ')
        ############### comment above and uncomment below if you want to preset an input
        element = 'y'
        ###############
        if element == 'y':
            print 'Final studies: ', studies
            return studies
        else:
            try:
                studies.remove(element)
                print studies
            except:
                print "You must enter valid input."
    

def get_Words(studies): #gathers the target words and sounds for the studies currently interested in
    #Called by main()
    #This opens the words_Sounds.txt file, which contains a list of stimuli in the format "word\tstudy\tsoundlabel".  Each
    #stimuli is chosen based on its study (obtained from above user interface).  All the information is put into the 
    #words_Sounds set, with the word, soundlabel, and stud(y|ies) as the entry.  This is used in many other
    #functions to identify sounds/words and determine the correct soundlabel

    words_Sounds = set()
    words_Studies = open('Words_Sounds.txt', 'rU').readlines()  #document containing textgrid labels (sound) and study number
    i=0
    for line in words_Studies:                                     #this doc MUST be in the cwd with this script
        i+=1
        line = line.split("\t")
        word = line[0]
        sound = line[2].strip()    #removes '\n' character from textgrid label
        key = [word,sound]

        if '"' in line[1]:  #formats the list of studies
            ln = line[1][1:-1]
            ln1 = ln.split(',')

            for stdy in ln1:
                if stdy in studies:        
                    s = tuple(ln1)
                    key.append(s)
                    key = tuple(key)

                    words_Sounds.add(key)
                    break

        else:
            if line[1] in studies:# or line[1] == '?':    #this latter '?' part is a hack/fix to allow for the affricated stops (same sound and word) to be identified; should be changed
                ln = (line[1],)
                key.append(ln)
                key = tuple(key)
                words_Sounds.add(key)

    return words_Sounds

#*****************************************************************************************************************************

##############################################################################################################################
### MAIN FUNCTION
##############################################################################################################################

def main():
    #This code enables the user to choose (a) subject(s) and stud(y/ies), with the end goal of both copying and renaming
    #the frame image files that are of interest for that/those subject(s) and stud(y/ies), and, filling out a master excel
    #file that contains the identified frames by each reviewer, for each stimulus. The excel file serves the dual
    #purpose of both a record keeping document (for when reviewers' frame #s agree), and to show discrepancies when
    #they do not.  This excel file helps reviewers to fix discrepencies.  When all reviewers chosen frames agree (textgrids
    #must be altered to reflect changes), this code can move the images corresponding to the subj/study/ies into a directory
    #named 'targetFrames' in the directory for each given subject.  These images are ready for tracing; once traced, use the
    #"moveTraces.py" code to continue the analysis process.
    ###################
    #Initializes code; calls get_Subjs(),get_Studs(),get_Words(),runGetFramesPraat(),getFrameNumbers(),findSameIteration()
    #First, the user is asked for input as to what subject(s) and study(ies) they wish to look at (getSubj/getWord).  Based
    #on the study(ies), the stimuli for that study are identified in the Words_Sounds.txt file.  Then, a list of all the
    #folders for all the subjects we're interested in is compiled, and the code iterates through this list.  All 
    #the code outside of main() is per-subject. The framelog files (if any) are identified within the subj dir, and
    #then the user is asked if they would like to recalculate (or create) the framlog files (runGetFramesPraat()).  The 
    #framelog files are then read (getFrameNumbers()), and the relevant information extracted.  Then any labeling errors
    #are identified, and between-reviewer frame number mismatches are found (findSameIteration()). The excel file is filled,
    #and if there are no errors, all frames are moved (findSameIteration()).  If any errors were present, a notice will
    #be printed at the end of the code describing future actions.
    path = os.getcwd()
    print 'Identifying subjects, studies, and words...\n'
    subjects = get_Subjs()  #will be list of all subjects desired
    studies = get_Studs()    #will be list of all studies desired
    words_Sounds = get_Words(studies) #will be dict of (word,sound) keys and [study] values  
    folders = [i for i in os.listdir(path) if os.path.isdir(os.path.join(path, i))] #identify all folders in cwd
    MerrorReport = defaultdict(set)       #dicts to store information presented at end, notifying of actions to take
    IerrorReport = {}
    for folder in folders:
        cwd = path+'/'+folder
        subject = folder[:3]
        if subject in subjects: #open the folder if it corresponds to a desired subject
            textGrids = [i for i in os.listdir(cwd) if '.TextGrid' in i and 'NEW' not in i and 'TextGrid.' not in i and 'tims.' not in i]
            print textGrids
            ###############################################################################
            while True:
                # remake = raw_input('Would you like to rerun framelog files? [y/n]: ')
                ##############uncomment below, comment above, to hard-set answer##########
                remake = 'y'
                if remake == 'y':
                    redo = defaultdict(list)
                    for grid in textGrids:
                        redo[subject].append(grid[:grid.rfind('.')])
                    print redo
                    runGetFramesPraat(subject, folder, redo, path)
                    break
                if remake == 'n':
                    break
                else:
                    print 'You must enter a valid input: y/n'

            ################################################################################
            print '\nCURRENT SUBJECT: {0} \nIDENTIFYING FRAME NUMBERS...\n'.format(subject)
            #framelog files must be only txt files in the cwd
            frameLogs = [i for i in os.listdir(cwd) if 'framelog.txt' in i]  #finds all .txt's
            #make a dict of framelog->word->sound->targetframe (needs to be updtated for frame sequence)
            all_Logs, noMatches = getFrameNumbers(frameLogs, words_Sounds,cwd)
            #compares the frames ID'd from different reviewers to find discrepencies; if no discrepancies are found, it asks to move the files
            print 'CHECKING FOR ERRORS...\n'
            IerrorReport, MerrorReport = findSameIteration(all_Logs, IerrorReport, MerrorReport, cwd, noMatches)

    #The below code simply reports the errors (frame # discrepencies, and textgrid labeling errors) discovered in the code above
    if len(MerrorReport) > 0:
        print '\n**************************FRAME MISMATCH ERROR REPORT*****************************\n'
        # j = 1
        for comparison in MerrorReport:
            j = 0
            for discrepancy in MerrorReport[comparison]:
                print discrepancy
                j += 1
            print 'There are {0} frame boundaries to fix in {1} in order to move frames: '.format(j,comparison), '\n*******************************'
    if len(IerrorReport) > 0:
        print '\n**************************TEXTGRID LABELING ERROR REPORT*****************************\n'
        print 'The following framelog files and TextGrids have been rewritten and need to be rerun (or fixed manually):\n**************************'
        for subject in IerrorReport:
            for folder in folders:
                if subject in folder:
                    runGetFramesPraat(subject, folder, IerrorReport, path)
            print 'For subject {0}, {1} textgrids and framelogs were rewritten.\n'.format(subject, IerrorReport[subject])

fricatives = ['s', 'dh', 'ch', 'th', 'bh', 'mh', 'sh', 'ph', 'f', 'fh', 'gh']   #used to identify sounds in Scottish Gaelic study - uses orthography for simplicity
sonorants = ['r', 'l', 'n', 'm', 'nn', 'll']                                #thought "global vars" here may be useful

main()
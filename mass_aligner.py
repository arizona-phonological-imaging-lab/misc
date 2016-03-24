# # # # # # # # # # # # # # # # # # #
#  Mass P2FA Aligner and Organizer  #
# # # # # # # # # # # # # # # # # # #

'''
 _________
|         |  
| READ ME |
|_________|

Description:

This code is intended for aligning many files at once using the P2FA, and then putting them into folders based on file name if the user so desires.
-----------------------------------------------------------------------------------

Instrutions: 

This script requires that pyton 2, the P2FA aligner, and HTK installed on your PC. 

To run this script, change your working directory to the folder containing this script, then type in "python2 mass_aligner.py <directory_of_files_to_align> <directory_of_aligner>" and press enter

You will be prompted on whether or not you would like to place the files into separate folders, simply type "Y," "y," "N," or "n" to proceed.
-----------------------------------------------------------------------------------

Some important notes:

	This script works based on the file names of the respecting .wav, .text, (and eventually) .TextGrid files. This means that, for each set of files the aligner runs, these files must all have the same name. 
	For example, Jerry01Gaelic.wav, Jerry01Gaelic.txt, and Jerry01Gaelic.TextGrid would be the same sample, but Jerry01Gaelic.wav and Sam03Gaelic.txt would not. 
	
	And, if the user decides to run the "put into folders" option, it will move the files with the same name into a folder titled that name. So, Jerry01Gaelic.wav, Jerry01Gaelic.txt, and Jerry01Gaelic.TextGrid would be placed into a folder entitled "Jerry01Gaelic."


'''

import sys, subprocess, os, re

filenames = []
d_dir={}
d_file={}



def run_p2fa(file_dir, prog_dir):

	### Run P2FA on multiple files ###	
	
	os.chdir(file_dir)
	for filename in os.listdir(os.getcwd()):
		filenames.append(filename)
	
	for fn1 in filenames:
		if not fn.endswith("wav"):
			continue
		minus_ext = re.sub('\..*','',fn1)
		for fn2 in filenames:
			minus_ext2 = re.sub('\..*','',fn2)
			if minus_ext == minus_ext2 and fn1 != fn2:
				fn3 = str(minus_ext)+'.TextGrid'
				d_file[(minus_ext)] = ([fn1, fn2, fn3])
				print(d_file)
				fxn1 = str(file_dir)+'/'+str(fn1)
				fxn2 = str(file_dir)+'/'+str(fn2) 
				fxn3 = str(file_dir)+'/'+str(fn3)
				d_dir[(minus_ext)] = ([fxn1, fxn2, fxn3])
				subprocess.Popen((['python', prog_dir] + d_dir[(minus_ext)]))	
	
	
	### Put files into folders (optional) ###
	
	while True:
		folders = raw_input('Do you want to put these files in folders? (Y/N) ')
		if folders != 'y' and folders != 'n' and folders != 'Y' and folders != 'N':
			print('Please type Y or N. ')
			
		elif folders == 'Y' or folders == 'y':
			for key in d_dir:
				folder_dir = str(file_dir)+'/'+str(key)
				os.mkdir(folder_dir)	
				for f in d_file[(key)]:
					print(str(file_dir)+'/'+str(f))
					print(str(folder_dir)+'/'+str(f))
					os.rename(str(file_dir)+'/'+str(f), str(folder_dir)+'/'+str(f)) 	
			return '\nDone!'
		else: 
			return '\nDone!'


def main():
	print(run_p2fa(sys.argv[1], sys.argv[2]))



if __name__ == '__main__':
	main()
	

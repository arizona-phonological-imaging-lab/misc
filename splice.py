# # # # # # # # #
# TEXT SWAPPER  #
# # # # # # # # #

'''
Description:
This code is intended for swapping the tiers of TextGrid files so that "word" and "phone" may appear in the reverse order. 
-----------------------------------------------------------------------------------

Instrutions: 

To run this script, you must have python 3 installed.

The script will rearrange according to this formula:
	beginning, middle, end --> beginning, end, middle

Run the following in the command line/terminal:
	python splice.py original_file_name new_file_name term1 term2

The original file name should be the TextGrid file (or text file) you are editing, and then a new file to create must be specified (the script can create one, so simply input the name you would like to make the file under. Term 1 and term 2 are the start and stopping points for the splicing. The beginning will have from the beginning, up to, but not including the term 1; the middle will have term 1 up tp, but not including term 2; and the end will have term 2 to the end.

The script will rearrange according to this formula:
	beginning, middle, end --> beginning, end, middle
'''

import sys

def splice(original_text, new_text, term1, term2):
	
	original = open(original_text, 'r').read() 
	new = open(new_text, 'a') 
	index1 = original.find(term1)   
	index2 = original.rfind(term2)
	beg = original[:index1]   #splices from start to index 1
	mid = original[index1:index2]   #splices from index1 to index 2
	end = original[index2:]   #splices from index 2 to end
	new.write(beg)   #writes new file with the new order
	new.write(end)
	new.write(mid)
	new.close()
	


def main():
	splice(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
	
if __name__ == "__main__":
	main()

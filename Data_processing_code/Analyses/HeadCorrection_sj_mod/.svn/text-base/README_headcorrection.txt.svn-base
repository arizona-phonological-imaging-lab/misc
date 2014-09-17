To perform head correction on a set of ultrasound images, do the following:

1.  Open Matlab
2.  Make sure the folder containing all the scripts are Matlab's current folder.
    You may need to change Matlab's current folder by navigating to it in the 
    "Current Folder" window on the left.  Once you have navigated to the
    folder, make sure that it is on Matlab's path by right clicking on the folder
    from within the "Current Folder" window.  Select "Add to Path".
3.  In the Matlab Command Window, type in head_correction('path to the input folder').
4.  The input folder must be set up in the manner described in the head_correction 
    pre-code documentation:
    
% This program takes in a folder containing two neutral tongue contour
% files.  One must have 'C1_' (Contour 1) somewhere in its names.  The other 
% must have 'C2_' somewhere in its name.  The program will find the optimal
% transformation of C2 such that it aligns as well as it can to C1.  Then, it 
% will apply that transformation to C2 and to any other tongue contours in
% the folder.  It will create new .jpg.traced.txt files for the aligned
% contours.  It is important to note that the neutral contour labeled 
% as C2 should be the one which corresponds to all the other files to be
% aligned.  In other words, if you put two vowel contours from repetition 2
% of a word into the input folder, the neutral contour labeled C2 should be 
% the one taken from repetition 2.  If you don't do this, they will be 
% incorrectly transformed.

5.  The script will produce head-corrected .traced.txt files and a file describing
    the transformation (x shift, y shift, and rotation in radians) performed on the 
    original files in order to perform head correcting.  
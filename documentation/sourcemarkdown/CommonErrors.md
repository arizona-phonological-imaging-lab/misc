Common Errors
=====
> This document contains solutions to problems that show up often when using Autotrace.
> Will be updated as new errors are reported or discovered.

Matlab ceases and returns errors during __Train Network__. 
--

+ Double check that ROIConfig.txt is in the __train/Subject1/__ directory. 
+ Try again, making sure to select both __train/Subject1/__ and __train/traces__. 
+ If your images are in .png format, convert them using the included __png_to_jpg.py__ script, by typing `python png_to_jpg.py /path/to/png/images`. 

"moved 0 images and 0 traces" after running __Configdir__
--
+ Most likely you gave the incorrect folder as an argument. You *must* select the /train directory made by Image Diversity. To do so, navigate *into* that directory, and then click __OK__.

Final traced images wildly inaccurate.
---

+ Look through your training data one image at a time (you can use the Autotrace tool for this), and look for bad traces, and bad images. 
+ Clear the folders and restart the process from the beginning, but this time, during the __Image Diversity__ step, use a larger training set, and include more images from the "least diverse" category in the training set.  

Train Network Error Messages
---
> show =
>
>     0
>
>
>filename =
>
>/home/trevor/AutotraceTest/WorkDirectory/train/Subject1/Subject1/TongueContours.csv
>
>Error using fgets
>Invalid file identifier. Use fopen to generate a valid file identifier.
>
>Error in fgetl (line 33)
>[tline,lt] = fgets(fid);
>
>Error in loadContours (line 13)
>    header = fgetl(fid);
>
>Error in TrainNetwork (line 48)
>  [contfiles, subjectid, contx, conty] = loadContours(data_dir, subject_nums);

+ You did not pass the correct directories to Train Network. Click on the _Data_ menu, then _Select Training Data_ You must either click on the Subject1 directory, and click __Open__, or in the Subject1 directory, select all files and click __Open__

>reply: '221 2.0.0 closing connection ff6sm8363539pdb.80 - gsmtp\r\n'
>reply: retcode (221); Msg: 2.0.0 closing connection ff6sm8363539pdb.80 - gsmtp

+ This is not an error. The program ran successfully! You should have recieved an email indicating that the training process has finished. Use the `Ctrl-C` key combination to exit. 

>Saving trained network...done

+ This is also not an error. If you did not input your email for a notification, this message indicates that Train Network has finished running. 

ROI_config.txt not found
---

 + If this error occurs while running TrainNetwork, then ROI_config.txt was not in one of the folders that you selected for training. 

 + If this occurs during AutoTracing, then ROI_config.txt is not in the directory containing your test images. 

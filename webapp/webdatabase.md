---
layout: default
title: Database
categories: webapp
updated: May 2016
---

To access the database, go to the APIL database website, or the website associated with your lab's database, or if running locally, go to http://localhost:8000/uat/1

##Menu Items

###Download
Use these tools to get the images that you have selected with the buffer, downloaded over the network to your machine. 

###Tags
Use these tools to manipulate the tags and experiment labels on images you have selected. 

###View
No tools present as of this writing. 

###Data
>Note, these tools are only available when accessing the server locally

Add and remove projects and images from the database. To add images in the APIL format (see below) click `add standard project`. Enter the name of the project and the language of the data, and enter the direct path to the directory containing the video folders. 

>APIL Data Format:
>One folder contains all of the data for a particular experiment. Each video has a subdirectory in this folder. The video folders are labeled `subjectnum[abc]_YYYY-MM-DD`. Inside each video folder is a folder labeled `frames` which contains each frame of the video, and the accompanying trace, if one exists. The frame images are in the form `frame-#######.png`, and the traces are in the form `frame-#######.png.TRACER.traced.txt`.

##Search Box

![Search Box]({{site.baseurl}}/images/searchbar.jpg)

These search fields can be used to query the database for images and traces that match a particular request. Most of the options should be straightforward. 

##Tools:

(Default) Pen: Draw on the central window to make your trace. 

ROI: Maximize will set the Region of interest to the whole window. Constrain turns your pointer into a marker so that you can draw a box over the region of the image containing the toungue in image. 

Eraser: Turns your cursor into a marker so that you can draw a box over a region that you want to delete. 

Clear: Undoes all marks

Back & Next: Cycles the images left and right. 

Load: Opens a file dialog to open a series of images or traces to be edited from the local computer. 

##Metadata Fields

tracer: Your name. The APIL standard is three uppercase letters. 

subject: The indicator/number/whatever you use to differentiate subjects (APIL uses two digits)

project: The name of the project or experiment. 
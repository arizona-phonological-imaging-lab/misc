APIL TODO
===
A list of things the lab needs to accomplish.

##AutoTrace
1. Rewrite `pygtk`-dependendent scripts with a different GUI toolkit (`Qt` or `Tkinter`)
     - [`image_diversityNEW.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/image_diversityNEW.py)
     - [`CompareContours.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/CompareContours.py)
     - [`LinguaView.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/LinguaView.py)
     - [`AutoTrace.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/AutoTrace.py)
     - [`FileRename.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/FileRename.py)
     - [`TrackDots.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/TrackDots.py) (This is the one with the disappearing points bug)
     - [`fixImages.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/fixImages.py)
     - [`selectROI.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/SelectROI.py)
     -  ~~[`configdir.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/configdir.py)~~(*Completed by Gus using* `Tkinter`)
     - ~~`TrainNetwork.py`~~(*Jeff's new version is* [`TrainNetworkQt.py`](https://github.com/jjberry/Autotrace/blob/master/under-development/TrainNetworkQt.py))
2.  Fix disappearing points bug
     - (see [`TrackDots.py`](https://github.com/jjberry/Autotrace/blob/master/matlab-version/TrackDots.py))
3.  Complete Wonky Trace Finder(/Correcter/etc)
     - *Assigned to Gus*
4.  EdgeTrak to AutoTrace trace converter
     -  *Needs GUI*
5.  Write `trace_diversity.py`  
     - *Assigned to Gus*
6.  Adapt `image_diversity.py` to select diverse set *automagically* using diversity scores distribution  
7.  Complete `matlab`-free version

##Analysis  
1.  ~~"Traces from Hell" bug~~ (*Switched to SSANOVA code from Lisa Davidson*)

##Head tracking (Sam)
1. Determine whether or not Kinect can be used for head tracking
2. Determine whether image correction (and what kind) can be performed with the current data

##Documentation (Trevor)  
1. Revise existing documentation
2. Complete documentation for Database tool
3. Complete [documentation for AutoTrace](https://github.com/jjberry/Autotrace/tree/master/documentation)
4. Develop `Github` wiki for project

##uA Tracker (Mohsen)
1. Interact with Praat
2.  Interact with AutoTrace

##UltraPraat
1.  Include java&harr;Praat add-on

##Data Collection  
1. Recollect Harvard Sentences data
2.  Fully annotate newly collected Harvard Sentences data
3.  Distribute dataset

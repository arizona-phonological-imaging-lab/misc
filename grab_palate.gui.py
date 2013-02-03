#!/usr/bin/python


#######################################
# Simple audio/video capture demo
# created by Gustave Hahn-Powell for 
# the Apil Ultraspeech project at
# the University of Arizona
#
# You can do better!
#
# Contact me at gushahnpowell@gmail.com
#
######################################

import sys, cv, pyaudio, wave, time
from PySide.QtCore import *
from PySide.QtGui import *

import threading
from threading import Thread

Finished = False

def CapturePalate():
	"""
	grabs a frame using opencv.  Should be 
	used in conjunction with the "Show video stream" button
	in order to better monitor when to capture the image
	"""
	#grab a frame
	cv.NamedWindow("badass window", cv.CV_WINDOW_AUTOSIZE)
	camera_index = 0
	capture = cv.CaptureFromCAM(camera_index)
	frame = cv.QueryFrame(capture)
	cv.SaveImage("palate.jpg", frame)
	print  ("Palate image captured!")
 
def AudioStream():
	"""
	audio capture is peformed via a thread 
	created by the VideoStream function
	"""
	global Finished
	CHUNK = 1024	#No. of input samples used for a single FFT
	FORMAT = pyaudio.paInt16 #for Mac, checked in Audio-Midi-Setup
	CHANNELS = 2
	RATE = 44100
#	RECORD_SECONDS = 800
	WAVE_OUTPUT_FILENAME = "Stream.wav"
	
	print "Recording audio, TIME: ", time.time()
#	time.sleep(5.2)#magic number on my macbook
	p = pyaudio.PyAudio()
	stream = p.open(format=FORMAT, 
					channels=CHANNELS, 
					rate=RATE, 
					input=True, 
					frames_per_buffer=CHUNK)

	audio_frames = []
	while not Finished:
		try:
			data = stream.read(CHUNK)
		except IOError:
			print 'warning: dropped frame'
		audio_frames.append(data)
	stream.stop_stream()
	stream.close()
	p.terminate()
	
	wf = wave.open(WAVE_OUTPUT_FILENAME, 'wb')
	wf.setnchannels(CHANNELS)
	wf.setsampwidth(p.get_sample_size(FORMAT))
	wf.setframerate(RATE)
	wf.writeframes(b''.join(audio_frames))
	wf.close()
	print "Finished recording audio at: ", time.time()
	Finished = False
	return

def VideoStream():
	"""
	VideoStream should probably be updated to 
	utilized the cv2 python wrapper for opencv
	If you're having problems, remember to check the camera index, fps
	"""
	global Finished
	#display live video
	cv.NamedWindow("Badass video window", cv.CV_WINDOW_AUTOSIZE)
	#peripheral devices begin at > 0
	camera_index = 0 #can set to -1 if only one device
	capture = cv.CaptureFromCAM(camera_index)
	frame = cv.QueryFrame(capture)
	writer = cv.CreateVideoWriter("Stream.avi", 0, 20, cv.GetSize(frame), 1) #"filename", codec,fps, frame_size, is_color=true
	#isight can't handle 30 fps so changed it to 15
	print "Calling thread at: ", time.time()
	Thread(target = AudioStream).start()
	i = 1
	while True:
		print "Recording Video Frame: ",i," At: ", time.time()
		frame = cv.QueryFrame(capture)
		cv.WriteFrame(writer, frame)
		cv.ShowImage("Badass video window", frame)
		k = cv.WaitKey(10) #milliseconds
		i+=1
		if k == 0x1b: #ESC
			print 'ESC pressed. Exiting ... Time is: ', time.time()
			break
	Finished = True
	cv.DestroyWindow("Baddass video window")
	sys.exit(1)
	return

 
#---------------Qt stuff---------------------#

# Create the Qt Application
app = QApplication(sys.argv)
# Create a button, connect it and show it
button = QPushButton("Capture Palate")
button.clicked.connect(CapturePalate)
button2 = QPushButton("Show Video Stream")
button2.clicked.connect(VideoStream)
#button.show()
#button2.show()
buttons = [button,button2]
widget = QWidget()
layout = QVBoxLayout()
map(layout.addWidget, buttons) #necessary to bind buttons to the same window
widget.setLayout(layout)
widget.show()
# Run the main Qt loop
app.exec_()    

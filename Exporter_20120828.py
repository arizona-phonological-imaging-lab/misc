# Last Modified by Zac Chapman 20120827
# Exports audio and image frames from ROSBag input

#!/usr/bin/env python
import roslib
roslib.load_manifest('ultraspeech')
import rospy
import rosbag
import optparse
import os
import glob
import scikits.audiolab as audio
from numpy import array, reshape, array_split

#############Imports_From_timecodes2images.py#######################
import errno
import cv
import re

#=============================================================================================
# Function:     write_clip
#
# Parameters    stimulus:       Stimulus word (string; comes from the ROSBag)
#               stimtime:       Duration of each production of the stimulus (comes from ROSBag)
#               dirname:        Directory where the audio files will be written
#               num_channels:   Number of audio channels recorded into the ROSBag (4 channels
#                               as of 20120621)
#               sample_rate:    Sample rate of the audio recordings (96KHz, as recorded by the
#                               Saffire audio interface)
#               data:           List of Raw audio chunks from the ROSBag. A list of tuples of
#                               floats of length 16384 bytes)
#
# The write_clip function takes raw audio data collected from the ROSBag, puts it into an
# array, separates the array into 4 subarrays (one for each audio channel), and then writes
# the audio data in a .AU container (with PCM16 Encoding) in the specified directory.
#
# Last modified: Zac Chapman, 20120627
#=============================================================================================


####################Functions_From_timecodes2images.py########################################
def getDVframes(dvfile, output_dir, frame_start, frame_end):
    """
    Snip out the selected frames from the DV file
    """
    capture = cv.CaptureFromFile(dvfile)
    print "Dimensions: ", cv.GetCaptureProperty(capture, cv.CV_CAP_PROP_FRAME_WIDTH), \
    "x", cv.GetCaptureProperty(capture, cv.CV_CAP_PROP_FRAME_HEIGHT)
    numFrames = cv.GetCaptureProperty(capture, cv.CV_CAP_PROP_FRAME_COUNT)
    print "Num frames: ", numFrames

    for i in range(frame_start, frame_end):
        print "Exporting frame", i
        cv.SetCaptureProperty(capture, cv.CV_CAP_PROP_POS_FRAMES, i)
        img = cv.RetrieveFrame(capture)
        cv.SaveImage(os.path.join(output_dir, 'frame' + str(i)) + '.png', img)
        #cv.ShowImage("Frame " + str(i), img)
        #cv.WaitKey()

def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError as exc: # Python >2.5
        if exc.errno == errno.EEXIST:
            pass
        else: raise


####################Functions_From_bag2sound.py###########################################

def write_clip(stimulus, stimtime, output_dir, num_channels, sample_rate, data):
    # Set format for Audiolab file export AU or WAV 16bit (encoding originally 'float32')
    format1 = audio.Format(type='au', encoding='pcm16')
    format2 = audio.Format(type='wav', encoding='pcm16')

    # Single File output -- Needs tweaking to work right
    if stimulus == 'sound':
        nameSingle = output_dir + '/' + str(stimtime.secs) + '.' + str(stimtime.nsecs) + '_' + stimulus + '_All.wav'
        single_file = audio.Sndfile(nameSingle, 'w', format2, num_channels, sample_rate)
    
    file_name = output_dir + '/' + str(stimtime.secs) + '.' + str(stimtime.nsecs) + '_' + stimulus + '_Channel-1.wav'
    sound_file = audio.Sndfile(file_name, 'w', format2, 1, sample_rate)
    
    # Create list of sound file entities with unique channel names
    # Each channel output separately
##    channel_files = []
##    for channel in range(0,num_channels):
##        name = output_dir + '/' + str(stimtime.secs) + '.' + str(stimtime.nsecs) + '_' + stimulus + '_Channel-%d.wav' % (channel+1)
##        channel_files.append(audio.Sndfile(name, 'w', format2, 1, sample_rate))

    # Manipulate the data to each individual sound file
    for line in data:
        soundarray = array(line) # shape: (16384,)
        soundarray = reshape(soundarray, (len(soundarray)/num_channels, num_channels)) # shape: (4096, 4)
        #single_file.write_frames(soundarray)
        soundarray = array_split(soundarray, num_channels, axis=1) # splits into list with 4 arrays of shape (4096, 1)
        sound_file.write_frames(soundarray[0])
##        for channel in range(0,num_channels):
##            channel_files[channel].write_frames(soundarray[channel])

    # Finish each of the files
    sound_file.sync()
##    for soundfile in channel_files:
##        soundfile.sync()
##    single_file.sync()
    
def exportBag(opts):
    bag = rosbag.Bag(opts.input_dir)

    shortpath = os.path.basename(opts.input_dir)
    dirname = os.path.dirname(opts.input_dir)
    print "dirname,  shortpath: \t", dirname, shortpath

#    framelist = []
    start_frame = 0
    end_frame = None

# start from bag2sound.py    
    audio_buf = []
    time_before = 0
    sample_rate = None
    last_stim = None
    last_stim_time = None
    current_stim = None
    current_stim_time = None
    audio_chunks_since_last_stim = 0
    for topic, msg, t in bag.read_messages(topics=['/audio_capture/audio', '/current_stimulus', '/ros_dvgrab/framenum', '/control']):
        if topic == '/audio_capture/audio':
            num_channels = msg.num_channels
            sample_rate = msg.sample_rate
            audio_buf.append(msg.samples)
            audio_chunks_since_last_stim += 1
            if not opts.singlefile:
                if (audio_chunks_since_last_stim == opts.time_after) and (last_stim is not None):
                    write_clip(last_stim, last_stim_time, opts.output_dir, num_channels, sample_rate, audio_buf)
                    audio_buf = audio_buf[-(audio_chunks_since_last_stim+time_before):]
        if topic == '/current_stimulus':
            if current_stim_time != None:
                clipdir = os.path.join(opts.output_dir, str(start_time.secs) + str(start_time.nsecs) + '_' + current_stim)
                mkdir_p(clipdir)
                getDVframes(dvfile, clipdir, start_frame, end_frame) # do dv frame export with stim start and stop time

            audio_chunks_since_last_stim = 0
            last_stim = current_stim
            last_stim_time = current_stim_time
            current_stim = msg.stimulus
            current_stim_time = t
            start_time = t
            print "Current stimulus: \t\t", current_stim
            print "Current stim Time start: \t", last_stim_time
            print "Current stim Time stop: \t", current_stim_time
            flag = 1
        if topic == '/control':
            ultrasound_filename = msg.ultrasound_filename
            ultrasound_filename = os.path.basename(ultrasound_filename)
            root, ext = os.path.splitext(ultrasound_filename)
            dvfiles = glob.glob( os.path.join(dirname, root + '*.dv') )
            dvfile = dvfiles[0]
            print "Current dvfile: \t", dvfile
        if topic == '/ros_dvgrab/framenum':
            end_frame = msg.data # set end frame of dv
            if flag == 1:
                start_frame = end_frame # set new start frame
                flag = 0

    if current_stim is not None:
        if opts.singlefile:
            write_clip('sound', '', opts.output_dir, num_channels, sample_rate, audio_buf)
        else:
            write_clip(current_stim, current_stim_time, opts.output_dir, num_channels, sample_rate, audio_buf)

    bag.close()


if __name__ == "__main__":
    usage = "usage: %prog [options] clipfile"
    parser = optparse.OptionParser(usage=usage)
    parser.add_option('-i', '--input-directory', help='Directory containing bagfiles to have snippets extracted', dest='input_dir', default = '.')
    parser.add_option('-o', '--output-directory', help='Directory where new Ultrasound snippets will be saved', dest='output_dir', default = '.')
#    parser.add_option('-b', '--bag-directory', help='directory containing bagfiles to have sound extracted', dest='bagname')
#    parser.add_option('-d', '--out-directory', help='path where new soundfiles will go', dest='dirname', default = '.')
    parser.add_option('-a', '--after', help='amount of audio after click to include', dest='time_after', type='int', default = 8)
    parser.add_option('-s', '--single-file', help='Don''t split sound into clips', dest='singlefile', default=False, action='store_true')
    (opts, args) = parser.parse_args()

    if opts.time_after < 1:
        opts.time_after = 1
    if opts.singlefile:
        print "Storing as a single file."
    if opts.input_dir is None:
        print "input-directory option must be input on command line"
        parser.print_help()
        exit()

    bags = glob.glob( os.path.join( opts.input_dir, '*.bag') )
    if len(bags) is 0:
        print "No bagfiles found"
        exit()
        
    for infile in bags:
        print "current file is: " + infile
        opts.input_dir = infile
        exportBag(opts)

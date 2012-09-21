import roslib
roslib.load_manifest('ultraspeech')
import rospy
import rosbag

import subprocess, os, sys
import numpy as np
import scipy.io.wavfile as wavfile

def print_log(bagfile):
    bag = rosbag.Bag(bagfile)
    o = open('logfile.csv', 'w')
    o.write('topic,secs,nsecs,extra\n')
    for topic, msg, t in bag.read_messages():
        if topic == '/ros_dvgrab/framenum':
            o.write("us,%d,%d,%d\n" %(t.secs, t.nsecs, msg.data))
        elif topic == '/audio_capture/audio':
            o.write("au,%d,%d,,\n" %(t.secs, t.nsecs))
        elif topic == '/current_stimulus':
            o.write("st,%d,%d,%s\n" %(t.secs, t.nsecs, msg.stimulus))
    o.close()
    
def extract_images(dvfile):
    if not os.path.isdir('images'):
        os.mkdir('images')
    cmd = ['ffmpeg','-i',dvfile,'-sameq','images/image-%6d.png']
    p = subprocess.Popen(cmd)
    p.wait()
    
    
def get_sound(bagfile):
    bag = rosbag.Bag(bagfile)
    audio = []
    for topic, msg, t in bag.read_messages():
        if topic == '/audio_capture/audio':
            nchan = msg.num_channels
            sr = msg.sample_rate
            audio.extend(msg.samples)
    nsamples = len(audio)/nchan

    #convert the samples from float to int16
    samples = ((np.asarray(audio)*(2**16/2)).astype('int16')).reshape((nsamples,nchan))
    wavfile.write('bagsound.wav',sr,samples)
    
    #downsample and combine channels
    cmd = ['sox','bagsound.wav','-c1','-r','16000','bagsound_16k_mono.wav']
    p = subprocess.Popen(cmd)
    p.wait()
    
def synchronize(bagfile, dvfile):
    print_log(bagfile)
    get_sound(bagfile)
    extract_images(dvfile)
    f = open('logfile.csv','r').readlines()
    
    #use the first logfile entry as time zero
    chunks = f[1][:-1].split(',')
    secs = int(chunks[1])
    n = len(chunks[2])
    nsecs = int(chunks[2]) * 10**-n
    zerotime = secs+nsecs

    #open the downsampled wavfile
    sr, samples = wavfile.read('bagsound_16k_mono.wav')
    
    #find the first ultrasound entry in the logfile
    i = 0
    while f[i][0:2] != 'us':
        i += 1
    chunks = f[i][:-1].split(',')
    secs = int(chunks[1])
    n = len(chunks[2])
    nsecs = int(chunks[2]) * 10**-n
    ultrasound_start = secs+nsecs - zerotime
    
    #find the starting point for the audio 
    start_sample = int(sr * ultrasound_start)
    
    #find the ending point for the audio 
    ultrasound_sr = 29.97
    jpgs = os.listdir('images')
    n_frames = len(jpgs)
    total_time = n_frames * (1./ultrasound_sr)
    n_samples = int(total_time * sr)
    end_sample = start_sample + n_samples
    
    #write the audio 
    audio = np.zeros((n_samples,), dtype='int16')
    audio = samples[start_sample:end_sample]
    wavfile.write('synchronized.wav',sr,audio)
    
    #put it all together with ffmpeg
    cmd = ['ffmpeg','-f','image2','-r','29.97','-sameq','-i','images/image-%6d.png','-i','synchronized.wav','synchronized.mp4']
    p = subprocess.Popen(cmd)
    p.wait()
    
if __name__ == "__main__":
    #first arg is bag file, second is dv file
    synchronize(sys.argv[1], sys.argv[2])

    
import roslib
roslib.load_manifest('ultraspeech')
import rospy
import rosbag
import cv
from cv_bridge import CvBridge
from sensor_msgs.msg import Image
import subprocess, os, sys
import numpy as np
import scipy.io.wavfile as wavfile

def save_images(bagfile):
    if not os.path.isdir("video_images"):
        os.mkdir("video_images")
    if not os.path.isdir("depth_images"):
        os.mkdir("depth_images")
    bag = rosbag.Bag(bagfile)
    bridge = CvBridge()
    count = 0
    o = open('image_log.csv','w')
    o.write('im,secs,nsecs,extra\n')
    
    for topic, msg, t in bag.read_messages():        
        if topic == '/camera/rgb/image_rect_color':
            img = bridge.imgmsg_to_cv(msg, "bgr8")
            img_name = "video_images/img_%06d.png" %count
            o.write('im,%d,%d,\n' % (t.secs,t.nsecs))
            count += 1
            cv.SaveImage(img_name, img)

        elif topic == '/camera/depth_registered/image_rect':
            img = bridge.imgmsg_to_cv(msg, "bgr8")
            img_name = "depth_images/img_%06d.png" %count
            #o.write('de,%d,%d,\n' % (t.secs,t.nsecs))
            #count += 1
            cv.SaveImage(img_name, img)

def synchronize(bagfile):
    save_images(bagfile)
    l = open('image_log.csv','r').readlines()
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
    chunks = l[1][:-1].split(',')
    secs = int(chunks[1])
    n = len(chunks[2])
    nsecs = int(chunks[2]) * 10**-n
    video_start = secs+nsecs - zerotime
    
    #find the starting point for the audio 
    start_sample = int(sr * video_start)
    
    #find the ending point for the audio 
    video_sr = 30 #this is a guess but seems to be correct (cf. http://en.wikipedia.org/wiki/Kinect)
    jpgs = os.listdir('video_images')
    n_frames = len(jpgs)
    total_time = n_frames * (1./video_sr)
    n_samples = int(total_time * sr)
    end_sample = start_sample + n_samples
    
    #write the audio 
    audio = np.zeros((n_samples,), dtype='int16')
    audio = samples[start_sample:end_sample]
    wavfile.write('video_synchronized.wav',sr,audio)
    
    #put it all together with ffmpeg
    cmd = ['ffmpeg','-f','image2','-r',str(video_sr),'-sameq','-i','video_images/img_%6d.png','-i', 'video_synchronized.wav','video_synchronized.mp4']
    p = subprocess.Popen(cmd)
    p.wait()
            
            
if __name__ == "__main__":
    synchronize(sys.argv[1])
        

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace apil_sound_kinect_matching_b01
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void intermeshVidKinect( string vidFileName, string kinectFileName, string outputFileName, string framesFolder ) {

            string[] vidText = System.IO.File.ReadAllLines(@vidFileName);
            string[] kinectText = System.IO.File.ReadAllLines(@kinectFileName);
            int inputFrames = System.IO.Directory.GetFiles(framesFolder, "frame*.*").Length ;

            string[] temp;

            int vidTextLength = vidText.Length;
            int kinectTextLength = kinectText.Length;

            double[] vidTimes = new double[vidTextLength];
            double[] kinectTimes = new double[kinectTextLength];
            string[] kinectPitch = new string[kinectTextLength];
            string[] kinectYaw = new string[kinectTextLength];
            string[] kinectRoll = new string[kinectTextLength];
            string[] kinectDistanceX = new string[kinectTextLength];
            string[] kinectDistanceY = new string[kinectTextLength];
            string[] kinectDistanceZ = new string[kinectTextLength];

            string output = "";
            output += "Frame Number\tFrameTime\tKinectTime\tPitch\tYaw\tRoll\tTranslation on X\tTranslation on Y\tTranslation on Z\r\n";

            //====================================================================
            // Extract the start and end times of the video from the vidTimes file
            //====================================================================

            for (int i = 0; i < vidText.Length; i++)
            {
                temp = vidText[i].Split(':');
                temp[1] = temp[1].TrimStart();
                vidTimes[i] = Convert.ToDouble(temp[1]);
            }

            //====================================================================
            // Extract the pitch, yaw and roll information from the Kinect file
            //====================================================================

            char[] kinectDividers = new char[] { ':', ' ', '°' };

            for (int i = 0; i < kinectText.Length / 2; i++)
            {

                temp = kinectText[(2 * i)].Split(kinectDividers);
                temp[2] = temp[2].TrimStart();

                kinectTimes[i] = Convert.ToDouble(temp[2]);
                kinectPitch[i] = temp[10];
                kinectYaw[i] = temp[14];
                kinectRoll[i] = temp[18];
                kinectRoll[i] = kinectRoll[i].Substring(0, kinectRoll[i].Length - 1); // Remove final parenthesis

            }

            //========================================================================
            // Extract the distance from zero-point information from the Kinect file
            //========================================================================

            for (int i = 0; i < kinectText.Length / 2; i++)
            {
                temp = kinectText[(2 * i) + 1].Split(kinectDividers);
                kinectDistanceX[i] = temp[10];
                kinectDistanceY[i] = temp[16];
                kinectDistanceZ[i] = temp[22];
            }

            //=============================================================================
            // Determine the total duration of the video and calculate number of frames
            //=============================================================================

            //double framesPerSecond = 30 * 2.9175;

            double videoStartTime = vidTimes[1];
            double videoStopTime = vidTimes[2];
            double totalVideoTime = videoStopTime - videoStartTime;

            //int totalFrames = Convert.ToInt32(totalVideoTime/(framesPerSecond));

            //double[] frameTimes = new double[totalFrames];
            //double[] frames = new double[totalFrames];
            double[] frameTimes = new double[inputFrames];
            double[] frames = new double[inputFrames];

            //Console.WriteLine(totalVideoTime.ToString());
            //Console.WriteLine(totalFrames.ToString());

            //=============================================================================
            // Create a vector with the timestamp for each frame
            //=============================================================================

            double durationOfEachFrame = totalVideoTime / inputFrames;
            Console.WriteLine("totalVideoTime:" + totalVideoTime.ToString() + ", inputFrames:" + inputFrames.ToString() + ", durationOfEachFrame: " + durationOfEachFrame.ToString());

            for (int i = 0; i < inputFrames; i++)
            {
                frameTimes[i] = videoStartTime + Convert.ToDouble(durationOfEachFrame * i);
                frames[i] = i + 1;
            }

            //=============================================================================
            // Search for the Kinect coordinate closest in time to the timestamp of
            // each frame.
            //=============================================================================

            double tempTemporalDistance = 0;
            double bestTemporalDistance = 1000000000;
            int bestTemporalDistanceIndex = 0;

            for (int i = 0; i < frameTimes.Length; i++)
            {
                for (int j = 0; j < kinectTimes.Length; j++)
                {
                    tempTemporalDistance = frameTimes[i] - kinectTimes[j];
                    if (Math.Abs(tempTemporalDistance) < bestTemporalDistance)
                    {
                        bestTemporalDistance = tempTemporalDistance;
                        bestTemporalDistanceIndex = j;
                    }
                }

                output += frames[i] + "\t" + frameTimes[i] + "\t" + kinectTimes[bestTemporalDistanceIndex].ToString() + "\t" + kinectPitch[bestTemporalDistanceIndex] + "\t" + kinectYaw[bestTemporalDistanceIndex] + "\t" + kinectRoll[bestTemporalDistanceIndex] + "\t" + kinectDistanceX[bestTemporalDistanceIndex] + "\t" + kinectDistanceY[bestTemporalDistanceIndex] + "\t" + kinectDistanceZ[bestTemporalDistanceIndex] + "\r\n";
                tempTemporalDistance = 0;
                bestTemporalDistance = 1000000000;
            }


            /*for (int i = 0; i < 2; i++) {

                temp = kinectText[i].Split(kinectDividers);

                for (int j = 0; j < temp.Length; j++) {
                    Console.WriteLine("j: " + j.ToString() + ", " + temp[j]);
                }

            }*/

            //Console.Write(output);

            System.IO.File.WriteAllText(@outputFileName, output);
            MessageBox.Show("File ready\nThe folder had " + inputFrames + " frames.");

        }

        private void btn_processFiles_Click(object sender, EventArgs e) {

            if ( txt_framesFolder.Text == "" ) {
                MessageBox.Show("Please select the folder that contains the frames.");
            }
            else if ( txt_videoFile.Text == "" ) {
                MessageBox.Show("Please select a video info file.\nThe file is usually called vidTimes.txt.");
            }
            else if ( txt_kinectFile.Text == "" ) {
                MessageBox.Show("Please select a Kinect info file.\nThe file is usually called coords.txt.");
            }
            else if (txt_output.Text == "") {
                MessageBox.Show("Please specify a name for the output file.");
            }
            else {
                intermeshVidKinect(txt_videoFile.Text, txt_kinectFile.Text, txt_output.Text, txt_framesFolder.Text);
            }
                        
        }

        private void btn_vidFile_Click(object sender, EventArgs e)
        {

            DialogResult result = openFile_vid.ShowDialog();

            if ( result == DialogResult.OK ) {
                string file = openFile_vid.FileName;
                txt_videoFile.Text = file;
            }


        }

        private void btn_kinectFile_Click(object sender, EventArgs e)
        {
            DialogResult result = openFile_kinect.ShowDialog();

            if (result == DialogResult.OK) {
                string file = openFile_kinect.FileName;
                txt_kinectFile.Text = file;
            }
        }

        private void btn_saveOutput_Click(object sender, EventArgs e)
        {
            

            if ( saveFile_output.ShowDialog() == DialogResult.OK ) {
                txt_output.Text = saveFile_output.FileName;
            }

        }

        private void button1_Click(object sender, EventArgs e)
        {

            DialogResult result = folder_selectFolder.ShowDialog();

            if ( result == DialogResult.OK ) {
                txt_framesFolder.Text = folder_selectFolder.SelectedPath;
            }

        }
    }
}

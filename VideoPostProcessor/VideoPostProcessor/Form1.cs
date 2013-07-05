using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.IO;
using System.Windows.Forms;
using System.Diagnostics;

namespace VideoPostProcessor
{
    
    public partial class Form1 : Form
    {

        private string dtopfolder = System.Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
        private string file;
        private string fullPath;
        private string fileName;
        private string path;
        private string frameFolder;
        public Form1()
        {

            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            button2.Enabled = false;
            label1.Enabled = false;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            openFileDialog1.InitialDirectory = dtopfolder;
            DialogResult result = openFileDialog1.ShowDialog(); // Show the dialog.
            if (result == DialogResult.OK) // Test result.
            {
                file = openFileDialog1.FileName;
                fullPath = openFileDialog1.FileName;
                fileName = openFileDialog1.SafeFileName;
                path = fullPath.Replace(fileName, "");
                // create output folder for frames
                frameFolder = path + "frames";
                if (!(System.IO.Directory.Exists(frameFolder)))
                {
                    System.IO.Directory.CreateDirectory(frameFolder);
                    //MessageBox.Show("created new directory at:\n\n" + frameFolder);
                }
                //MessageBox.Show("File: " + file + "\nFullPath: " + fullPath + "\nFilename: " + fileName + "\nPath: " + path + "\nDesktop Folder: " + dtopfolder);
                button2.Enabled = true;
                label1.Text = fileName;
                label1.Enabled = true;
            }
            
        }

        private void button2_Click(object sender, EventArgs e)
        {

            label1.Text = "Processing Video...";
            // run ffmpeg command line script
            Process process = new System.Diagnostics.Process();
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.WindowStyle = ProcessWindowStyle.Hidden;
            startInfo.FileName = "cmd.exe";
            // ffmpeg command to extract video frames and audio file
            // frames go in /frames folder created when opening the video
            // audio file goes into video file path folder
            startInfo.Arguments = "/C ffmpeg -i " + fullPath + " -r 30000/1001 -q:v 0 -f image2 " + frameFolder + "/frame-%07d.png -acodec pcm_s16le -ac 1 " + path + "audio.wav";
            
            // necessary for debugging in the event that files already exist and ffmpeg asks to overwrite
            //startInfo.UseShellExecute = false;
            //startInfo.RedirectStandardOutput = true;
            //startInfo.RedirectStandardError = true;
            process.StartInfo = startInfo;
            process.Start();
            
            //while (!process.StandardOutput.EndOfStream)
            //{
            //    Console.WriteLine(process.StandardOutput.ReadLine());
            //}
            //string errOutput = process.StandardError.ReadToEnd();

            process.WaitForExit();
            //if (!string.IsNullOrEmpty(errOutput))
            //{
            //    MessageBox.Show(errOutput);
            //}
            label1.Text = "Processing Complete.";
            process.Dispose();
            // some way to show when process is done
            //MessageBox.Show("Post-processing has completed");
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }

    }
}

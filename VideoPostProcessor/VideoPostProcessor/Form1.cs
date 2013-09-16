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
        private string folderName;

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
                folderName = Path.GetFileName(Path.GetDirectoryName(path));

                //MessageBox.Show("File: " + file + "\nFullPath: " + fullPath + "\nFolder: " + folderName + "\nFilename: " + fileName + "\nPath: " + path + "\nDesktop Folder: " + dtopfolder);
                button2.Enabled = true;
                label1.Text = @"\" + folderName + @"\" + fileName;
                label1.Enabled = true;
                label2.Visible = true;
            }
            
        }

        private void button2_Click(object sender, EventArgs e)
        {
            // FFMPEG video post processing starts here

            label2.Text = "Processing Video...";
            Process process = new System.Diagnostics.Process();
            ProcessStartInfo startInfo = new ProcessStartInfo();

            // keeps FFMPEG window hidden unless the menu item is checked
            if (showFFMPEGToolStripMenuItem.Checked == false)
            {
                startInfo.WindowStyle = ProcessWindowStyle.Hidden;

            }

            startInfo.FileName = "ffmpeg.exe";
            
            // start arguments string
            string args = "-i " + fullPath;

            if (jPEGToolStripMenuItem.Checked == true)
            {
                // create output folder for JPG frames
                frameFolder = path + "frames_jpg";
                if (!(System.IO.Directory.Exists(frameFolder)))
                {
                    System.IO.Directory.CreateDirectory(frameFolder);
                    label2.Text = @"Exporting JPG frames to \frames_jpg\ folder.";
                }
                // if folder exists, check for frames
                else if (Directory.GetFiles(frameFolder).Length != 0)
                {
                    label2.Text = @"Error! JPG frames already exist in \frames_jpg\ folder.";
                    return;
                }
                //use FFMPEG JPEG command here
                args += " -r 30000/1001 -q:v 0 -f image2 " + frameFolder + "/" + folderName + "-%07d.jpg";
            }
            else if (pNGToolStripMenuItem.Checked == true)
            {
                // create output folder for PNG frames
                frameFolder = path + "frames_png";
                if (!(System.IO.Directory.Exists(frameFolder)))
                {
                    System.IO.Directory.CreateDirectory(frameFolder);
                    label2.Text = @"Exporting PNG frames to \frames_png\ folder.";
                }
                // if folder exists, check for frames
                else if (Directory.GetFiles(frameFolder).Length != 0)
                {
                    label2.Text = @"Error! PNG frames already exist in \frames_png\ folder.";
                    return;
                }
                // use FFMPEG PNG command
                args += " -r 30000/1001 -q:v 0 -f image2 " + frameFolder + "/" + folderName + "-%07d.png";
            }
            if (wAVToolStripMenuItem.Checked == true)
            {
                // check for audio file
                string audioFile = path + folderName + ".wav";
                if (System.IO.File.Exists(audioFile))
                {
                    label2.Text = "Error! Audio file already exists.";
                    return;
                }

                // use FFMPEG WAV command
                // added '-map_channel 0.1.0 -map_channel -1' on 9/15/2013
                // outputs left audio channel only
                args += " -acodec pcm_s16le -map_channel 0.1.0 -map_channel -1 -ac 1 " + audioFile;
                label2.Text += "\nExporting WAV file to " + @"\" + folderName + @"\ folder.";
            }

            startInfo.Arguments = args;

            process.StartInfo = startInfo;
            process.Start();

            process.WaitForExit();

            // Let user know it is finished
            label2.Text = "Processing Complete!";

            process.Close();
            process.Dispose();
        }

        private void jPEGToolStripMenuItem_Click(object sender, EventArgs e)
        {
            pNGToolStripMenuItem.Checked = false;
        }

        private void pNGToolStripMenuItem_Click(object sender, EventArgs e)
        {
            jPEGToolStripMenuItem.Checked = false;
        }
    }
}

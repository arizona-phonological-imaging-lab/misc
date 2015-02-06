// ------------------------------------------------------------------
// CaptureTest.cs
// Sample application to show the DirectX.Capture class library.
//
// History:
//	2003-Jan-25		BL		- created
//
// Copyright (c) 2003 Brian Low
// ------------------------------------------------------------------

using System;
using System.Windows;
using System.Diagnostics;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using DirectX.Capture;
using Microsoft.VisualBasic;
using ScreenShotDemo;
using System.Drawing.Imaging;
using Microsoft.Kinect;
using Microsoft.Kinect.Face;
using Microsoft.Samples.Kinect.HDFaceBasics;
using System.Windows.Media;
using System.Windows.Media.Media3D;
//using Microsoft.Samples.Kinect.HDFaceBasics;

namespace CaptureTest
{
    public class CaptureTest : System.Windows.Forms.Form
    {
        // 2013-02-11 __ZSC__
        private string new_path;
        private string video_name;
        private string date_today = DateTime.Now.ToString("yyyy-MM-dd");
        private string launch_time = DateTime.Now.ToString("HH:mm:ss");
        private string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);

        private string subjectID;

        private Capture capture = null;
        private Filters filters = new Filters();

        /// Kinect namespaces
        /// <summary>
        /// Currently used KinectSensor
        /// </summary>
        private KinectSensor sensor = null;

        /// <summary>
        /// Body frame source to get a BodyFrameReader
        /// </summary>
        private BodyFrameSource bodySource = null;

        /// <summary>
        /// Body frame reader to get body frames
        /// </summary>
        private BodyFrameReader bodyReader = null;

        public string coordinates;

        private DateTime date1 = new DateTime(0);

        //private Quaternion quat;

        private System.Windows.Media.Media3D.MeshGeometry3D theGeometry;

        /// <summary>
        /// HighDefinitionFaceFrameSource to get a reader and a builder from.
        /// Also to set the currently tracked user id to get High Definition Face Frames of
        /// </summary>
        private HighDefinitionFaceFrameSource highDefinitionFaceFrameSource = null;

        /// <summary>
        /// HighDefinitionFaceFrameReader to read HighDefinitionFaceFrame to get FaceAlignment
        /// </summary>
        private HighDefinitionFaceFrameReader highDefinitionFaceFrameReader = null;

        /// <summary>
        /// FaceAlignment is the result of tracking a face, it has face animations location and orientation
        /// </summary>
        private FaceAlignment currentFaceAlignment = null;

        /// <summary>
        /// FaceModel is a result of capturing a face
        /// </summary>
        private FaceModel currentFaceModel = null;

        /// <summary>
        /// FaceModelBuilder is used to produce a FaceModel
        /// </summary>
        private FaceModelBuilder faceModelBuilder = null;

        /// <summary>
        /// The currently tracked body
        /// </summary>
        private Body currentTrackedBody = null;

        /// <summary>
        /// The currently tracked body
        /// </summary>
        private ulong currentTrackingId = 0;

        /// <summary>
        /// Gets or sets the current tracked user id
        /// </summary>
        private string currentBuilderStatus = string.Empty;

        /// <summary>
        /// Gets or sets the current status text to display
        /// </summary>
        private string statusText = "Ready To Start Capture";


        private System.Windows.Forms.TextBox txtFilename;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button btnCapture;
        private System.Windows.Forms.Button btnStart;
        private System.Windows.Forms.Button btnStop;
        private System.Windows.Forms.Button btnExit;
        private System.Windows.Forms.MenuItem menuItem1;
        private System.Windows.Forms.MenuItem menuItem7;
        private System.Windows.Forms.MainMenu mainMenu;
        private System.Windows.Forms.MenuItem mnuExit;
        private System.Windows.Forms.MenuItem mnuDevices;
        private System.Windows.Forms.MenuItem mnuVideoDevices;
        private System.Windows.Forms.MenuItem mnuAudioDevices;
        private System.Windows.Forms.MenuItem mnuVideoCompressors;
        private System.Windows.Forms.MenuItem mnuAudioCompressors;
        private System.Windows.Forms.MenuItem mnuVideoSources;
        private System.Windows.Forms.MenuItem mnuAudioSources;
        private System.Windows.Forms.Panel panelVideo;
        private System.Windows.Forms.MenuItem menuItem4;
        private System.Windows.Forms.MenuItem mnuAudioChannels;
        private System.Windows.Forms.MenuItem mnuAudioSamplingRate;
        private System.Windows.Forms.MenuItem mnuAudioSampleSizes;
        private System.Windows.Forms.MenuItem menuItem5;
        private System.Windows.Forms.MenuItem mnuFrameSizes;
        private System.Windows.Forms.MenuItem mnuFrameRates;
        private System.Windows.Forms.Button btnCue;
        private System.Windows.Forms.MenuItem menuItem6;
        private System.Windows.Forms.MenuItem mnuPreview;
        private System.Windows.Forms.MenuItem menuItem8;
        private System.Windows.Forms.MenuItem mnuPropertyPages;
        private System.Windows.Forms.MenuItem mnuVideoCaps;
        private System.Windows.Forms.MenuItem mnuAudioCaps;
        private System.Windows.Forms.MenuItem mnuChannel;
        private System.Windows.Forms.MenuItem menuItem3;
        private System.Windows.Forms.MenuItem mnuInputType;
        private IContainer components;

        //private DateTime date1 = new DateTime(0);
        private MenuItem menuPostProcess;
        private MenuItem menuRunPostProcess;
        private Label label2;
        private TextBox txt_recordingID;
        private Label isTrackingLabel;
        private Label upNeededLabel;
        private Label rightNeededLabel;
        private Label leftNeededLabel;
        private Label frontNeededLabel;
        private Label captureCompleteLabel;
        private string startEndtimes;
        Process myProc;

        public CaptureTest()
        {
            // 2013-02-20 __ZC__
            //Process myProc;
            // 4-22-2013 removing process for Face Tracking?? problem with Laptop running the secondary process
            //myProc = Process.Start(dtopfolder + @"\dev\GitHub\Ultraspeech\UltraCapture\FaceTrackingBasics-WPF\bin\x64\Release\FaceTrackingBasics.exe");

            //myProc = Process.Start(@"C:\Users\apiladmin\Documents\GitHub\APIL\UltraCapture\FaceTrackingBasics-WPF\bin\x64\Release\FaceTrackingBasics.exe");

            //myProc = Process.Start(@"C:\Users\apiladmin\Desktop\HDFaceBasics-WPF\bin\x64\Debug\HDFaceBasics-WPF.exe");
            //
            // Required for Windows Form Designer support
            //
            //Microsoft.Samples.Kinect.HDFaceBasics.MainWindow kinect = new Microsoft.Samples.Kinect.HDFaceBasics.MainWindow();
            //kinect.InitializeComponent();
            //kinect.InitializeHDFace();
            InitializeComponent();
            //InitializeKinect();
            
            
            // start 2013-02-19 __ZC__

            // need to specify how to get an actual subject ID number
            //subjectID = "000123";
            /*subjectID = txt_recordingID.Text;

            // Creates desktop folder for all files

            new_path = dtopfolder + @"\" + date_today + "_" + subjectID;

            if (!(System.IO.Directory.Exists(new_path)))
            {
                System.IO.Directory.CreateDirectory(new_path);
                //System.Windows.Forms.MessageBox.Show("created new directory at:\n\n" + new_path);
            }
            else
            {
                new_path += "_1";
                System.IO.Directory.CreateDirectory(new_path);
            }

            // specify video filename
            video_name = new_path + @"\" + "video.avi";*/

            // end 2013-02-19






            // Start with the first video/audio devices
            // Don't do this in the Release build in case the
            // first devices cause problems.

            // ======================================================
            // Commented 4-4-2013 -- We want first device initialized
            //#if DEBUG
            // ======================================================
            capture = new Capture(filters.VideoInputDevices[0], filters.AudioInputDevices[0]);
            capture.CaptureComplete += new EventHandler(OnCaptureComplete);
            // ======================================================
            ////#endif
            // ======================================================

            // Update the main menu
            // Much of the interesting work of this sample occurs here

            //var devices = await DeviceInformation.FindAllAsync(DeviceClass.VideoCapture);


            try { updateMenu(); }
            catch { }
        }

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                if (components != null)
                {
                    components.Dispose();
                }
            }
            base.Dispose(disposing);
        }


        #region Windows Form Designer generated code
        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.txtFilename = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.btnCapture = new System.Windows.Forms.Button();
            this.btnStart = new System.Windows.Forms.Button();
            this.btnStop = new System.Windows.Forms.Button();
            this.btnExit = new System.Windows.Forms.Button();
            this.mainMenu = new System.Windows.Forms.MainMenu(this.components);
            this.menuItem1 = new System.Windows.Forms.MenuItem();
            this.mnuExit = new System.Windows.Forms.MenuItem();
            this.mnuDevices = new System.Windows.Forms.MenuItem();
            this.mnuVideoDevices = new System.Windows.Forms.MenuItem();
            this.mnuAudioDevices = new System.Windows.Forms.MenuItem();
            this.menuItem4 = new System.Windows.Forms.MenuItem();
            this.mnuVideoCompressors = new System.Windows.Forms.MenuItem();
            this.mnuAudioCompressors = new System.Windows.Forms.MenuItem();
            this.menuItem7 = new System.Windows.Forms.MenuItem();
            this.mnuVideoSources = new System.Windows.Forms.MenuItem();
            this.mnuFrameSizes = new System.Windows.Forms.MenuItem();
            this.mnuFrameRates = new System.Windows.Forms.MenuItem();
            this.mnuVideoCaps = new System.Windows.Forms.MenuItem();
            this.menuItem5 = new System.Windows.Forms.MenuItem();
            this.mnuAudioSources = new System.Windows.Forms.MenuItem();
            this.mnuAudioChannels = new System.Windows.Forms.MenuItem();
            this.mnuAudioSamplingRate = new System.Windows.Forms.MenuItem();
            this.mnuAudioSampleSizes = new System.Windows.Forms.MenuItem();
            this.mnuAudioCaps = new System.Windows.Forms.MenuItem();
            this.menuItem3 = new System.Windows.Forms.MenuItem();
            this.mnuChannel = new System.Windows.Forms.MenuItem();
            this.mnuInputType = new System.Windows.Forms.MenuItem();
            this.menuItem6 = new System.Windows.Forms.MenuItem();
            this.mnuPropertyPages = new System.Windows.Forms.MenuItem();
            this.menuItem8 = new System.Windows.Forms.MenuItem();
            this.mnuPreview = new System.Windows.Forms.MenuItem();
            this.menuPostProcess = new System.Windows.Forms.MenuItem();
            this.menuRunPostProcess = new System.Windows.Forms.MenuItem();
            this.panelVideo = new System.Windows.Forms.Panel();
            this.btnCue = new System.Windows.Forms.Button();
            this.label2 = new System.Windows.Forms.Label();
            this.txt_recordingID = new System.Windows.Forms.TextBox();
            this.isTrackingLabel = new System.Windows.Forms.Label();
            this.frontNeededLabel = new System.Windows.Forms.Label();
            this.upNeededLabel = new System.Windows.Forms.Label();
            this.leftNeededLabel = new System.Windows.Forms.Label();
            this.rightNeededLabel = new System.Windows.Forms.Label();
            this.captureCompleteLabel = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // txtFilename
            // 
            this.txtFilename.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.txtFilename.Location = new System.Drawing.Point(139, 390);
            this.txtFilename.Name = "txtFilename";
            this.txtFilename.Size = new System.Drawing.Size(124, 20);
            this.txtFilename.TabIndex = 0;
            this.txtFilename.Text = "video.avi";
            // 
            // label1
            // 
            this.label1.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.label1.Location = new System.Drawing.Point(69, 393);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(64, 16);
            this.label1.TabIndex = 1;
            this.label1.Text = "Filename:";
            //
            //btnCapture
            //
            this.btnCapture.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.btnCapture.Location = new System.Drawing.Point(150, 422);
            //this.btnCapture.Location = new System.Drawing.Point(500, 600);
            this.btnCapture.Name = "btnCapture";
            this.btnCapture.Size = new System.Drawing.Size(60, 24);
            this.btnCapture.TabIndex = 4;
            this.btnCapture.Text = "Capture";
            //this.btnCapture.Click += new System.EventHandler(this.btnCapture_Capture);
            // 
            // btnStart
            // 
            this.btnStart.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.btnStart.Location = new System.Drawing.Point(230, 422);//(160, 422);
            this.btnStart.Name = "btnStart";
            this.btnStart.Size = new System.Drawing.Size(60, 24);
            this.btnStart.TabIndex = 2;
            this.btnStart.Text = "Record";
            this.btnStart.Click += new System.EventHandler(this.btnStart_Click);
            // 
            // btnStop
            // 
            this.btnStop.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.btnStop.Location = new System.Drawing.Point(320, 422);//(248, 422);
            this.btnStop.Name = "btnStop";
            this.btnStop.Size = new System.Drawing.Size(40, 24);
            this.btnStop.TabIndex = 3;
            this.btnStop.Text = "Stop";
            this.btnStop.Click += new System.EventHandler(this.btnStop_Click);
            // 
            // btnExit
            // 
            this.btnExit.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.btnExit.Location = new System.Drawing.Point(376, 422);
            this.btnExit.Name = "btnExit";
            this.btnExit.Size = new System.Drawing.Size(120, 24);
            this.btnExit.TabIndex = 5;
            this.btnExit.Text = "Exit";
            this.btnExit.Click += new System.EventHandler(this.btnExit_Click);
            // 
            // mainMenu
            // 
            this.mainMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.menuItem1,
            this.mnuDevices,
            this.menuItem7,
            this.menuPostProcess});
            // 
            // menuItem1
            // 
            this.menuItem1.Index = 0;
            this.menuItem1.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.mnuExit});
            this.menuItem1.Text = "File";
            // 
            // mnuExit
            // 
            this.mnuExit.Index = 0;
            this.mnuExit.Text = "E&xit";
            this.mnuExit.Click += new System.EventHandler(this.mnuExit_Click);
            // 
            // mnuDevices
            // 
            this.mnuDevices.Index = 1;
            this.mnuDevices.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.mnuVideoDevices,
            this.mnuAudioDevices,
            this.menuItem4,
            this.mnuVideoCompressors,
            this.mnuAudioCompressors});
            this.mnuDevices.Text = "Devices";
            // 
            // mnuVideoDevices
            // 
            this.mnuVideoDevices.Index = 0;
            this.mnuVideoDevices.Text = "Video Devices";
            // 
            // mnuAudioDevices
            // 
            this.mnuAudioDevices.Index = 1;
            this.mnuAudioDevices.Text = "Audio Devices";
            // 
            // menuItem4
            // 
            this.menuItem4.Index = 2;
            this.menuItem4.Text = "-";
            // 
            // mnuVideoCompressors
            // 
            this.mnuVideoCompressors.Index = 3;
            this.mnuVideoCompressors.Text = "Video Compressors";
            // 
            // mnuAudioCompressors
            // 
            this.mnuAudioCompressors.Index = 4;
            this.mnuAudioCompressors.Text = "Audio Compressors";
            // 
            // menuItem7
            // 
            this.menuItem7.Index = 2;
            this.menuItem7.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.mnuVideoSources,
            this.mnuFrameSizes,
            this.mnuFrameRates,
            this.mnuVideoCaps,
            this.menuItem5,
            this.mnuAudioSources,
            this.mnuAudioChannels,
            this.mnuAudioSamplingRate,
            this.mnuAudioSampleSizes,
            this.mnuAudioCaps,
            this.menuItem3,
            this.mnuChannel,
            this.mnuInputType,
            this.menuItem6,
            this.mnuPropertyPages,
            this.menuItem8,
            this.mnuPreview});
            this.menuItem7.Text = "Options";
            // 
            // mnuVideoSources
            // 
            this.mnuVideoSources.Index = 0;
            this.mnuVideoSources.Text = "Video Sources";
            // 
            // mnuFrameSizes
            // 
            this.mnuFrameSizes.Index = 1;
            this.mnuFrameSizes.Text = "Video Frame Size";
            // 
            // mnuFrameRates
            // 
            this.mnuFrameRates.Index = 2;
            this.mnuFrameRates.Text = "Video Frame Rate";
            this.mnuFrameRates.Click += new System.EventHandler(this.mnuFrameRates_Click);
            // 
            // mnuVideoCaps
            // 
            this.mnuVideoCaps.Index = 3;
            this.mnuVideoCaps.Text = "Video Capabilities...";
            this.mnuVideoCaps.Click += new System.EventHandler(this.mnuVideoCaps_Click);
            // 
            // menuItem5
            // 
            this.menuItem5.Index = 4;
            this.menuItem5.Text = "-";
            // 
            // mnuAudioSources
            // 
            this.mnuAudioSources.Index = 5;
            this.mnuAudioSources.Text = "Audio Sources";
            // 
            // mnuAudioChannels
            // 
            this.mnuAudioChannels.Index = 6;
            this.mnuAudioChannels.Text = "Audio Channels";
            // 
            // mnuAudioSamplingRate
            // 
            this.mnuAudioSamplingRate.Index = 7;
            this.mnuAudioSamplingRate.Text = "Audio Sampling Rate";
            // 
            // mnuAudioSampleSizes
            // 
            this.mnuAudioSampleSizes.Index = 8;
            this.mnuAudioSampleSizes.Text = "Audio Sample Size";
            // 
            // mnuAudioCaps
            // 
            this.mnuAudioCaps.Index = 9;
            this.mnuAudioCaps.Text = "Audio Capabilities...";
            this.mnuAudioCaps.Click += new System.EventHandler(this.mnuAudioCaps_Click);
            // 
            // menuItem3
            // 
            this.menuItem3.Index = 10;
            this.menuItem3.Text = "-";
            // 
            // mnuChannel
            // 
            this.mnuChannel.Index = 11;
            this.mnuChannel.Text = "TV Tuner Channel";
            // 
            // mnuInputType
            // 
            this.mnuInputType.Index = 12;
            this.mnuInputType.Text = "TV Tuner Input Type";
            this.mnuInputType.Click += new System.EventHandler(this.mnuInputType_Click);
            // 
            // menuItem6
            // 
            this.menuItem6.Index = 13;
            this.menuItem6.Text = "-";
            // 
            // mnuPropertyPages
            // 
            this.mnuPropertyPages.Index = 14;
            this.mnuPropertyPages.Text = "PropertyPages";
            // 
            // menuItem8
            // 
            this.menuItem8.Index = 15;
            this.menuItem8.Text = "-";
            // 
            // mnuPreview
            // 
            this.mnuPreview.Index = 16;
            this.mnuPreview.Text = "Preview";
            this.mnuPreview.Click += new System.EventHandler(this.mnuPreview_Click);
            // 
            // menuPostProcess
            // 
            this.menuPostProcess.Index = 3;
            this.menuPostProcess.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.menuRunPostProcess});
            this.menuPostProcess.Text = "Post Processing";
            // 
            // menuRunPostProcess
            // 
            this.menuRunPostProcess.Index = 0;
            this.menuRunPostProcess.Text = "Run Post Process";
            this.menuRunPostProcess.Click += new System.EventHandler(this.menuRunPostProcess_Click);
            // 
            // panelVideo
            // 
            this.panelVideo.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
            | System.Windows.Forms.AnchorStyles.Left)
            | System.Windows.Forms.AnchorStyles.Right)));
            this.panelVideo.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.panelVideo.Location = new System.Drawing.Point(8, 8);
            this.panelVideo.Name = "panelVideo";
            this.panelVideo.Size = new System.Drawing.Size(488, 366);
            this.panelVideo.TabIndex = 6;
            // 
            // btnCue
            // 
            this.btnCue.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.btnCue.Location = new System.Drawing.Point(36, 422);
            this.btnCue.Name = "btnCue";
            this.btnCue.Size = new System.Drawing.Size(80, 24);
            this.btnCue.TabIndex = 8;
            this.btnCue.Text = "Preview";
            this.btnCue.Click += new System.EventHandler(this.btnCue_Click);
            // 
            // label2
            // 
            this.label2.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.label2.Location = new System.Drawing.Point(313, 390);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(77, 17);
            this.label2.TabIndex = 11;
            this.label2.Text = "Recording ID:";
            // 
            // txt_recordingID
            // 
            this.txt_recordingID.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.txt_recordingID.Location = new System.Drawing.Point(393, 387);
            this.txt_recordingID.Name = "txt_recordingID";
            this.txt_recordingID.Size = new System.Drawing.Size(103, 20);
            this.txt_recordingID.TabIndex = 10;
            this.txt_recordingID.Text = "000123";
            //
            // isTrackingLabel
            //
            this.isTrackingLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.isTrackingLabel.Location = new System.Drawing.Point(550, 270);
            this.isTrackingLabel.Name = "isTrackingLabel";
            this.isTrackingLabel.TabIndex = 11;
            this.isTrackingLabel.Text = "Kinect\nNOT\nTracking";
            //this.isTrackingLabel.Text = "Kinect\nIS\nTracking";
            this.isTrackingLabel.Font = new Font("Times New Roman", 14);
            //this.isTrackingLabel.AutoSize = true;
            this.isTrackingLabel.BackColor = System.Drawing.Color.Red;
            //this.isTrackingLabel.BackColor = System.Drawing.Color.LawnGreen;
            this.isTrackingLabel.Size = new System.Drawing.Size(200, 100);
            this.isTrackingLabel.TextAlign = ContentAlignment.MiddleCenter;
            //
            // frontNeededLabel
            //
            this.frontNeededLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.frontNeededLabel.Location = new System.Drawing.Point(615, 100);
            this.frontNeededLabel.Name = "frontNeededLabel";
            this.frontNeededLabel.TabIndex = 11;
            //this.frontNeededLabel.Text = "Capture\nFRONT";
            this.frontNeededLabel.Text = "FRONT\nCaptured";
            this.frontNeededLabel.Font = new Font("Times New Roman", 14);
            this.frontNeededLabel.AutoSize = true;
            //this.frontNeededLabel.BackColor = System.Drawing.Color.Red;
            //this.frontNeededLabel.BackColor = System.Drawing.Color.Yellow;
            this.frontNeededLabel.BackColor = System.Drawing.Color.LawnGreen;
            this.frontNeededLabel.TextAlign = ContentAlignment.MiddleCenter;
            //
            // upNeededLabel
            //
            this.upNeededLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.upNeededLabel.Location = new System.Drawing.Point(615, 30);
            this.upNeededLabel.Name = "upNeededLabel";
            this.upNeededLabel.TabIndex = 11;
            //this.upNeededLabel.Text = "Capture\nUP";
            this.upNeededLabel.Text = "UP\nCaptured";
            this.upNeededLabel.Font = new Font("Times New Roman", 14);
            this.upNeededLabel.AutoSize = true;
            //this.upNeededLabel.BackColor = System.Drawing.Color.Red;
            this.upNeededLabel.BackColor = System.Drawing.Color.LawnGreen;
            this.upNeededLabel.TextAlign = ContentAlignment.MiddleCenter;
            //
            // rightNeededLabel
            //
            this.rightNeededLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.rightNeededLabel.Location = new System.Drawing.Point(710, 100);
            this.rightNeededLabel.Name = "rightNeededLabel";
            this.rightNeededLabel.TabIndex = 11;
            //this.rightNeededLabel.Text = "Capture\nRIGHT";
            this.rightNeededLabel.Text = "RIGHT\nCaptured";
            this.rightNeededLabel.Font = new Font("Times New Roman", 14);
            this.rightNeededLabel.AutoSize = true;
            //this.rightNeededLabel.BackColor = System.Drawing.Color.Red;
            //this.rightNeededLabel.BackColor = System.Drawing.Color.Yellow;
            this.rightNeededLabel.BackColor = System.Drawing.Color.LawnGreen;
            this.upNeededLabel.TextAlign = ContentAlignment.MiddleCenter;
            //
            // leftNeededLabel
            //
            this.leftNeededLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.leftNeededLabel.Location = new System.Drawing.Point(520, 100);
            this.leftNeededLabel.Name = "leftNeededLabel";
            this.leftNeededLabel.TabIndex = 11;
            //this.leftNeededLabel.Text = "Capture\nLEFT";
            this.leftNeededLabel.Text = "LEFT\nCaptured";
            this.leftNeededLabel.Font = new Font("Times New Roman", 14);
            this.leftNeededLabel.AutoSize = true;
            //this.leftNeededLabel.BackColor = System.Drawing.Color.Red;
            this.leftNeededLabel.BackColor = System.Drawing.Color.LawnGreen;
            this.leftNeededLabel.TextAlign = ContentAlignment.MiddleCenter;
            //
            // captureCompleteLabel
            //
            this.captureCompleteLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.captureCompleteLabel.Location = new System.Drawing.Point(550, 190);
            this.captureCompleteLabel.Name = "captureCompleteLabel";
            this.captureCompleteLabel.TabIndex = 11;
            this.captureCompleteLabel.Text = "CAPTURE COMPLETE";
            this.captureCompleteLabel.Font = new Font("Times New Roman", 14);
            this.captureCompleteLabel.AutoSize = true;
            this.captureCompleteLabel.BackColor = System.Drawing.Color.LawnGreen;
            this.captureCompleteLabel.TextAlign = ContentAlignment.MiddleCenter;
            // 
            // CaptureTest
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
            //this.ClientSize = new System.Drawing.Size(504, 455);
            this.ClientSize = new System.Drawing.Size(800, 475);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.txt_recordingID);
            this.Controls.Add(this.btnCue);
            this.Controls.Add(this.panelVideo);
            this.Controls.Add(this.btnExit);
            this.Controls.Add(this.btnStop);
            this.Controls.Add(this.btnStart);
            this.Controls.Add(this.btnCapture);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.txtFilename);
            this.Controls.Add(this.isTrackingLabel);
            this.Controls.Add(this.frontNeededLabel);
            this.Controls.Add(this.upNeededLabel);
            this.Controls.Add(this.rightNeededLabel);
            this.Controls.Add(this.leftNeededLabel);
            this.Controls.Add(this.captureCompleteLabel);
            this.Menu = this.mainMenu;
            this.Name = "CaptureTest";
            this.Text = "Ultrasound Capture";
            this.Load += new System.EventHandler(this.CaptureTest_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

            //MainWindow kinect = new MainWindow();
            //kinect.MainWindow
        }
        #endregion

        //private void rectangle_Paint(object sender, PaintEventArgs paint) //, Rectangle r, String s)
        //{
        //    // Create a local version of the graphics object for the PictureBox.
        //    Graphics g = paint.Graphics;

        //    // Fill a rectangle with a color.
        //    g.FillRectangle(new SolidBrush(Color.FromName("red")), this.isTracking);
        //}

        //protected override void OnPaint(PaintEventArgs paintEvnt)
        //{
        //    // Get the graphics object 
        //    Graphics gfx = paintEvnt.Graphics;
        //    // Create a new pen that we shall use for drawing the line 
        //    gfx.FillRectangle(new SolidBrush(Color.FromName("red")), this.isTracking);
        //}

        //public void makegreen(PaintEventArgs paintEvnt)
        //{
        //    // Get the graphics object 
        //    Graphics gfx = paintEvnt.Graphics;
        //    // Create a new pen that we shall use for drawing the line 
        //    gfx.FillRectangle(new SolidBrush(Color.FromName("red")), this.isTracking);
        //}


        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main() 
		{
            //System.Windows.Forms.Application.Run(new MainWindow());
            AppDomain currentDomain = AppDomain.CurrentDomain;
            
            System.Windows.Forms.Application.Run(new CaptureTest());
            
            
		}

        private void btnExit_Click(object sender, System.EventArgs e)
        {
            if (capture != null)
                capture.Stop();
            System.Windows.Forms.Application.Exit();
        }

        private void btnCue_Click(object sender, System.EventArgs e)
        {
            // 2013-02-19 __ZC__
            // added try/exception from Preview dropdown menu
            // allows Prevew button to show or hide preview
            try
            {
                if (capture.PreviewWindow == null)
                {
                    capture.PreviewWindow = panelVideo;
                    mnuPreview.Checked = true;
                }
                else
                {
                    capture.PreviewWindow = null;
                    mnuPreview.Checked = false;
                }
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable to enable/disable preview. Please submit a bug report.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void btnCapture_Click(object sender, System.EventArgs e)
        {
            // 2013-02-19 __ZC__
            // added try/exception from Preview dropdown menu
            // allows Prevew button to show or hide preview
            try
            {
                Microsoft.Kinect.Face.FaceModelBuilderCollectionStatus CollectionStatus;

                //{
                //    capture.PreviewWindow = panelVideo;
                //    mnuPreview.Checked = true;
                //}
                //else
                //{
                //    capture.PreviewWindow = null;
                //    mnuPreview.Checked = false;
                //}
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable to capture face.");
            }
        }

        private void btnStart_Click(object sender, System.EventArgs e)
        {
            try
            {
                btnStart.BackColor = System.Drawing.Color.Red;
                // start 2013-02-19 __ZC__

                // need to specify how to get an actual subject ID number
                //subjectID = "000123";
                string input_subject = txt_recordingID.Text;

                // getting rid of bad characters otherwise ffmpeg post-processing will break
                string pattern = "[\\~#%&*{}/:<>?|\"-]";
                string replacement = "_";

                System.Text.RegularExpressions.Regex regEx = new System.Text.RegularExpressions.Regex(pattern);
                subjectID = System.Text.RegularExpressions.Regex.Replace(regEx.Replace(input_subject, replacement), @"\s+", "_");

                // Creates desktop folder for all files

                new_path = dtopfolder + @"\" + date_today + "_" + subjectID;

                if (!(System.IO.Directory.Exists(new_path)))
                {
                    System.IO.Directory.CreateDirectory(new_path);
                    //System.Windows.Forms.MessageBox.Show("created new directory at:\n\n" + new_path);
                }
                else
                {
                    new_path += "_1";
                    System.IO.Directory.CreateDirectory(new_path);
                }

                // specify video filename
                video_name = new_path + @"\" + "video.avi";

                // end 2013-02-19

                if (capture == null)
                    throw new ApplicationException("Please select a video and/or audio device.");
                if (!capture.Cued)
                    capture.Filename = new_path + @"\" + txtFilename.Text;

                date1 = DateTime.Now;
                startEndtimes += "Before start command: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + "\r\n";

                capture.Start();

                date1 = DateTime.Now;
                startEndtimes += "After start command:  " + date1.ToString("yyyyyyyyMMddHHmmssfff") + "\r\n";


                btnCue.Enabled = false;
                btnStart.Enabled = true;
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show(ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void btnStop_Click(object sender, System.EventArgs e)
        {
            try
            {
                if (capture == null)
                    throw new ApplicationException("Please select a video and/or audio device.");
                if (btnStart.BackColor == System.Drawing.Color.Red)
                {
                    // 4-22-2013 removing process for Face Tracking?? doesn't work on Laptop
                    myProc.CloseMainWindow();
                    // Changed from Kill() to CloseMainWindow() so that coords.txt is generated
                    //myProc.Kill();

                    btnStart.BackColor = System.Drawing.Color.Gray;
                    date1 = DateTime.Now;
                    startEndtimes += "Before stop time:     " + date1.ToString("yyyyyyyyMMddHHmmssfff") + "\r\n";
                    capture.Stop();
                    date1 = DateTime.Now;
                    startEndtimes += "Stop time:            " + date1.ToString("yyyyyyyyMMddHHmmssfff") + "\r\n";

                    System.IO.File.WriteAllText((new_path + @"\" + "vidTimes.txt"), startEndtimes);

                    btnCue.Enabled = true;
                    // for tracking if video is running
                    btnStart.Enabled = false;

                }
                else
                {
                    System.Windows.Forms.MessageBox.Show("No recording started");
                }
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show(ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void updateMenu()
        {
            MenuItem m;
            Filter f;
            Source s;
            Source current;
            PropertyPage p;
            Control oldPreviewWindow = null;

            // Disable preview to avoid additional flashes (optional)
            if (capture != null)
            {
                oldPreviewWindow = capture.PreviewWindow;
                capture.PreviewWindow = null;
            }

            // Load video devices
            Filter videoDevice = null;
            if (capture != null)
                //Console.WriteLine("HELLO");
                videoDevice = capture.VideoDevice;
            //if (capture == null)
            //    Console.WriteLine("GOODBYE");
            //if (true)
            //    Console.WriteLine("EXIST");
            //Console.WriteLine("1: " + videoDevice.ToString());
            mnuVideoDevices.MenuItems.Clear();
            m = new MenuItem("(None)", new EventHandler(mnuVideoDevices_Click));
            m.Checked = (videoDevice == null);
            mnuVideoDevices.MenuItems.Add(m);
            for (int c = 0; c < filters.VideoInputDevices.Count; c++)
            {
                f = filters.VideoInputDevices[c];
                //Console.WriteLine("2: " + f.Name.ToString());
                m = new MenuItem(f.Name, new EventHandler(mnuVideoDevices_Click));
                //Console.WriteLine("3: "+ m.ToString() + ", " + m.Name.ToString());
                m.Checked = (videoDevice == f);
                mnuVideoDevices.MenuItems.Add(m);
            }
            mnuVideoDevices.Enabled = (filters.VideoInputDevices.Count > 0);

            // Load audio devices
            Filter audioDevice = null;
            if (capture != null)
                audioDevice = capture.AudioDevice;
            mnuAudioDevices.MenuItems.Clear();
            m = new MenuItem("(None)", new EventHandler(mnuAudioDevices_Click));
            m.Checked = (audioDevice == null);
            mnuAudioDevices.MenuItems.Add(m);
            for (int c = 0; c < filters.AudioInputDevices.Count; c++)
            {
                f = filters.AudioInputDevices[c];
                m = new MenuItem(f.Name, new EventHandler(mnuAudioDevices_Click));
                m.Checked = (audioDevice == f);
                mnuAudioDevices.MenuItems.Add(m);
            }
            mnuAudioDevices.Enabled = (filters.AudioInputDevices.Count > 0);


            // Load video compressors
            try
            {
                mnuVideoCompressors.MenuItems.Clear();
                m = new MenuItem("(None)", new EventHandler(mnuVideoCompressors_Click));
                m.Checked = (capture.VideoCompressor == null);
                mnuVideoCompressors.MenuItems.Add(m);
                for (int c = 0; c < filters.VideoCompressors.Count; c++)
                {
                    f = filters.VideoCompressors[c];
                    m = new MenuItem(f.Name, new EventHandler(mnuVideoCompressors_Click));
                    m.Checked = (capture.VideoCompressor == f);
                    mnuVideoCompressors.MenuItems.Add(m);
                }
                mnuVideoCompressors.Enabled = ((capture.VideoDevice != null) && (filters.VideoCompressors.Count > 0));
            }
            catch { mnuVideoCompressors.Enabled = false; }

            // Load audio compressors
            try
            {
                mnuAudioCompressors.MenuItems.Clear();
                m = new MenuItem("(None)", new EventHandler(mnuAudioCompressors_Click));
                m.Checked = (capture.AudioCompressor == null);
                mnuAudioCompressors.MenuItems.Add(m);
                for (int c = 0; c < filters.AudioCompressors.Count; c++)
                {
                    f = filters.AudioCompressors[c];
                    m = new MenuItem(f.Name, new EventHandler(mnuAudioCompressors_Click));
                    m.Checked = (capture.AudioCompressor == f);
                    mnuAudioCompressors.MenuItems.Add(m);
                }
                mnuAudioCompressors.Enabled = ((capture.AudioDevice != null) && (filters.AudioCompressors.Count > 0));
            }
            catch { mnuAudioCompressors.Enabled = false; }

            // Load video sources
            try
            {
                mnuVideoSources.MenuItems.Clear();
                current = capture.VideoSource;
                //Console.WriteLine("1: " + capture.VideoSource.ToString());
                //Console.WriteLine("2: " + capture.VideoSources[0].ToString());
                for (int c = 0; c < capture.VideoSources.Count; c++)
                {
                    s = capture.VideoSources[c];
                    m = new MenuItem(s.Name, new EventHandler(mnuVideoSources_Click));
                    m.Checked = (current == s);
                    mnuVideoSources.MenuItems.Add(m);
                }
                mnuVideoSources.Enabled = (capture.VideoSources.Count > 0);
            }
            catch { mnuVideoSources.Enabled = false; }

            // Load audio sources
            try
            {
                mnuAudioSources.MenuItems.Clear();
                current = capture.AudioSource;
                for (int c = 0; c < capture.AudioSources.Count; c++)
                {
                    s = capture.AudioSources[c];
                    m = new MenuItem(s.Name, new EventHandler(mnuAudioSources_Click));
                    m.Checked = (current == s);
                    mnuAudioSources.MenuItems.Add(m);
                }
                mnuAudioSources.Enabled = (capture.AudioSources.Count > 0);
            }
            catch { mnuAudioSources.Enabled = false; }

            // Load frame rates
            try
            {
                mnuFrameRates.MenuItems.Clear();
                int frameRate = (int)(capture.FrameRate * 1000);
                m = new MenuItem("15 fps", new EventHandler(mnuFrameRates_Click));
                m.Checked = (frameRate == 15000);
                mnuFrameRates.MenuItems.Add(m);
                m = new MenuItem("24 fps (Film)", new EventHandler(mnuFrameRates_Click));
                m.Checked = (frameRate == 24000);
                mnuFrameRates.MenuItems.Add(m);
                m = new MenuItem("25 fps (PAL)", new EventHandler(mnuFrameRates_Click));
                m.Checked = (frameRate == 25000);
                mnuFrameRates.MenuItems.Add(m);
                m = new MenuItem("29.997 fps (NTSC)", new EventHandler(mnuFrameRates_Click));
                m.Checked = (frameRate == 29997);
                mnuFrameRates.MenuItems.Add(m);
                m = new MenuItem("30 fps (~NTSC)", new EventHandler(mnuFrameRates_Click));
                m.Checked = (frameRate == 30000);
                mnuFrameRates.MenuItems.Add(m);
                m = new MenuItem("59.994 fps (2xNTSC)", new EventHandler(mnuFrameRates_Click));
                m.Checked = (frameRate == 59994);
                mnuFrameRates.MenuItems.Add(m);
                mnuFrameRates.Enabled = true;
            }
            catch { mnuFrameRates.Enabled = false; }

            // Load frame sizes
            try
            {
                mnuFrameSizes.MenuItems.Clear();
                System.Drawing.Size frameSize = capture.FrameSize;
                m = new MenuItem("160 x 120", new EventHandler(mnuFrameSizes_Click));
                m.Checked = (frameSize == new System.Drawing.Size(160, 120));
                mnuFrameSizes.MenuItems.Add(m);
                m = new MenuItem("320 x 240", new EventHandler(mnuFrameSizes_Click));
                m.Checked = (frameSize == new System.Drawing.Size(320, 240));
                mnuFrameSizes.MenuItems.Add(m);
                m = new MenuItem("640 x 480", new EventHandler(mnuFrameSizes_Click));
                m.Checked = (frameSize == new System.Drawing.Size(640, 480));
                mnuFrameSizes.MenuItems.Add(m);
                m = new MenuItem("1024 x 768", new EventHandler(mnuFrameSizes_Click));
                m.Checked = (frameSize == new System.Drawing.Size(1024, 768));
                mnuFrameSizes.MenuItems.Add(m);
                mnuFrameSizes.Enabled = true;
            }
            catch { mnuFrameSizes.Enabled = false; }

            // Load audio channels
            try
            {
                mnuAudioChannels.MenuItems.Clear();
                short audioChannels = capture.AudioChannels;
                m = new MenuItem("Mono", new EventHandler(mnuAudioChannels_Click));
                m.Checked = (audioChannels == 1);
                mnuAudioChannels.MenuItems.Add(m);
                m = new MenuItem("Stereo", new EventHandler(mnuAudioChannels_Click));
                m.Checked = (audioChannels == 2);
                mnuAudioChannels.MenuItems.Add(m);
                mnuAudioChannels.Enabled = true;
            }
            catch { mnuAudioChannels.Enabled = false; }

            // Load audio sampling rate
            try
            {
                mnuAudioSamplingRate.MenuItems.Clear();
                int samplingRate = capture.AudioSamplingRate;
                m = new MenuItem("8 kHz", new EventHandler(mnuAudioSamplingRate_Click));
                m.Checked = (samplingRate == 8000);
                mnuAudioSamplingRate.MenuItems.Add(m);
                m = new MenuItem("11.025 kHz", new EventHandler(mnuAudioSamplingRate_Click));
                m.Checked = (capture.AudioSamplingRate == 11025);
                mnuAudioSamplingRate.MenuItems.Add(m);
                m = new MenuItem("22.05 kHz", new EventHandler(mnuAudioSamplingRate_Click));
                m.Checked = (capture.AudioSamplingRate == 22050);
                mnuAudioSamplingRate.MenuItems.Add(m);
                m = new MenuItem("44.1 kHz", new EventHandler(mnuAudioSamplingRate_Click));
                m.Checked = (capture.AudioSamplingRate == 44100);
                mnuAudioSamplingRate.MenuItems.Add(m);
                mnuAudioSamplingRate.Enabled = true;
            }
            catch { mnuAudioSamplingRate.Enabled = false; }

            // Load audio sample sizes
            try
            {
                mnuAudioSampleSizes.MenuItems.Clear();
                short sampleSize = capture.AudioSampleSize;
                m = new MenuItem("8 bit", new EventHandler(mnuAudioSampleSizes_Click));
                m.Checked = (sampleSize == 8);
                mnuAudioSampleSizes.MenuItems.Add(m);
                m = new MenuItem("16 bit", new EventHandler(mnuAudioSampleSizes_Click));
                m.Checked = (sampleSize == 16);
                mnuAudioSampleSizes.MenuItems.Add(m);
                mnuAudioSampleSizes.Enabled = true;
            }
            catch { mnuAudioSampleSizes.Enabled = false; }

            // Load property pages
            try
            {
                mnuPropertyPages.MenuItems.Clear();
                for (int c = 0; c < capture.PropertyPages.Count; c++)
                {
                    p = capture.PropertyPages[c];
                    m = new MenuItem(p.Name + "...", new EventHandler(mnuPropertyPages_Click));
                    mnuPropertyPages.MenuItems.Add(m);
                }
                mnuPropertyPages.Enabled = (capture.PropertyPages.Count > 0);
            }
            catch { mnuPropertyPages.Enabled = false; }

            // Load TV Tuner channels
            try
            {
                mnuChannel.MenuItems.Clear();
                int channel = capture.Tuner.Channel;
                for (int c = 1; c <= 25; c++)
                {
                    m = new MenuItem(c.ToString(), new EventHandler(mnuChannel_Click));
                    m.Checked = (channel == c);
                    mnuChannel.MenuItems.Add(m);
                }
                mnuChannel.Enabled = true;
            }
            catch { mnuChannel.Enabled = false; }

            // Load TV Tuner input types
            try
            {
                mnuInputType.MenuItems.Clear();
                m = new MenuItem(TunerInputType.Cable.ToString(), new EventHandler(mnuInputType_Click));
                m.Checked = (capture.Tuner.InputType == TunerInputType.Cable);
                mnuInputType.MenuItems.Add(m);
                m = new MenuItem(TunerInputType.Antenna.ToString(), new EventHandler(mnuInputType_Click));
                m.Checked = (capture.Tuner.InputType == TunerInputType.Antenna);
                mnuInputType.MenuItems.Add(m);
                mnuInputType.Enabled = true;
            }
            catch { mnuInputType.Enabled = false; }

            // Enable/disable caps
            mnuVideoCaps.Enabled = ((capture != null) && (capture.VideoCaps != null));
            mnuAudioCaps.Enabled = ((capture != null) && (capture.AudioCaps != null));

            // Check Preview menu option
            mnuPreview.Checked = (oldPreviewWindow != null);
            mnuPreview.Enabled = (capture != null);

            // Reenable preview if it was enabled before
            if (capture != null)
                capture.PreviewWindow = oldPreviewWindow;
        }

        private void mnuVideoDevices_Click(object sender, System.EventArgs e)
        {
            try
            {
                // Get current devices and dispose of capture object
                // because the video and audio device can only be changed
                // by creating a new Capture object.
                Filter videoDevice = null;
                Filter audioDevice = null;
                if (capture != null)
                {
                    videoDevice = capture.VideoDevice;
                    audioDevice = capture.AudioDevice;
                    capture.Dispose();
                    capture = null;
                }

                // Get new video device
                MenuItem m = sender as MenuItem;
                videoDevice = (m.Index > 0 ? filters.VideoInputDevices[m.Index - 1] : null);

                // Create capture object
                if ((videoDevice != null) || (audioDevice != null))
                {
                    capture = new Capture(videoDevice, audioDevice);
                    capture.CaptureComplete += new EventHandler(OnCaptureComplete);
                }

                // Update the menu
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Video device not supported.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuAudioDevices_Click(object sender, System.EventArgs e)
        {
            try
            {
                // Get current devices and dispose of capture object
                // because the video and audio device can only be changed
                // by creating a new Capture object.
                Filter videoDevice = null;
                Filter audioDevice = null;
                if (capture != null)
                {
                    videoDevice = capture.VideoDevice;
                    audioDevice = capture.AudioDevice;
                    capture.Dispose();
                    capture = null;
                }

                // Get new audio device
                MenuItem m = sender as MenuItem;
                audioDevice = (m.Index > 0 ? filters.AudioInputDevices[m.Index - 1] : null);

                // Create capture object
                if ((videoDevice != null) || (audioDevice != null))
                {
                    capture = new Capture(videoDevice, audioDevice);
                    capture.CaptureComplete += new EventHandler(OnCaptureComplete);
                }

                // Update the menu
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Audio device not supported.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuVideoCompressors_Click(object sender, System.EventArgs e)
        {
            try
            {
                // Change the video compressor
                // We subtract 1 from m.Index beacuse the first item is (None)
                MenuItem m = sender as MenuItem;
                capture.VideoCompressor = (m.Index > 0 ? filters.VideoCompressors[m.Index - 1] : null);
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Video compressor not supported.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }

        }

        private void mnuAudioCompressors_Click(object sender, System.EventArgs e)
        {
            try
            {
                // Change the audio compressor
                // We subtract 1 from m.Index beacuse the first item is (None)
                MenuItem m = sender as MenuItem;
                capture.AudioCompressor = (m.Index > 0 ? filters.AudioCompressors[m.Index - 1] : null);
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Audio compressor not supported.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuVideoSources_Click(object sender, System.EventArgs e)
        {
            try
            {
                // Choose the video source
                // If the device only has one source, this menu item will be disabled
                MenuItem m = sender as MenuItem;
                capture.VideoSource = capture.VideoSources[m.Index];
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable to set video source. Please submit bug report.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuAudioSources_Click(object sender, System.EventArgs e)
        {
            try
            {
                // Choose the audio source
                // If the device only has one source, this menu item will be disabled
                MenuItem m = sender as MenuItem;
                capture.AudioSource = capture.AudioSources[m.Index];
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable to set audio source. Please submit bug report.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }


        private void mnuExit_Click(object sender, System.EventArgs e)
        {
            if (capture != null)
                capture.Stop();
            System.Windows.Forms.Application.Exit();
        }

        private void mnuFrameSizes_Click(object sender, System.EventArgs e)
        {
            try
            {
                // Disable preview to avoid additional flashes (optional)
                bool preview = (capture.PreviewWindow != null);
                capture.PreviewWindow = null;

                // Update the frame size
                MenuItem m = sender as MenuItem;
                string[] s = m.Text.Split('x');
                System.Drawing.Size size = new System.Drawing.Size(int.Parse(s[0]), int.Parse(s[1]));
                capture.FrameSize = size;

                // Update the menu
                updateMenu();

                // Restore previous preview setting
                capture.PreviewWindow = (preview ? panelVideo : null);
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Frame size not supported.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuFrameRates_Click(object sender, System.EventArgs e)
        {
            try
            {
                MenuItem m = sender as MenuItem;
                string[] s = m.Text.Split(' ');
                capture.FrameRate = double.Parse(s[0]);
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Frame rate not supported.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }


        private void mnuAudioChannels_Click(object sender, System.EventArgs e)
        {
            try
            {
                MenuItem m = sender as MenuItem;
                capture.AudioChannels = (short)Math.Pow(2, m.Index);
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Number of audio channels not supported.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuAudioSamplingRate_Click(object sender, System.EventArgs e)
        {
            try
            {
                MenuItem m = sender as MenuItem;
                string[] s = m.Text.Split(' ');
                int samplingRate = (int)(double.Parse(s[0]) * 1000);
                capture.AudioSamplingRate = samplingRate;
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Audio sampling rate not supported.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuAudioSampleSizes_Click(object sender, System.EventArgs e)
        {
            try
            {
                MenuItem m = sender as MenuItem;
                string[] s = m.Text.Split(' ');
                short sampleSize = short.Parse(s[0]);
                capture.AudioSampleSize = sampleSize;
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Audio sample size not supported.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuPreview_Click(object sender, System.EventArgs e)
        {
            try
            {
                if (capture.PreviewWindow == null)
                {
                    capture.PreviewWindow = panelVideo;
                    mnuPreview.Checked = true;
                }
                else
                {
                    capture.PreviewWindow = null;
                    mnuPreview.Checked = false;
                }
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable to enable/disable preview. Please submit a bug report.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuPropertyPages_Click(object sender, System.EventArgs e)
        {
            try
            {
                MenuItem m = sender as MenuItem;
                capture.PropertyPages[m.Index].Show(this);
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable display property page. Please submit a bug report.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuChannel_Click(object sender, System.EventArgs e)
        {
            try
            {
                MenuItem m = sender as MenuItem;
                capture.Tuner.Channel = m.Index + 1;
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable change channel. Please submit a bug report.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuInputType_Click(object sender, System.EventArgs e)
        {
            try
            {
                MenuItem m = sender as MenuItem;
                capture.Tuner.InputType = (TunerInputType)m.Index;
                updateMenu();
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable change tuner input type. Please submit a bug report.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuVideoCaps_Click(object sender, System.EventArgs e)
        {
            try
            {
                string s;
                s = String.Format(
                    "Video Device Capabilities\n" +
                    "--------------------------------\n\n" +
                    "Input Size:\t\t{0} x {1}\n" +
                    "\n" +
                    "Min Frame Size:\t\t{2} x {3}\n" +
                    "Max Frame Size:\t\t{4} x {5}\n" +
                    "Frame Size Granularity X:\t{6}\n" +
                    "Frame Size Granularity Y:\t{7}\n" +
                    "\n" +
                    "Min Frame Rate:\t\t{8:0.000} fps\n" +
                    "Max Frame Rate:\t\t{9:0.000} fps\n",
                    capture.VideoCaps.InputSize.Width, capture.VideoCaps.InputSize.Height,
                    capture.VideoCaps.MinFrameSize.Width, capture.VideoCaps.MinFrameSize.Height,
                    capture.VideoCaps.MaxFrameSize.Width, capture.VideoCaps.MaxFrameSize.Height,
                    capture.VideoCaps.FrameSizeGranularityX,
                    capture.VideoCaps.FrameSizeGranularityY,
                    capture.VideoCaps.MinFrameRate,
                    capture.VideoCaps.MaxFrameRate);
                System.Windows.Forms.MessageBox.Show(s);

            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable display video capabilities. Please submit a bug report.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void mnuAudioCaps_Click(object sender, System.EventArgs e)
        {
            try
            {
                string s;
                s = String.Format(
                    "Audio Device Capabilities\n" +
                    "--------------------------------\n\n" +
                    "Min Channels:\t\t{0}\n" +
                    "Max Channels:\t\t{1}\n" +
                    "Channels Granularity:\t{2}\n" +
                    "\n" +
                    "Min Sample Size:\t\t{3}\n" +
                    "Max Sample Size:\t\t{4}\n" +
                    "Sample Size Granularity:\t{5}\n" +
                    "\n" +
                    "Min Sampling Rate:\t\t{6}\n" +
                    "Max Sampling Rate:\t\t{7}\n" +
                    "Sampling Rate Granularity:\t{8}\n",
                    capture.AudioCaps.MinimumChannels,
                    capture.AudioCaps.MaximumChannels,
                    capture.AudioCaps.ChannelsGranularity,
                    capture.AudioCaps.MinimumSampleSize,
                    capture.AudioCaps.MaximumSampleSize,
                    capture.AudioCaps.SampleSizeGranularity,
                    capture.AudioCaps.MinimumSamplingRate,
                    capture.AudioCaps.MaximumSamplingRate,
                    capture.AudioCaps.SamplingRateGranularity);
                System.Windows.Forms.MessageBox.Show(s);

            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Unable display audio capabilities. Please submit a bug report.\n\n" + ex.Message + "\n\n" + ex.ToString());
            }
        }

        private void OnCaptureComplete(object sender, EventArgs e)
        {
            // Demonstrate the Capture.CaptureComplete event.
            Debug.WriteLine("Capture complete.");
        }

        // 2013-02-05 __ZSC__
        // added palate button and screen capture functionality
        //private void palate_Click(object sender, EventArgs e)
        //{
        //    // date and desktop folder path for saving stuff
        //    //String date_today = DateTime.Now.ToString("yyyy-MM-dd");
        //    //String dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);

        //    ScreenCapture sc = new ScreenCapture();
        //    // capture this window, and save it
        //    //System.Windows.Forms.MessageBox.Show(new_path);
        //    sc.CaptureWindowToFile(this.Handle, new_path + "\\palate_" + date_today + ".jpg", ImageFormat.Jpeg);
        //}

        private void CaptureTest_Load(object sender, EventArgs e)
        {
            // work in progress
            // trying to get window to display to get Subject ID info on window load

            //ExperimentInfo form = new ExperimentInfo();
            //form.Show();
        }

        private void menuRunPostProcess_Click(object sender, EventArgs e)
        {
            // check that capture is not still recording
            if (!btnStart.Enabled)
            {
                //System.Windows.Forms.MessageBox.Show("I have started running the post processing function");

                // catches if people are trying to post-process without recording
                if (capture == null)
                    throw new ApplicationException("Please record something before Post-Processing.");
                // create output folder for frames
                string frame_folder = new_path + @"\" + "frames";
                //System.Windows.Forms.MessageBox.Show("frame_folder: " + frame_folder);
                if (!(System.IO.Directory.Exists(frame_folder)))
                {
                    System.IO.Directory.CreateDirectory(frame_folder);
                    //System.Windows.Forms.MessageBox.Show("created new directory at:\n\n" + frame_folder);
                }

                // run ffmpeg command line script
                Process process = new System.Diagnostics.Process();
                ProcessStartInfo startInfo = new ProcessStartInfo();
                startInfo.WindowStyle = ProcessWindowStyle.Hidden;
                startInfo.FileName = "cmd.exe";
                // moves the coords.txt from desktop to subjectID folder
                string coords_file = "coords.txt";

                string sourceFile = System.IO.Path.Combine(dtopfolder, coords_file);
                string destFile = System.IO.Path.Combine(new_path, coords_file);

                //System.Windows.Forms.MessageBox.Show("sourcefile: " + sourceFile);
                //System.Windows.Forms.MessageBox.Show("destFile: "+destFile);


                if (System.IO.File.Exists(sourceFile))
                {
                    Console.WriteLine("i'll try to move the file");
                    System.IO.File.Move(sourceFile, destFile);
                }
                else
                {
                    System.Windows.Forms.MessageBox.Show("Unable to move coords.txt file.\nFile Not Found!");
                }

                // moves the stimulus file from stim folder to subjectID folder
                string stim_file = "stimulus_response.csv";

                //string stimSource = System.IO.Path.Combine(dtopfolder, "Stimulus Display", stim_file);
                string stimSource = System.IO.Path.Combine(dtopfolder, stim_file);
                string stimDest = System.IO.Path.Combine(new_path, stim_file);

                /*if (System.IO.File.Exists(stimSource))
                {
                    System.IO.File.Move(stimSource, stimDest);
                }
                else
                {
                    System.Windows.Forms.MessageBox.Show("Unable to move stimulus file.\nFile Not Found!");
                }*/

                // extracts video frames and audio file
                startInfo.Arguments = @"/C ffmpeg -i " + video_name + " -r 30000/1001 -qscale 0 -f image2 " + frame_folder + @"\frame-%07d.png -acodec copy " + new_path + @"\" + subjectID + ".wav";
                System.Windows.Forms.MessageBox.Show(startInfo.Arguments.ToString());
                process.StartInfo = startInfo;
                process.Start();

                process.WaitForExit();

                // some way to show when process is done
                System.Windows.Forms.MessageBox.Show("Post-processing has completed");
            }
            else
            {
                System.Windows.Forms.MessageBox.Show("Please STOP recording before Post-Processing");
            }
        }




        //Below are the Kinect modules taken from HDFace Basics, used to interact with the Kinect
        ///// <summary>
        ///// Gets the current collection status
        ///// </summary>
        ///// <param name="status">Status value</param>
        ///// <returns>Status value as text</returns>
        //private static string GetCollectionStatusText(FaceModelBuilderCollectionStatus status)
        //{
        //    string res = string.Empty;

        //    if ((status & FaceModelBuilderCollectionStatus.FrontViewFramesNeeded) != 0)
        //    {
        //        res = "FrontViewFramesNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.LeftViewsNeeded) != 0)
        //    {
        //        res = "LeftViewsNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.RightViewsNeeded) != 0)
        //    {
        //        res = "RightViewsNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.TiltedUpViewsNeeded) != 0)
        //    {
        //        res = "TiltedUpViewsNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.MoreFramesNeeded) != 0)
        //    {
        //        res = "TiltedUpViewsNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.Complete) != 0)
        //    {
        //        res = "Complete";
        //        return res;
        //    }

        //    return res;
        //}


        ///// <summary>
        ///// Start a face capture operation
        ///// </summary>
        //private void StartCapture()
        //{
        //    this.StopFaceCapture();

        //    this.faceModelBuilder = null;

        //    this.faceModelBuilder = this.highDefinitionFaceFrameSource.OpenModelBuilder(FaceModelBuilderAttributes.None);

        //    this.faceModelBuilder.BeginFaceDataCollection();



        //    this.faceModelBuilder.CollectionCompleted += this.HdFaceBuilder_CollectionCompleted;
        //}

        ///// <summary>
        ///// Cancel the current face capture operation
        ///// </summary>
        //private void StopFaceCapture()
        //{
        //    if (this.faceModelBuilder != null)
        //    {
        //        this.faceModelBuilder.Dispose();
        //        this.faceModelBuilder = null;
        //    }
        //}
        ///// This event fires when the face capture operation is completed
        ///// </summary>
        ///// <param name="sender">object sending the event</param>
        ///// <param name="e">event arguments</param>
        //private void HdFaceBuilder_CollectionCompleted(object sender, FaceModelBuilderCollectionCompletedEventArgs e)
        //{
        //    var modelData = e.ModelData;

        //    this.currentFaceModel = modelData.ProduceFaceModel();

        //    this.faceModelBuilder.Dispose();
        //    this.faceModelBuilder = null;

        //    string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
        //    //System.IO.File.WriteAllText(dtopfolder + @"\coords.txt", coordinates);

        //    //this.InternalDispose();
        //    GC.SuppressFinalize(this);

        //    this.CurrentBuilderStatus = "Capture Complete";
        //}

        //private void InitializeHDFace()
        //{
        //    this.CurrentBuilderStatus = "Ready To Start Capture";

        //    this.sensor = KinectSensor.GetDefault();
        //    //this.quat = 
        //    this.bodySource = this.sensor.BodyFrameSource;
        //    this.bodyReader = this.bodySource.OpenReader();
        //    this.bodyReader.FrameArrived += this.BodyReader_FrameArrived;

        //    this.highDefinitionFaceFrameSource = new HighDefinitionFaceFrameSource(this.sensor);
        //    this.highDefinitionFaceFrameSource.TrackingIdLost += this.HdFaceSource_TrackingIdLost;

        //    this.highDefinitionFaceFrameReader = this.highDefinitionFaceFrameSource.OpenReader();
        //    this.highDefinitionFaceFrameReader.FrameArrived += this.HdFaceReader_FrameArrived;

        //    this.currentFaceModel = new FaceModel();
        //    this.currentFaceAlignment = new FaceAlignment();

        //    this.InitializeMesh();
        //    this.UpdateMesh();

        //    this.sensor.Open();
        //}



        /// <summary>
        /// Initializes a new instance of the MainWindow class.
        /// </summary>
        //public MainWindow()
        //{
        //    this.InitializeComponent();
        //    this.DataContext = this;
        //}
        /// <summary>
        /// INotifyPropertyChangedPropertyChanged event to allow window controls to bind to changeable data
        /// </summary>
        //public event PropertyChangedEventHandler PropertyChanged;

        ///// <summary>
        ///// Gets or sets the current status text to display
        ///// </summary>
        //public string StatusText
        //{
        //    get
        //    {
        //        return this.statusText;
        //    }

        //    set
        //    {
        //        if (this.statusText != value)
        //        {
        //            this.statusText = value;

        //            // notify any bound elements that the text has changed
        //            if (this.PropertyChanged != null)
        //            {
        //                this.PropertyChanged(this, new PropertyChangedEventArgs("StatusText"));
        //            }
        //        }
        //    }
        //}

        ///// <summary>
        ///// Gets or sets the current tracked user id
        ///// </summary>
        //private ulong CurrentTrackingId
        //{
        //    get
        //    {
        //        return this.currentTrackingId;
        //    }

        //    set
        //    {
        //        this.currentTrackingId = value;

        //        this.StatusText = this.MakeStatusText();
        //    }
        //}

        ///// <summary>
        ///// Gets or sets the current Face Builder instructions to user
        ///// </summary>
        //private string CurrentBuilderStatus
        //{
        //    get
        //    {
        //        return this.currentBuilderStatus;
        //    }

        //    set
        //    {
        //        this.currentBuilderStatus = value;

        //        this.StatusText = this.MakeStatusText();
        //    }
        //}

        ///// <summary>
        ///// Returns the length of a vector from origin
        ///// </summary>
        ///// <param name="point">Point in space to find it's distance from origin</param>
        ///// <returns>Distance from origin</returns>
        //private static double VectorLength(CameraSpacePoint point)
        //{
        //    var result = Math.Pow(point.X, 2) + Math.Pow(point.Y, 2) + Math.Pow(point.Z, 2);

        //    result = Math.Sqrt(result);

        //    return result;
        //}

        ///// <summary>
        ///// Finds the closest body from the sensor if any
        ///// </summary>
        ///// <param name="bodyFrame">A body frame</param>
        ///// <returns>Closest body, null of none</returns>
        //private static Body FindClosestBody(BodyFrame bodyFrame)
        //{
        //    Body result = null;
        //    double closestBodyDistance = double.MaxValue;

        //    Body[] bodies = new Body[bodyFrame.BodyCount];
        //    bodyFrame.GetAndRefreshBodyData(bodies);

        //    foreach (var body in bodies)
        //    {
        //        if (body.IsTracked)
        //        {
        //            var currentLocation = body.Joints[JointType.SpineBase].Position;

        //            var currentDistance = VectorLength(currentLocation);

        //            if (result == null || currentDistance < closestBodyDistance)
        //            {
        //                result = body;
        //                closestBodyDistance = currentDistance;
        //            }
        //        }
        //    }

        //    return result;
        //}

        ///// <summary>
        ///// Find if there is a body tracked with the given trackingId
        ///// </summary>
        ///// <param name="bodyFrame">A body frame</param>
        ///// <param name="trackingId">The tracking Id</param>
        ///// <returns>The body object, null of none</returns>
        //private static Body FindBodyWithTrackingId(BodyFrame bodyFrame, ulong trackingId)
        //{
        //    Body result = null;

        //    Body[] bodies = new Body[bodyFrame.BodyCount];
        //    bodyFrame.GetAndRefreshBodyData(bodies);

        //    foreach (var body in bodies)
        //    {
        //        if (body.IsTracked)
        //        {
        //            if (body.TrackingId == trackingId)
        //            {
        //                result = body;
        //                break;
        //            }
        //        }
        //    }

        //    return result;
        //}

        ///// <summary>
        ///// Gets the current collection status
        ///// </summary>
        ///// <param name="status">Status value</param>
        ///// <returns>Status value as text</returns>
        //private static string GetCollectionStatusText(FaceModelBuilderCollectionStatus status)
        //{
        //    string res = string.Empty;

        //    if ((status & FaceModelBuilderCollectionStatus.FrontViewFramesNeeded) != 0)
        //    {
        //        res = "FrontViewFramesNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.LeftViewsNeeded) != 0)
        //    {
        //        res = "LeftViewsNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.RightViewsNeeded) != 0)
        //    {
        //        res = "RightViewsNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.TiltedUpViewsNeeded) != 0)
        //    {
        //        res = "TiltedUpViewsNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.MoreFramesNeeded) != 0)
        //    {
        //        res = "TiltedUpViewsNeeded";
        //        return res;
        //    }

        //    if ((status & FaceModelBuilderCollectionStatus.Complete) != 0)
        //    {
        //        res = "Complete";
        //        return res;
        //    }

        //    return res;
        //}

        ///// <summary>
        ///// Helper function to format a status message
        ///// </summary>
        ///// <returns>Status text</returns>
        //private string MakeStatusText()
        //{
        //    string status = string.Format(System.Globalization.CultureInfo.CurrentCulture, "APIL. Builder Status: {0}, Current Tracking ID: {1}", this.CurrentBuilderStatus, this.CurrentTrackingId);

        //    return status;
        //}

        ///// <summary>
        ///// Fires when Window is Loaded
        ///// </summary>
        ///// <param name="sender">object sending the event</param>
        ///// <param name="e">event arguments</param>
        //private void Window_Loaded(object sender, RoutedEventArgs e)
        //{
        //    this.InitializeHDFace();
        //}

        ///// <summary>
        ///// Initialize Kinect object
        ///// </summary>
        //private void InitializeHDFace()
        //{
        //    this.CurrentBuilderStatus = "Ready To Start Capture";

        //    this.sensor = KinectSensor.GetDefault();
        //    //this.quat = 
        //    this.bodySource = this.sensor.BodyFrameSource;
        //    this.bodyReader = this.bodySource.OpenReader();
        //    this.bodyReader.FrameArrived += this.BodyReader_FrameArrived;

        //    this.highDefinitionFaceFrameSource = new HighDefinitionFaceFrameSource(this.sensor);
        //    this.highDefinitionFaceFrameSource.TrackingIdLost += this.HdFaceSource_TrackingIdLost;

        //    this.highDefinitionFaceFrameReader = this.highDefinitionFaceFrameSource.OpenReader();
        //    this.highDefinitionFaceFrameReader.FrameArrived += this.HdFaceReader_FrameArrived;

        //    this.currentFaceModel = new FaceModel();
        //    this.currentFaceAlignment = new FaceAlignment();

        //    this.InitializeMesh();
        //    this.UpdateMesh();

        //    this.sensor.Open();
        //}

        ///// <summary>
        ///// Initializes a 3D mesh to deform every frame
        ///// </summary>
        //private void InitializeMesh()
        //{
        //    var vertices = this.currentFaceModel.CalculateVerticesForAlignment(this.currentFaceAlignment);

        //    var triangleIndices = this.currentFaceModel.TriangleIndices;

        //    var indices = new Int32Collection(triangleIndices.Count);

        //    for (int i = 0; i < triangleIndices.Count; i += 3)
        //    {
        //        uint index01 = triangleIndices[i];
        //        uint index02 = triangleIndices[i + 1];
        //        uint index03 = triangleIndices[i + 2];

        //        indices.Add((int)index03);
        //        indices.Add((int)index02);
        //        indices.Add((int)index01);
        //    }

        //    this.theGeometry.TriangleIndices = indices;
        //    this.theGeometry.Normals = null;
        //    this.theGeometry.Positions = new Point3DCollection();
        //    this.theGeometry.TextureCoordinates = new PointCollection();

        //    foreach (var vert in vertices)
        //    {
        //        this.theGeometry.Positions.Add(new Point3D(vert.X, vert.Y, -vert.Z));
        //        this.theGeometry.TextureCoordinates.Add(new System.Windows.Point());
        //    }

        //}

        ///// <summary>
        ///// Sends the new deformed mesh to be drawn
        ///// </summary>
        //private void UpdateMesh()
        //{
        //    var vertices = this.currentFaceModel.CalculateVerticesForAlignment(this.currentFaceAlignment);


        //    for (int i = 0; i < vertices.Count; i++)
        //    {
        //        var vert = vertices[i];
        //        this.theGeometry.Positions[i] = new Point3D(vert.X, vert.Y, -vert.Z);
        //    }

        //    //Console.WriteLine("Here it is: " + );
        //    //Console.WriteLine("ANGLE: " + Vector4Face);

        //    //Console.WriteLine("AXIS: " + Quaternion.Identity.Axis.ToString());

        //    //Console.WriteLine("W, X, Y, Z : " + Quaternion.Identity.W.ToString() + "," +
        //    //                                    Quaternion.Identity.X.ToString() + "," +
        //    //                                    Quaternion.Identity.Y.ToString() + "," +
        //    //                                    Quaternion.Identity.Z.ToString());

        //    //Console.WriteLine("ANGLE = " + this.quat.Angle);
        //    //Console.WriteLine("ISIDENTITY = " + this.quat.IsIdentity);
        //    //Console.WriteLine("AXIS = " + this.quat.Axis);
        //    //Console.WriteLine("Orientation (deg): (pitch:" + (this.currentFaceAlignment.FaceOrientation.X * 180).ToString() + ", yaw:" +
        //    //                                     (this.currentFaceAlignment.FaceOrientation.Y* 180).ToString() + ", roll:" +
        //    //                                     (this.currentFaceAlignment.FaceOrientation.Z*180).ToString() + ", " +
        //    //                                     (this.currentFaceAlignment.FaceOrientation.W*180).ToString() + ")");
        //    //Console.WriteLine("Pivot (cm): (X:" + (this.currentFaceAlignment.HeadPivotPoint.X*100).ToString() + ", Y:" +
        //    //(this.currentFaceAlignment.HeadPivotPoint.Y*100).ToString() + "Z:, " +
        //    //(this.currentFaceAlignment.HeadPivotPoint.Z*100).ToString() + ")");


        //    double w = this.currentFaceAlignment.FaceOrientation.W;
        //    double x = this.currentFaceAlignment.FaceOrientation.X;
        //    double y = this.currentFaceAlignment.FaceOrientation.Y;
        //    double z = this.currentFaceAlignment.FaceOrientation.Z;

        //    double w2 = Math.Pow(w, 2);
        //    double x2 = Math.Pow(x, 2);
        //    double y2 = Math.Pow(y, 2);
        //    double z2 = Math.Pow(z, 2);

        //    //CHRobot (rolando cited)
        //    //double roll = Math.Atan2(2*(w*x + y*z), (w2-x2-y2+z2));
        //    //double pitch = -1 * (Math.Asin(2*(x*z - w*y)));
        //    //double yaw = Math.Atan2(2*(w*z + x*y), (w2+x2-y2-z2));

        //    //Rolando Pitch
        //    double rpitch = Math.Atan2(2 * (x * y + z * w), 1 - (2 * (y2 + z2)));

        //    //Wikipedia convert quat to euler
        //    //double roll = Math.Atan2(2 * ((w * x) + (y * z)), (1 - (2 * (x2 + y2))));
        //    //double pitch = -1 * (Math.Asin(2 * ((w * y) - (x * z))));
        //    //double yaw = Math.Atan2(2 * ((w * z) + (x * y)), (1 - (2 * (y2 + z2))));

        //    //euclideanspace.com quat to euler
        //    double yaw = Math.Atan2(2 * (y * w) - 2 * (x * z), 1 - (2 * (y2)) - (2 * (z2)));
        //    double roll = Math.Asin(2 * (x * y) + 2 * (z * w));
        //    double pitch = Math.Atan2(2 * (x * w) - 2 * (y * z), 1 - (2 * (x2)) - (2 * (z2)));

        //    yaw = Math.Round((yaw * (180 / Math.PI)) * -1, 1);
        //    pitch = Math.Round(pitch * (180 / Math.PI), 1);
        //    roll = Math.Round((roll * (180 / Math.PI)) * -1, 1);

        //    //Attempt to back-calculate the "angle" variable which 'looks' like it could be used to calc p/y/r
        //    double angle = Math.Acos(w) * 2;
        //    double n_x = x / Math.Sin(0.5 * angle);
        //    double n_z = z / Math.Sin(0.5 * angle);
        //    double n_y = y / Math.Sin(0.5 * angle);

        //    //from math.stackexchange site
        //    //double roll = Math.Atan2(((y * z) + (w * x)), 0.5 - (x2 + y2));
        //    //double pitch = Math.Asin(-2 * ((x * z)+(w * y)));
        //    //double yaw = Math.Atan2((x * y)+(w * z), 0.5 - (y2 + z2));


        //    Console.WriteLine("Pitch: " + pitch.ToString() + " Roll: " + roll.ToString() + " Yaw: " + yaw.ToString());

        //    //Console.WriteLine(quat.ToString());

        //    //date1 = DateTime.Now;

        //    //coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Rotation:    (Pitch: " + ((this.currentFaceAlignment.FaceOrientation.X)*180).ToString();
        //    //coordinates += "°, Yaw: " + ((this.currentFaceAlignment.FaceOrientation.Y)*180).ToString(); 
        //    //coordinates += "°, Roll: " + ((this.currentFaceAlignment.FaceOrientation.Z)*180).ToString();
        //    //coordinates += ")\r\n";
        //    //coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Translation: (Zero point in X-Axis: " + this.currentFaceAlignment.HeadPivotPoint.X.ToString() + " mm, Zero-point in Y-Axis: " + this.currentFaceAlignment.HeadPivotPoint.Y.ToString() + " mm, Distance from Kinect: " + this.currentFaceAlignment.HeadPivotPoint.Z.ToString() + " mm)" + "\r\n";

        //    coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Rotation:    (Pitch: " + pitch.ToString();
        //    coordinates += "°, Yaw: " + yaw.ToString();
        //    coordinates += "°, Roll: " + roll.ToString();
        //    coordinates += ")\r\n";
        //    coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Translation: (Zero point in X-Axis: " + this.currentFaceAlignment.HeadPivotPoint.X.ToString() + " mm, Zero-point in Y-Axis: " + this.currentFaceAlignment.HeadPivotPoint.Y.ToString() + " mm, Distance from Kinect: " + this.currentFaceAlignment.HeadPivotPoint.Z.ToString() + " mm)" + "\r\n";


        //    //Console.WriteLine(coordinates.ToString());

        //    string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
        //    System.IO.File.WriteAllText(dtopfolder + @"\coords.txt", coordinates);
        //}

        ///// <summary>
        ///// Start a face capture on clicking the button
        ///// </summary>
        ///// <param name="sender">object sending the event</param>
        ///// <param name="e">event arguments</param>
        //private void StartCapture_Button_Click(object sender, RoutedEventArgs e)
        //{
        //    this.StartCapture();
        //}

        ///// <summary>
        ///// Disposes this instance and clears the native resources allocated
        ///// </summary>
        //public void Dispose()
        //{
        //    string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
        //    System.IO.File.WriteAllText(dtopfolder + @"\coords.txt", coordinates);

        //    //this.InternalDispose();
        //    GC.SuppressFinalize(this);
        //}


        ///// <summary>
        ///// This event fires when a BodyFrame is ready for consumption
        ///// </summary>
        ///// <param name="sender">object sending the event</param>
        ///// <param name="e">event arguments</param>
        //private void BodyReader_FrameArrived(object sender, BodyFrameArrivedEventArgs e)
        //{
        //    this.CheckOnBuilderStatus();

        //    var frameReference = e.FrameReference;
        //    using (var frame = frameReference.AcquireFrame())
        //    {
        //        if (frame == null)
        //        {
        //            // We might miss the chance to acquire the frame, it will be null if it's missed
        //            return;
        //        }

        //        if (this.currentTrackedBody != null)
        //        {
        //            this.currentTrackedBody = FindBodyWithTrackingId(frame, this.CurrentTrackingId);

        //            if (this.currentTrackedBody != null)
        //            {
        //                return;
        //            }
        //        }

        //        Body selectedBody = FindClosestBody(frame);

        //        if (selectedBody == null)
        //        {
        //            return;
        //        }

        //        this.currentTrackedBody = selectedBody;
        //        this.CurrentTrackingId = selectedBody.TrackingId;

        //        this.highDefinitionFaceFrameSource.TrackingId = this.CurrentTrackingId;
        //    }
        //}

        ///// <summary>
        ///// This event is fired when a tracking is lost for a body tracked by HDFace Tracker
        ///// </summary>
        ///// <param name="sender">object sending the event</param>
        ///// <param name="e">event arguments</param>
        //private void HdFaceSource_TrackingIdLost(object sender, TrackingIdLostEventArgs e)
        //{
        //    var lostTrackingID = e.TrackingId;

        //    if (this.CurrentTrackingId == lostTrackingID)
        //    {
        //        this.CurrentTrackingId = 0;
        //        this.currentTrackedBody = null;
        //        this.isTrackingLabel.Text = "Kinect IS Tracking";
        //        this.isTrackingLabel.BackColor = System.Drawing.Color.Green;
        //        if (this.faceModelBuilder != null)
        //        {
        //            this.faceModelBuilder.Dispose();
        //            this.faceModelBuilder = null;
        //        }

        //        this.highDefinitionFaceFrameSource.TrackingId = 0;
        //    }
        //}

        ///// <summary>
        ///// This event is fired when a new HDFace frame is ready for consumption
        ///// </summary>
        ///// <param name="sender">object sending the event</param>
        ///// <param name="e">event arguments</param>
        //private void HdFaceReader_FrameArrived(object sender, HighDefinitionFaceFrameArrivedEventArgs e)
        //{
        //    using (var frame = e.FrameReference.AcquireFrame())
        //    {
        //        // We might miss the chance to acquire the frame; it will be null if it's missed.
        //        // Also ignore this frame if face tracking failed.
        //        if (frame == null || !frame.IsFaceTracked)
        //        {
        //            return;
        //        }
        //        date1 = DateTime.Now;

        //        //coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Rotation:    (Pitch: " + ((this.currentFaceAlignment.FaceOrientation.X + 1)*180).ToString();
        //        //coordinates += "°, Yaw: " + ((this.currentFaceAlignment.FaceOrientation.Y + 1)*180).ToString();
        //        //coordinates += "°, Roll: " + ((this.currentFaceAlignment.FaceOrientation.Z + 1) * 180).ToString() + ")";
        //        //coordinates += "\r\n";
        //        //coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Translation: (Zero point in X-Axis: " + this.currentFaceAlignment.HeadPivotPoint.X.ToString() + " mm, Zero-point in Y-Axis: " + this.currentFaceAlignment.HeadPivotPoint.Y.ToString() + " mm, Distance from Kinect: " + this.currentFaceAlignment.HeadPivotPoint.Z.ToString() + " mm)" + "\r\n";

        //        //string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
        //        //System.IO.File.WriteAllText(dtopfolder + @"\coords.txt", coordinates);

        //        frame.GetAndRefreshFaceAlignmentResult(this.currentFaceAlignment);

        //        Console.WriteLine(frame.RelativeTime.ToString());

        //        this.UpdateMesh();
        //    }
        //}

        ///// <summary>
        ///// Start a face capture operation
        ///// </summary>
        //private void StartCapture()
        //{
        //    this.StopFaceCapture();

        //    this.faceModelBuilder = null;

        //    this.faceModelBuilder = this.highDefinitionFaceFrameSource.OpenModelBuilder(FaceModelBuilderAttributes.None);

        //    this.faceModelBuilder.BeginFaceDataCollection();



        //    this.faceModelBuilder.CollectionCompleted += this.HdFaceBuilder_CollectionCompleted;
        //}

        ///// <summary>
        ///// Cancel the current face capture operation
        ///// </summary>
        //private void StopFaceCapture()
        //{
        //    if (this.faceModelBuilder != null)
        //    {
        //        this.faceModelBuilder.Dispose();
        //        this.faceModelBuilder = null;
        //    }
        //}

        ///// <summary>
        ///// This event fires when the face capture operation is completed
        ///// </summary>
        ///// <param name="sender">object sending the event</param>
        ///// <param name="e">event arguments</param>
        //private void HdFaceBuilder_CollectionCompleted(object sender, FaceModelBuilderCollectionCompletedEventArgs e)
        //{
        //    var modelData = e.ModelData;

        //    this.currentFaceModel = modelData.ProduceFaceModel();

        //    this.faceModelBuilder.Dispose();
        //    this.faceModelBuilder = null;

        //    string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
        //    System.IO.File.WriteAllText(dtopfolder + @"\coords.txt", coordinates);

        //    //this.InternalDispose();
        //    GC.SuppressFinalize(this);

        //    this.CurrentBuilderStatus = "Capture Complete";
        //}

        ///// <summary>
        ///// Check the face model builder status
        ///// </summary>
        //private void CheckOnBuilderStatus()
        //{
        //    if (this.faceModelBuilder == null)
        //    {
        //        return;
        //    }

        //    string newStatus = string.Empty;

        //    var captureStatus = this.faceModelBuilder.CaptureStatus;
        //    newStatus += captureStatus.ToString();

        //    var collectionStatus = this.faceModelBuilder.CollectionStatus;

        //    newStatus += ", " + GetCollectionStatusText(collectionStatus);

        //    this.CurrentBuilderStatus = newStatus;
        //}


        
    }
}

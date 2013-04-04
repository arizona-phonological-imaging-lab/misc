namespace apil_sound_kinect_matching_b01
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
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
            this.label1 = new System.Windows.Forms.Label();
            this.txt_framesFolder = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
            this.txt_videoFile = new System.Windows.Forms.TextBox();
            this.txt_kinectFile = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.btn_processFiles = new System.Windows.Forms.Button();
            this.txt_output = new System.Windows.Forms.TextBox();
            this.label4 = new System.Windows.Forms.Label();
            this.openFile_vid = new System.Windows.Forms.OpenFileDialog();
            this.openFile_kinect = new System.Windows.Forms.OpenFileDialog();
            this.saveFile_output = new System.Windows.Forms.SaveFileDialog();
            this.btn_vidFile = new System.Windows.Forms.Button();
            this.btn_kinectFile = new System.Windows.Forms.Button();
            this.btn_saveOutput = new System.Windows.Forms.Button();
            this.button1 = new System.Windows.Forms.Button();
            this.label5 = new System.Windows.Forms.Label();
            this.folder_selectFolder = new System.Windows.Forms.FolderBrowserDialog();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(12, 20);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(248, 13);
            this.label1.TabIndex = 0;
            this.label1.Text = "Folder that contains the frames from the ultrasound:";
            // 
            // txt_framesFolder
            // 
            this.txt_framesFolder.Location = new System.Drawing.Point(15, 36);
            this.txt_framesFolder.Name = "txt_framesFolder";
            this.txt_framesFolder.Size = new System.Drawing.Size(355, 20);
            this.txt_framesFolder.TabIndex = 1;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(12, 95);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(73, 13);
            this.label2.TabIndex = 2;
            this.label2.Text = "Video info file:";
            // 
            // txt_videoFile
            // 
            this.txt_videoFile.Location = new System.Drawing.Point(15, 111);
            this.txt_videoFile.Name = "txt_videoFile";
            this.txt_videoFile.Size = new System.Drawing.Size(355, 20);
            this.txt_videoFile.TabIndex = 3;
            // 
            // txt_kinectFile
            // 
            this.txt_kinectFile.Location = new System.Drawing.Point(15, 169);
            this.txt_kinectFile.Name = "txt_kinectFile";
            this.txt_kinectFile.Size = new System.Drawing.Size(355, 20);
            this.txt_kinectFile.TabIndex = 5;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(12, 153);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(76, 13);
            this.label3.TabIndex = 4;
            this.label3.Text = "Kinect info file:";
            // 
            // btn_processFiles
            // 
            this.btn_processFiles.Location = new System.Drawing.Point(497, 197);
            this.btn_processFiles.Name = "btn_processFiles";
            this.btn_processFiles.Size = new System.Drawing.Size(97, 50);
            this.btn_processFiles.TabIndex = 6;
            this.btn_processFiles.Text = "Process file";
            this.btn_processFiles.UseVisualStyleBackColor = true;
            this.btn_processFiles.Click += new System.EventHandler(this.btn_processFiles_Click);
            // 
            // txt_output
            // 
            this.txt_output.Location = new System.Drawing.Point(15, 224);
            this.txt_output.Name = "txt_output";
            this.txt_output.Size = new System.Drawing.Size(355, 20);
            this.txt_output.TabIndex = 8;
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(12, 208);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(58, 13);
            this.label4.TabIndex = 7;
            this.label4.Text = "Output file:";
            // 
            // openFile_vid
            // 
            this.openFile_vid.FileName = "vidTimes.txt";
            this.openFile_vid.Filter = "Text file|*.txt|All files|*.*";
            this.openFile_vid.Title = "Open video information file";
            // 
            // openFile_kinect
            // 
            this.openFile_kinect.FileName = "coords.txt";
            this.openFile_kinect.Filter = "Text files|*.txt|All files|*.*";
            this.openFile_kinect.Title = "Open kinect information file";
            // 
            // saveFile_output
            // 
            this.saveFile_output.FileName = "output.txt";
            this.saveFile_output.Filter = "Text files|*.txt|All files|*.*";
            this.saveFile_output.Title = "Save output file as...";
            // 
            // btn_vidFile
            // 
            this.btn_vidFile.Location = new System.Drawing.Point(376, 111);
            this.btn_vidFile.Name = "btn_vidFile";
            this.btn_vidFile.Size = new System.Drawing.Size(75, 23);
            this.btn_vidFile.TabIndex = 9;
            this.btn_vidFile.Text = "Open";
            this.btn_vidFile.UseVisualStyleBackColor = true;
            this.btn_vidFile.Click += new System.EventHandler(this.btn_vidFile_Click);
            // 
            // btn_kinectFile
            // 
            this.btn_kinectFile.Location = new System.Drawing.Point(376, 169);
            this.btn_kinectFile.Name = "btn_kinectFile";
            this.btn_kinectFile.Size = new System.Drawing.Size(75, 23);
            this.btn_kinectFile.TabIndex = 10;
            this.btn_kinectFile.Text = "Open";
            this.btn_kinectFile.UseVisualStyleBackColor = true;
            this.btn_kinectFile.Click += new System.EventHandler(this.btn_kinectFile_Click);
            // 
            // btn_saveOutput
            // 
            this.btn_saveOutput.Location = new System.Drawing.Point(376, 224);
            this.btn_saveOutput.Name = "btn_saveOutput";
            this.btn_saveOutput.Size = new System.Drawing.Size(75, 23);
            this.btn_saveOutput.TabIndex = 11;
            this.btn_saveOutput.Text = "Save as...";
            this.btn_saveOutput.UseVisualStyleBackColor = true;
            this.btn_saveOutput.Click += new System.EventHandler(this.btn_saveOutput_Click);
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(377, 36);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(75, 23);
            this.button1.TabIndex = 12;
            this.button1.Text = "Select folder";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(14, 58);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(348, 13);
            this.label5.TabIndex = 13;
            this.label5.Text = "(The program will count all of the \"frame*.*\" files in the folder you specify)";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(620, 271);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.btn_saveOutput);
            this.Controls.Add(this.btn_kinectFile);
            this.Controls.Add(this.btn_vidFile);
            this.Controls.Add(this.txt_output);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.btn_processFiles);
            this.Controls.Add(this.txt_kinectFile);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.txt_videoFile);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.txt_framesFolder);
            this.Controls.Add(this.label1);
            this.Name = "Form1";
            this.Text = "APIL Kinect-Video post-processing system (20130222)";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox txt_framesFolder;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox txt_videoFile;
        private System.Windows.Forms.TextBox txt_kinectFile;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Button btn_processFiles;
        private System.Windows.Forms.TextBox txt_output;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.OpenFileDialog openFile_vid;
        private System.Windows.Forms.OpenFileDialog openFile_kinect;
        private System.Windows.Forms.SaveFileDialog saveFile_output;
        private System.Windows.Forms.Button btn_vidFile;
        private System.Windows.Forms.Button btn_kinectFile;
        private System.Windows.Forms.Button btn_saveOutput;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.FolderBrowserDialog folder_selectFolder;
    }
}


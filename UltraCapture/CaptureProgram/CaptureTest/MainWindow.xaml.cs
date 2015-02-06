// --------------------------------------------------------------------------------------------------------------------
// <copyright file="MainWindow.xaml.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>
// --------------------------------------------------------------------------------------------------------------------

namespace Microsoft.Samples.Kinect.HDFaceBasics
{
    using System;
    using System.ComponentModel;
    using System.Windows;
    using System.Windows.Media;
    using System.Windows.Media.Media3D;
    using Microsoft.Kinect;
    using Microsoft.Kinect.Face;

    /// <summary>
    /// Main Window
    /// </summary>
    public partial class MainWindow : Window, INotifyPropertyChanged
    {
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

        private Quaternion quat;

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

        /// <summary>
        /// Initializes a new instance of the MainWindow class.
        /// </summary>
        public MainWindow()
        {
            this.InitializeComponent();
            this.DataContext = this;
        }

        /// <summary>
        /// INotifyPropertyChangedPropertyChanged event to allow window controls to bind to changeable data
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary>
        /// Gets or sets the current status text to display
        /// </summary>
        public string StatusText
        {
            get
            {
                return this.statusText;
            }

            set
            {
                if (this.statusText != value)
                {
                    this.statusText = value;

                    // notify any bound elements that the text has changed
                    if (this.PropertyChanged != null)
                    {
                        this.PropertyChanged(this, new PropertyChangedEventArgs("StatusText"));
                    }
                }
            }
        }
        
        /// <summary>
        /// Gets or sets the current tracked user id
        /// </summary>
        private ulong CurrentTrackingId
        {
            get
            {
                return this.currentTrackingId;
            }

            set
            {
                this.currentTrackingId = value;

                this.StatusText = this.MakeStatusText();
            }
        }

        /// <summary>
        /// Gets or sets the current Face Builder instructions to user
        /// </summary>
        private string CurrentBuilderStatus
        {
            get
            {
                return this.currentBuilderStatus;
            }

            set
            {
                this.currentBuilderStatus = value;

                this.StatusText = this.MakeStatusText();
            }
        }

        /// <summary>
        /// Returns the length of a vector from origin
        /// </summary>
        /// <param name="point">Point in space to find it's distance from origin</param>
        /// <returns>Distance from origin</returns>
        private static double VectorLength(CameraSpacePoint point)
        {
            var result = Math.Pow(point.X, 2) + Math.Pow(point.Y, 2) + Math.Pow(point.Z, 2);

            result = Math.Sqrt(result);

            return result;
        }

        /// <summary>
        /// Finds the closest body from the sensor if any
        /// </summary>
        /// <param name="bodyFrame">A body frame</param>
        /// <returns>Closest body, null of none</returns>
        private static Body FindClosestBody(BodyFrame bodyFrame)
        {
            Body result = null;
            double closestBodyDistance = double.MaxValue;

            Body[] bodies = new Body[bodyFrame.BodyCount];
            bodyFrame.GetAndRefreshBodyData(bodies);

            foreach (var body in bodies)
            {
                if (body.IsTracked)
                {
                    var currentLocation = body.Joints[JointType.SpineBase].Position;

                    var currentDistance = VectorLength(currentLocation);

                    if (result == null || currentDistance < closestBodyDistance)
                    {
                        result = body;
                        closestBodyDistance = currentDistance;
                    }
                }
            }

            return result;
        }

        /// <summary>
        /// Find if there is a body tracked with the given trackingId
        /// </summary>
        /// <param name="bodyFrame">A body frame</param>
        /// <param name="trackingId">The tracking Id</param>
        /// <returns>The body object, null of none</returns>
        private static Body FindBodyWithTrackingId(BodyFrame bodyFrame, ulong trackingId)
        {
            Body result = null;

            Body[] bodies = new Body[bodyFrame.BodyCount];
            bodyFrame.GetAndRefreshBodyData(bodies);

            foreach (var body in bodies)
            {
                if (body.IsTracked)
                {
                    if (body.TrackingId == trackingId)
                    {
                        result = body;
                        break;
                    }
                }
            }

            return result;
        }

        /// <summary>
        /// Gets the current collection status
        /// </summary>
        /// <param name="status">Status value</param>
        /// <returns>Status value as text</returns>
        private static string GetCollectionStatusText(FaceModelBuilderCollectionStatus status)
        {
            string res = string.Empty;

            if ((status & FaceModelBuilderCollectionStatus.FrontViewFramesNeeded) != 0)
            {
                res = "FrontViewFramesNeeded";
                return res;
            }

            if ((status & FaceModelBuilderCollectionStatus.LeftViewsNeeded) != 0)
            {
                res = "LeftViewsNeeded";
                return res;
            }

            if ((status & FaceModelBuilderCollectionStatus.RightViewsNeeded) != 0)
            {
                res = "RightViewsNeeded";
                return res;
            }

            if ((status & FaceModelBuilderCollectionStatus.TiltedUpViewsNeeded) != 0)
            {
                res = "TiltedUpViewsNeeded";
                return res;
            }

            if ((status & FaceModelBuilderCollectionStatus.MoreFramesNeeded) != 0)
            {
                res = "TiltedUpViewsNeeded";
                return res;
            }

            if ((status & FaceModelBuilderCollectionStatus.Complete) != 0)
            {
                res = "Complete";
                return res;
            }

            return res;
        }

        /// <summary>
        /// Helper function to format a status message
        /// </summary>
        /// <returns>Status text</returns>
        private string MakeStatusText()
        {
            string status = string.Format(System.Globalization.CultureInfo.CurrentCulture, "APIL. Builder Status: {0}, Current Tracking ID: {1}", this.CurrentBuilderStatus, this.CurrentTrackingId);

            return status;
        }

        /// <summary>
        /// Fires when Window is Loaded
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        public void Window_Loaded(object sender, RoutedEventArgs e)
        {
            this.InitializeHDFace();
        }

        /// <summary>
        /// Initialize Kinect object
        /// </summary>
        public void InitializeHDFace()
        {
            Console.WriteLine("INSIDDDDEEE!!!!");
            this.CurrentBuilderStatus = "Ready To Start Capture";

            this.sensor = KinectSensor.GetDefault();
            
            this.bodySource = this.sensor.BodyFrameSource;
            this.bodyReader = this.bodySource.OpenReader();
            this.bodyReader.FrameArrived += this.BodyReader_FrameArrived;

            this.highDefinitionFaceFrameSource = new HighDefinitionFaceFrameSource(this.sensor);
            this.highDefinitionFaceFrameSource.TrackingIdLost += this.HdFaceSource_TrackingIdLost;

            this.highDefinitionFaceFrameReader = this.highDefinitionFaceFrameSource.OpenReader();
            this.highDefinitionFaceFrameReader.FrameArrived += this.HdFaceReader_FrameArrived;

            this.currentFaceModel = new FaceModel();
            this.currentFaceAlignment = new FaceAlignment();
            Console.WriteLine("prior to initializing mesh");
            this.InitializeMesh();
            Console.WriteLine("in-BETWEEN");
            this.UpdateMesh();

            Console.WriteLine("after updating mesh");

            this.sensor.Open();
        }

        /// <summary>
        /// Initializes a 3D mesh to deform every frame
        /// </summary>
        private void InitializeMesh()
        {
            var vertices = this.currentFaceModel.CalculateVerticesForAlignment(this.currentFaceAlignment);

            var triangleIndices = this.currentFaceModel.TriangleIndices;

            var indices = new Int32Collection(triangleIndices.Count);

            for (int i = 0; i < triangleIndices.Count; i += 3)
            {
                uint index01 = triangleIndices[i];
                uint index02 = triangleIndices[i + 1];
                uint index03 = triangleIndices[i + 2];

                indices.Add((int)index03);
                indices.Add((int)index02);
                indices.Add((int)index01);
            }

            this.theGeometry.TriangleIndices = indices;
            this.theGeometry.Normals = null;
            this.theGeometry.Positions = new Point3DCollection();
            this.theGeometry.TextureCoordinates = new PointCollection();

            foreach (var vert in vertices)
            {
                this.theGeometry.Positions.Add(new Point3D(vert.X, vert.Y, -vert.Z));
                this.theGeometry.TextureCoordinates.Add(new Point());
            }

        }

        /// <summary>
        /// Sends the new deformed mesh to be drawn
        /// </summary>
        private void UpdateMesh()
        {
            var vertices = this.currentFaceModel.CalculateVerticesForAlignment(this.currentFaceAlignment);
            Console.WriteLine("HAHAHAHAHA");

            for (int i = 0; i < vertices.Count; i++)
            {
                var vert = vertices[i];
                this.theGeometry.Positions[i] = new Point3D(vert.X, vert.Y, -vert.Z);
            }

            //Console.WriteLine("Here it is: " + );
            //Console.WriteLine("ANGLE: " + Vector4Face);

            //Console.WriteLine("AXIS: " + Quaternion.Identity.Axis.ToString());

            //Console.WriteLine("W, X, Y, Z : " + Quaternion.Identity.W.ToString() + "," +
            //                                    Quaternion.Identity.X.ToString() + "," +
            //                                    Quaternion.Identity.Y.ToString() + "," +
            //                                    Quaternion.Identity.Z.ToString());

            //Console.WriteLine("ANGLE = " + this.quat.Angle);
            //Console.WriteLine("ISIDENTITY = " + this.quat.IsIdentity);
            //Console.WriteLine("AXIS = " + this.quat.Axis);
            //Console.WriteLine("Orientation (deg): (pitch:" + (this.currentFaceAlignment.FaceOrientation.X * 180).ToString() + ", yaw:" +
            //                                     (this.currentFaceAlignment.FaceOrientation.Y* 180).ToString() + ", roll:" +
            //                                     (this.currentFaceAlignment.FaceOrientation.Z*180).ToString() + ", " +
            //                                     (this.currentFaceAlignment.FaceOrientation.W*180).ToString() + ")");
            //Console.WriteLine("Pivot (cm): (X:" + (this.currentFaceAlignment.HeadPivotPoint.X*100).ToString() + ", Y:" +
                                                 //(this.currentFaceAlignment.HeadPivotPoint.Y*100).ToString() + "Z:, " +
                                                 //(this.currentFaceAlignment.HeadPivotPoint.Z*100).ToString() + ")");


            double w = this.currentFaceAlignment.FaceOrientation.W;
            double x = this.currentFaceAlignment.FaceOrientation.X;
            double y = this.currentFaceAlignment.FaceOrientation.Y;
            double z = this.currentFaceAlignment.FaceOrientation.Z;

            double w2 = Math.Pow(w,2);
            double x2 = Math.Pow(x,2);
            double y2 = Math.Pow(y,2);
            double z2 = Math.Pow(z,2);

            //CHRobot (rolando cited)
            //double roll = Math.Atan2(2*(w*x + y*z), (w2-x2-y2+z2));
            //double pitch = -1 * (Math.Asin(2*(x*z - w*y)));
            //double yaw = Math.Atan2(2*(w*z + x*y), (w2+x2-y2-z2));

            //Rolando Pitch
            double rpitch = Math.Atan2(2 * (x * y + z * w), 1 - (2 * (y2 + z2)));

            //Wikipedia convert quat to euler
            //double roll = Math.Atan2(2 * ((w * x) + (y * z)), (1 - (2 * (x2 + y2))));
            //double pitch = -1 * (Math.Asin(2 * ((w * y) - (x * z))));
            //double yaw = Math.Atan2(2 * ((w * z) + (x * y)), (1 - (2 * (y2 + z2))));

            //euclideanspace.com quat to euler
            double yaw = Math.Atan2(2 * (y * w) - 2 * (x * z), 1 - (2 * (y2)) - (2 * (z2)));
            double roll = Math.Asin(2 * (x * y) + 2 * (z * w));
            double pitch = Math.Atan2(2 * (x * w) - 2 * (y * z), 1 - (2 * (x2)) - (2 * (z2)));

            yaw = Math.Round((yaw * (180/Math.PI))*-1,1);
            pitch = Math.Round(pitch * (180 / Math.PI),1);
            roll = Math.Round((roll * (180 / Math.PI))*-1,1);

            //Attempt to back-calculate the "angle" variable which 'looks' like it could be used to calc p/y/r
            double angle = Math.Acos(w) * 2;
            double n_x = x/Math.Sin(0.5*angle);
            double n_z = z/Math.Sin(0.5*angle);
            double n_y = y/Math.Sin(0.5*angle);

            //from math.stackexchange site
            //double roll = Math.Atan2(((y * z) + (w * x)), 0.5 - (x2 + y2));
            //double pitch = Math.Asin(-2 * ((x * z)+(w * y)));
            //double yaw = Math.Atan2((x * y)+(w * z), 0.5 - (y2 + z2));
            

            Console.WriteLine("Pitch: " + pitch.ToString() + " Roll: "+roll.ToString()+" Yaw: "+yaw.ToString());

            //Console.WriteLine(quat.ToString());

            //date1 = DateTime.Now;

            //coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Rotation:    (Pitch: " + ((this.currentFaceAlignment.FaceOrientation.X)*180).ToString();
            //coordinates += "°, Yaw: " + ((this.currentFaceAlignment.FaceOrientation.Y)*180).ToString(); 
            //coordinates += "°, Roll: " + ((this.currentFaceAlignment.FaceOrientation.Z)*180).ToString();
            //coordinates += ")\r\n";
            //coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Translation: (Zero point in X-Axis: " + this.currentFaceAlignment.HeadPivotPoint.X.ToString() + " mm, Zero-point in Y-Axis: " + this.currentFaceAlignment.HeadPivotPoint.Y.ToString() + " mm, Distance from Kinect: " + this.currentFaceAlignment.HeadPivotPoint.Z.ToString() + " mm)" + "\r\n";

            coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Rotation:    (Pitch: " + pitch.ToString();
            coordinates += "°, Yaw: " + yaw.ToString();
            coordinates += "°, Roll: " + roll.ToString();
            coordinates += ")\r\n";
            coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Translation: (Zero point in X-Axis: " + this.currentFaceAlignment.HeadPivotPoint.X.ToString() + " mm, Zero-point in Y-Axis: " + this.currentFaceAlignment.HeadPivotPoint.Y.ToString() + " mm, Distance from Kinect: " + this.currentFaceAlignment.HeadPivotPoint.Z.ToString() + " mm)" + "\r\n";


            //Console.WriteLine(coordinates.ToString());

            string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
            System.IO.File.WriteAllText(dtopfolder + @"\coords.txt", coordinates);
        }

        /// <summary>
        /// Start a face capture on clicking the button
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void StartCapture_Button_Click(object sender, RoutedEventArgs e)
        {
            this.StartCapture();
        }

        /// <summary>
        /// Disposes this instance and clears the native resources allocated
        /// </summary>
        public void Dispose()
        {
            string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
            System.IO.File.WriteAllText(dtopfolder + @"\coords.txt", coordinates);

            //this.InternalDispose();
            GC.SuppressFinalize(this);
        }


        /// <summary>
        /// This event fires when a BodyFrame is ready for consumption
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void BodyReader_FrameArrived(object sender, BodyFrameArrivedEventArgs e)
        {
            this.CheckOnBuilderStatus();

            var frameReference = e.FrameReference;
            using (var frame = frameReference.AcquireFrame())
            {
                if (frame == null)
                {
                    // We might miss the chance to acquire the frame, it will be null if it's missed
                    return;
                }

                if (this.currentTrackedBody != null)
                {
                    this.currentTrackedBody = FindBodyWithTrackingId(frame, this.CurrentTrackingId);

                    if (this.currentTrackedBody != null)
                    {
                        return;
                    }
                }

                Body selectedBody = FindClosestBody(frame);

                if (selectedBody == null)
                {
                    return;
                }

                this.currentTrackedBody = selectedBody;
                this.CurrentTrackingId = selectedBody.TrackingId;

                this.highDefinitionFaceFrameSource.TrackingId = this.CurrentTrackingId;
            }
        }
        
        /// <summary>
        /// This event is fired when a tracking is lost for a body tracked by HDFace Tracker
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void HdFaceSource_TrackingIdLost(object sender, TrackingIdLostEventArgs e)
        {
            var lostTrackingID = e.TrackingId;

            if (this.CurrentTrackingId == lostTrackingID)
            {
                this.CurrentTrackingId = 0;
                this.currentTrackedBody = null;
                if (this.faceModelBuilder != null)
                {
                    this.faceModelBuilder.Dispose();
                    this.faceModelBuilder = null;
                }

                this.highDefinitionFaceFrameSource.TrackingId = 0;
            }
        }

        /// <summary>
        /// This event is fired when a new HDFace frame is ready for consumption
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void HdFaceReader_FrameArrived(object sender, HighDefinitionFaceFrameArrivedEventArgs e)
        {
            using (var frame = e.FrameReference.AcquireFrame())
            {
                // We might miss the chance to acquire the frame; it will be null if it's missed.
                // Also ignore this frame if face tracking failed.
                if (frame == null || !frame.IsFaceTracked)
                {
                    return;
                }
                date1 = DateTime.Now;

                //coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Rotation:    (Pitch: " + ((this.currentFaceAlignment.FaceOrientation.X + 1)*180).ToString();
                //coordinates += "°, Yaw: " + ((this.currentFaceAlignment.FaceOrientation.Y + 1)*180).ToString();
                //coordinates += "°, Roll: " + ((this.currentFaceAlignment.FaceOrientation.Z + 1) * 180).ToString() + ")";
                //coordinates += "\r\n";
                //coordinates += "Time: " + date1.ToString("yyyyyyyyMMddHHmmssfff") + " Translation: (Zero point in X-Axis: " + this.currentFaceAlignment.HeadPivotPoint.X.ToString() + " mm, Zero-point in Y-Axis: " + this.currentFaceAlignment.HeadPivotPoint.Y.ToString() + " mm, Distance from Kinect: " + this.currentFaceAlignment.HeadPivotPoint.Z.ToString() + " mm)" + "\r\n";

                //string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
                //System.IO.File.WriteAllText(dtopfolder + @"\coords.txt", coordinates);

                frame.GetAndRefreshFaceAlignmentResult(this.currentFaceAlignment);

                Console.WriteLine(frame.RelativeTime.ToString());

                this.UpdateMesh();
            }
        }

        /// <summary>
        /// Start a face capture operation
        /// </summary>
        private void StartCapture()
        {
            this.StopFaceCapture();

            this.faceModelBuilder = null;

            this.faceModelBuilder = this.highDefinitionFaceFrameSource.OpenModelBuilder(FaceModelBuilderAttributes.None);

            this.faceModelBuilder.BeginFaceDataCollection();

            

            this.faceModelBuilder.CollectionCompleted += this.HdFaceBuilder_CollectionCompleted;
        }

        /// <summary>
        /// Cancel the current face capture operation
        /// </summary>
        private void StopFaceCapture()
        {
            if (this.faceModelBuilder != null)
            {
                this.faceModelBuilder.Dispose();
                this.faceModelBuilder = null;
            }
        }

        /// <summary>
        /// This event fires when the face capture operation is completed
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void HdFaceBuilder_CollectionCompleted(object sender, FaceModelBuilderCollectionCompletedEventArgs e)
        {
            var modelData = e.ModelData;

            this.currentFaceModel = modelData.ProduceFaceModel();

            this.faceModelBuilder.Dispose();
            this.faceModelBuilder = null;

            string dtopfolder = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
            System.IO.File.WriteAllText(dtopfolder + @"\coords.txt", coordinates);

            //this.InternalDispose();
            GC.SuppressFinalize(this);

            this.CurrentBuilderStatus = "Capture Complete";
        }

        /// <summary>
        /// Check the face model builder status
        /// </summary>
        private void CheckOnBuilderStatus()
        {
            if (this.faceModelBuilder == null)
            {
                return;
            }

            string newStatus = string.Empty;

            var captureStatus = this.faceModelBuilder.CaptureStatus;
            newStatus += captureStatus.ToString();

            var collectionStatus = this.faceModelBuilder.CollectionStatus;

            newStatus += ", " + GetCollectionStatusText(collectionStatus);

            this.CurrentBuilderStatus = newStatus;
        }
    }
}
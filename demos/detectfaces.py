import cv2

face_cascade = cv2.CascadeClassifier('/usr/local/share/OpenCV/haarcascades/haarcascade_frontalface_default.xml')
eye_cascade = cv2.CascadeClassifier('/usr/local/share/OpenCV/haarcascades/haarcascade_eye.xml')
mouth_cascade = cv2.CascadeClassifier('/usr/local/share/OpenCV/haarcascades/haarcascade_mcs_mouth.xml')

#/usr/local/share/OpenCV/haarcascades

#facial_component = face_cascade
#cv2.namedWindow("preview")

def detect_component(img, facial_component=face_cascade):

  colors = {"green":(0, 255, 0),
            "blue": (255, 0, 0)}

  def draw_detected_features(image, detections, color=None):
    color = colors.get(color,(0, 255, 0))
    for (x, y, w, h) in detections:
      #print "({0}, {1}, {2}, {3})".format(x, y, w, h)
      cv2.rectangle(image, (x, y), (x + w, y + h), color, 2)


  gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
  components = facial_component.detectMultiScale(
               gray,
               scaleFactor=1.1,
               minNeighbors=5,
               minSize=(30, 30),
               flags = cv2.cv.CV_HAAR_SCALE_IMAGE
               )

  facial_components = face_cascade.detectMultiScale(
             gray,
             scaleFactor=1.1,
             minNeighbors=5,
             minSize=(30, 30),
             flags = cv2.cv.CV_HAAR_SCALE_IMAGE
             )
  try:
      draw_detected_features(img, components, 'blue')
      draw_detected_features(img, facial_components, 'green')
  except Exception as e:
      pass

  return img

#cv2.imshow("preview", img)

def display_video(component=face_cascade):
  components = {"face": face_cascade,
                "eye": eye_cascade,
                "mouth": mouth_cascade}

  #assume a face detector
  detector = components.get(component, face_cascade)

  camera = cv2.VideoCapture(0)
  exit_keys = [27, ord('q')]
  #cv2.namedWindow("live-feed")
  while True:
    try:
      _, poo = camera.read()
      poo = cv2.flip(poo,1)
      img = detect_component(poo, detector)
      cv2.imshow("", img)
      if cv2.waitKey(50) in exit_keys:
          return False
      #if key in [27, ord('Q'), ord('q')]: # exit on ESC
        #break
    except Exception as e:
      #print "dropped frame..."
      pass

if __name__ == "__main__":
    display_video("eye")

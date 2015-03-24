#!/usr/bin/python
# -*- coding: utf-8 -*-

import edgetrak_converter                                                       # this script (edgetrak_converter.py) should be in 
                                                                                # same dir as GUI script (edgetrak_converter_GUI.py)
import sys
from PyQt4 import QtGui

class edgetrak_converter_GUI(QtGui.QMainWindow):
    ''' This script (1) generates a GUI dialog to ask for a user's local 
        existing directory, and then (2) converts the contents of that dir to 
        output usable by AutoTrace. The dir contents are assumed to be one 
        *.con file and multiple images (tongue frames).'''

    def __init__(self):
        super(edgetrak_converter_GUI, self).__init__()                          # make main window
        self.dirOfInterest = str(QtGui.QFileDialog.getExistingDirectory(self, 
                               "Select folder containing *.con file + images")) # make dialog and select dir
        edgetrak_converter.Converter().main(folder=self.dirOfInterest)          # convert contents of dir
        self.close()                                                            # shut it all down

def main():
    app = QtGui.QApplication(sys.argv)
    conversionGUI = edgetrak_converter_GUI()
    conversionGUI.destroy()
    sys.exit()

if __name__ == '__main__':
    main()

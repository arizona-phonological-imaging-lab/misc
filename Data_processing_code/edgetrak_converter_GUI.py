#!/usr/bin/python
# -*- coding: utf-8 -*-

from edgetrak_converter import Converter
import sys
from PyQt4 import QtGui, QtCore

class Converter_GUI(QtGui.QWidget):
    def __init__(self):
        self.createMasterWidget()
        self.createButton()                                 # create button
        self.createTextbox()                                # create textbox
        self.show()

    def createMasterWidget(self):
        super(Converter_GUI, self).__init__()                   # create master widget
        self.setGeometry(300, 300, 300, 150)
        self.setWindowTitle('EdgeTrak File Conversion')
        self.label = QtGui.QLabel(self)
        self.label.setText('Path to Folder:')
        self.label.move(20,25)

    def createButton(self):
        myButton = QtGui.QPushButton('Convert', self)       # create the button with text 'Convert'
        myButton.clicked.connect(self.buttonCommand)        # what to do if button gets clicked
        myButton.move(100, 80)                              # place the textbox x,y pixels from center

    def buttonCommand(self):
        Converter().main(folder=str(self.folderPath))
        self.close()

    def createTextbox(self):
        myTextbox = QtGui.QLineEdit(self)                          # editable textbox (user can undo, redo, cut, paste, and drag and drop text)
        myTextbox.textChanged[str].connect(self.textboxCommand)    # detect when the user enters text
        myTextbox.move(150, 20)                                   # place the textbox x,y pixels from center

    def textboxCommand(self, text):
        self.folderPath = text


def main():
    app = QtGui.QApplication(sys.argv)      # create app
    conv = Converter_GUI()
    conv.show()
    sys.exit(app.exec_())


if __name__ == '__main__':
    main()


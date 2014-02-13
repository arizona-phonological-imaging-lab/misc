import Tkinter
import tkFileDialog as td
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure

class App:
    def __init__(self, master, fname, increment=.2, height=10):
        #self.rankings =
        self.increment = increment
        # Create a container
        frame = Tkinter.Frame(master)
        # Make buttons...
        self.button_left = Tkinter.Button(frame,text="< Move Left",
                                        command=self.move_left)
        self.button_left.pack(side="left")
        self.button_right = Tkinter.Button(frame,text="Move Right >",
                                        command=self.move_right)
        self.button_right.pack(side="left")

        fig = Figure()
        ax = fig.add_subplot(111)
        ax.set_xlim([-5, 5])
        x = [3]*height
        y = range(height)
        #so that it's a tuple
        self.line, = ax.plot(x, y)
        self.canvas = FigureCanvasTkAgg(fig, master=master)
        self.canvas.show()
        self.canvas.get_tk_widget().pack(side='top', fill='both', expand=1)
        frame.pack()

    def move_left(self):
        x, y = self.line.get_data()
        self.line.set_xdata(x-self.increment)
        x, y = self.line.get_data()
        print "x: {0}".format(x)
        print "y: {0}".format(y)
        self.canvas.draw()


    def move_right(self):
        x, y = self.line.get_data()
        self.line.set_xdata(x+self.increment)
        x, y = self.line.get_data()
        print "x: {0}".format(x)
        print "y: {0}".format(y)
        self.canvas.draw()

root = Tkinter.Tk()
filename = td.askopenfilename(title='Select Sorted Results file', filetypes=[("Text files", ".txt")])
app = App(root,filename)
root.mainloop()

def load_file(self):

    filename = filedialog.askopenfilename(filetypes = (("Template files", "*.tplate")
                                                         ,("HTML files", "*.html;*.htm")
                                                         ,("All files", "*.*") ))
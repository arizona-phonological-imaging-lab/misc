from Tkinter import *
#import Image, ImageTk
import os

#home = '/'.join([os.path.expanduser("~"), "Gus"])
home = os.getcwd()

class PaintBox(Frame): #from Tkinter
	def __init__(self, parent, point_size=3):
		Frame.__init__(self, parent)
		self.parent = parent

		#font size for dots
		self.point_size = point_size

		self.coordinates = set()
		self.initUI()

	def initUI(self):
		self.parent.title("Stinky shoes")
		self.parent.geometry("700x550")
		self.pack(expand = YES, fill = BOTH )

		self.message = Label(self, text = "Drag the mouse to draw" )
		self.message.pack(side = BOTTOM)

		self.myCanvas = Canvas(self)
		self.myCanvas.pack(expand = YES, fill = BOTH)

		self.photo = PhotoImage(file = '/'.join([home, 'poop.gif']))

		self.myCanvas.create_image(50,50, image = self.photo, anchor = NW)

		#buttons
		button1 = Button(self, text = "Draw Grid", command = self.draw_lines, anchor = W)
		button1.configure(width = 10, activebackground = "#33B5E5", relief = FLAT)
		button1_window = self.myCanvas.create_window(10, 10, anchor=NW, window=button1)

		button2 = Button(self, text = "Erase all", command = self.erase_all, anchor = E)
		button2.configure(width = 10, activebackground = "#33B5E5", relief = FLAT)
		#button2_window = self.myCanvas.create_window(10, 100, anchor=E, window=button2)
		button2.place(relx=0.85, rely=0.95, anchor=NE)
		
		#events
		self.myCanvas.bind("<B1-Motion>", self.paint)
		self.myCanvas.bind("<B2-Motion>", self.erase)
		self.myCanvas.bind("<Button-2>", self.erase)
		self.myCanvas.mainloop() #necessary to display image 
		
	def paint(self, event):
		"""
		Pait ovals to screen
		"""
		x1, y1 = (event.x - self.point_size), (event.y - self.point_size)
		x2, y2 = (event.x + self.point_size), (event.y + self.point_size)
		coordinate_id = self.myCanvas.create_oval( x1, y1, x2, y2, fill = "red" )
		self.coordinates.add(coordinate_id)
		print "{0}: {1} {2}".format("x1 & y1", x2, y2)
		#print "{0}: {1} {2}".format("x2 & y2", x2, y2)

	def erase_all(self):
		"""
		Erase all elements in set
		"""
		for c in self.coordinates:
			self.myCanvas.delete(c)
		self.coordinates.clear()

	def erase(self, event):
		print "trying to erase something..."
		x1, y1 = (event.x - self.point_size), (event.y - self.point_size)
		x2, y2 = (event.x + self.point_size), (event.y + self.point_size)
		to_remove = self.myCanvas.find_closest((x1+x2)/2, (y1+y2)/2)
		if to_remove:
			try:
				print "To remove: {0}".format(to_remove)
				for item in to_remove: 
					self.coordinates.remove(item)
					self.myCanvas.delete(item)
				print "Deleted {0}!".format(item)
			except:
				print "Nothing there..."

	def draw_lines(self, event):
		pass
		
def main():
  
    root = Tk()
    #root.geometry("250x150+300+300")
    app = PaintBox(root)
    root.mainloop()  


if __name__ == '__main__':
    main()  
"""  
canvas = Canvas(width=300, height=300, bg='white')
canvas.pack(expand=YES, fill=BOTH)                
     
photo=PhotoImage(file='/'.join([home, 'Gus/poop.gif']))
canvas.create_image(250, 0, image=photo, anchor=NW)
     
     
widget = Label(canvas, text='AAA', fg='white', bg='black')
widget.pack()
canvas.create_window(100, 100, window=widget)      
mainloop()
"""		 

#canvas.create_window(100, 100, window=widget)   
#Working with git
Git is a nifty version control system.  The instructions below pertain specifically to Unix derivatives.
##Make a GitHub account 
- Register [here](https://github.com).  

==================
#Install git 
##Using a package manager...  
###Mac  

&nbsp;&nbsp;~~`brew install git`~~  
&nbsp;&nbsp;`sudo port install git`

###Linux 
&nbsp;&nbsp;It might already be installed

#Configuring things
Let's set our name and email...  

`git config --global user.name "Count Chocula"`  
`git config --global user.email "cereal_lover@sonorandogs.co.jp"`

Done!  

==================
#Cloning a remote repository
You can clone (i.e. copy) a remote repository from the command line with **git**.  Here we'll clone the APIL repo hosted on GitHub. 
First we'll want to change to the directory we want to house our local copy of the repository...  

`cd my/favorite/directory`  

Ok, now we can finally clone the APIL repository! 
 
`git clone https://github.com/myedibleenso/APIL.git`



==================

#Changing something  
Now that we have a local copy of the repository, we can finally get to work.  You know what this repository is missing?  A meaningless text file!  Let's add one...  

`touch poop.txt`

Yuck!  Glad that's over.  Why don't we add some text this new file?

`echo "some text" >> poop.txt`  

Did that really work?  Let's make sure...  

`tail poop.txt`  

Wow.  It's probably a better idea to leave this sort of thing to your favorite text editor.  

We have our new text file ready, but how do we add it to our repository?  

=================
#Adding files
Adding a file to a repository is dead simple in **git**.  

`git add poop.txt`  

That it.  We've added a file to the repository.  We should probably leave a record of what we've done so others will know how and what we're contributing to the project... 

==================
#Committing changes
Commit is record of what we've done.  It is an absolutely essential step of the whole process.  We need to leave a succinct description of our change, so let's try adding a message *in-line* (designated below with **-m**)...

`git commit -m "Added to test my git skillz"`  

Great!  I guess we're finished here.  

I can't wait to see what others do with my new text file.  Hold on a second...why isn't this file showing up on the reomote GitHub repository?  How is anyone going to be able to access my work?  
  
==============
# 


#Simplifying things (*optional*)
Are you tired of entering your username and password after every push?  Wouldn't it be nice if there was a secure way to push those changes that saved you from having to enter all of that info again and again and again?  

Try [this](https://help.github.com/articles/generating-ssh-keys).
>***Tips:***  
	  *&nbsp;&nbsp;&nbsp;- you may need to create `.ssh` with `mkdir`*  
	  *&nbsp;&nbsp;&nbsp;- use `xclip` instead of `pbcopy` if you're on Linux*  
	  *&nbsp;&nbsp;&nbsp;- alternatively*
	  , you can simply copy the output of `cat id_rsa.pub`   

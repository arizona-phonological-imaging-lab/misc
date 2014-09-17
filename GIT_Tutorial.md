#Version control with git
As you've surmised by now, [Git](http://git-scm.com) is the lab's version control system of choice.  Follow the steps below to get up and running with git and Github.

##Make a GitHub account
- Register [here](https://github.com).  

#Install git
##Using a package manager...  
###Mac  

&nbsp;&nbsp;~~`brew install git`~~  
&nbsp;&nbsp;`sudo port install git-core +bash_completion`

###Linux
&nbsp;&nbsp;It might already be installed

#Configuring things
Let's set our name and email...  

`git config --global user.name "Count Chocula"`  
`git config --global user.email "cereal_lover@sonorandogs.co.jp"`

Now we'll change the default behavior for `push`:  

`git config --global push.default current`

#Cloning a remote repository
You can clone (i.e. copy) a remote repository from the command line with git.  Here we'll clone the [APIL repo](https://github.com/myedibleenso/APIL) hosted on GitHub.

First we'll want to change to the directory where our local copy of the repository will live...  

`cd my/favorite/directory`  

Clone the APIL repository and its submodules...

`git clone --recursive https://github.com/myedibleenso/APIL.git`

#Changing something  
Now that we have a local copy of the repository, we can finally get to work.  You know what this repository is missing?  A meaningless text file!  Let's add one...  

`touch poop.txt`

Nice work.  Why don't we add some text this new file?

`echo "some text" >> poop.txt`  

Did that really work?  Let's make sure...  

`tail poop.txt`  

We have our new text file ready, but how do we add it to our repository?  

#Adding files
Adding a file to a repository is pretty simple.  

`git add poop.txt`  

#Removing files
If you remove a tracked file from a repository without telling git about it, you might run into problems.  There are two ways of removing files with git.

1. If you want to safely __delete__ a file so that git knows about the change:

  `git rm /path/to/the/tracked/file/you/want/to/delete`  

2.  If you want to keep a file but simply tell git to stop tracking it:

  `git rm --cached forget/about/this/file`

#Committing changes
A commit is a record of what we've done.  Whenever we publish our changes, we'll need to leave a succinct description of work.  Let's try adding a message (designated below with the option **-m**)...

`git commit -m "Added poop to test my git skillz"`  

Great!  I guess we're finished here.

Hold on a second...why isn't our poop showing up on the remote GitHub repository?

#Pushing to the remote repository

Sharing our changes is easy.  We simply *push* them to the remote.

`git push`

What about the changes submitted by other contributors?  How do we keep things up-to-date?

#Updating the local repository

We need to *pull* our changes from the remote repository.

`git pull`

That covers the basics!

#Making a new branch

If you want to develop a new feature or drastically revise the current bulk of the current code, it's probably a good idea to make these changes in a separate branch before merging the changes with the `master` branch.  This helps to keep the code in `master` stable.

To make a new branch off of the current branch and switch to that branch:

`git checkout -b my-awesome-new-branch`  

*more coming soon*

#Switching branches

Switching branches is actually quite simple:  

`git checkout somebranchname`

If you checkout an existing branch, don't forget to pull in any changes that may have been pushed to the remote by your collaborators:

`git pull`

*more coming soon*

#What branches exist?

Sometimes you just want to see a list of all existing branches:  

`git branch -a`  

*more coming soon*

#Merging branches

*coming soon*

#Simplifying things (*optional*)
Are you tired of entering your username and password after every push?  Wouldn't it be nice if there was a secure way to push those changes that saved you from having to enter all of that info again and again and again?  

Try [this](https://help.github.com/articles/generating-ssh-keys).
>***Tips:***

You may need to...
 - *create the `.ssh` folder with `mkdir ~/.ssh`*
 - *use `xclip` instead of `pbcopy` if you're on Linux*  
    -  *alternatively, you can simply copy the output of `cat id_rsa.pub`*

#Bells & Whistles

- [git-completion.bash](http://git-scm.com/book/en/Git-Basics-Tips-and-Tricks) (makes life a little easier)

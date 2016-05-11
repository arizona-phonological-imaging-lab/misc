---
layout: default
title: Installation
categories: webapp
updated: May 2016
---


#Installation Instructions.

The easiest way to use the webapp is to access our APIL server, or a similar server set up by your lab. If you want to install it locally or set up a production server of your own, follow the instructions below. 

##Pre-install

Before you install, you need to have certain dependencies. Your computer needs Python 3 and pip (comes with python 3). Once you have these, run the following commands (may require sudo)

`pip3 install django`
`pip3 install django-debug`
`pip3 install textgrid`

>Note these may not be the only dependencies. If you run into "module not found" errors while trying to run the webserver, use pip3 to install those modules.

##Use of APILWeb

Download the zip archive or clone the github repository for APILWeb. In the command line, navigate to the APILWeb folder. Run the following command:

`python3 manage.py runserver`

This should start a webservice on your local machine on port 8000 (this is the default, but you can change it.)

Once the server is running, you must merely open your browser and navigate to http://127.0.0.1:8000/ . At the time of this writing, there is no landing page, so you need to manually select which app you wish to use. To use the database, navigate to http://localhost:8000/uat/1/ . To use the tracer, navigate to http://localhost:8000/tracer/draw/ . 

>localhost and 127.0.0.1 should be synonymous


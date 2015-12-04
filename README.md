# torcs-scr-gui
A remote-control for the TORCS scr 2013 server patch.

* Do you like race car simulators? TORCS is a great one, and it's open source.
* Do you like to code your own driver-AI? Install the SCR server patch, which offers a network interface, inspired by the sensor data autonomous race cars can get via LiDAR, RADAR and cameras.
* Do you like to start a race with several SCR driver clients written in Java? Then download this GUI.
 
## Features
* Searchs a given directory and lists all Drivers (Java classes named "*Driver.java")
* You can 
  * Drag and Drop drivers as they shall start in the race
  * Customize each car with a texture (car1-trb1.rgb)
  * Enter the number of laps and select the race-track
  * Check for damage, verbosity, test or qualifying
  * Press the start button
* Press the button will 
  * kill any preexisting TORCS or Java process (via cmd.exe)
  * replace XML files in torcs-subdirectories with customized ones reflecting your selection
  * replace the default texture with the one that comes with the driver (if any)
  * start TORCS (via cmd.exe)
  * start all selected clients as separate processes (via cmd.exe)
* Last step for you: 3 clicks in the TORCS Menu. Then wait for the drivers to be loaded and enjoy the race.

## Prerequisits
* works on Windows only
* Java 1.7
* TORCS V 1.3.4 http://torcs.sourceforge.net/
* The 2013 SCR Server Patch http://sourceforge.net/projects/cig/files/SCR%20Championship/
 
Screenshot of the TORCS GUI: 
![alt text](https://raw.githubusercontent.com/kinnla/torcs-scr-gui/master/torcs-gui.PNG "Screenshot of the TORCS GUI")


## Credits
This GUI was developed in the context of the lecture "ProInformatik III -- Objektorientierte Programmierung" 2015 at Freie Universität Berlin. http://www.fu-berlin.de/vv/de/lv/216597
Thanks to Bernhard Wymann for the hint, how to replace the driver names in TORCS!

This repository contains (modified) files that are part of TORCS, and the (modified) SCR Java Client.

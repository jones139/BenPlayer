BenPlayer
=========

BenPlayer is a simple YouTube video player system for a disabled person (called Benjamin)
 who likes to watch
videos on a tablet computer, but can not use a touch screen (in fact the touch screen is a 
problem because he keeps pausing or cancelling the video by touching the screen with his fingers
or nose (he is very short sighted).

To overcome this the tablet computer is put in a perspex holder so that the touch screen functions can
not be operated by accident.   The problem with this is that it is difficult for his carer to set it
going for him.   BenPlayer is designed to overcome this problem.

There are two applications 'BenPlayer' runs on Benjamin's tablet computer.  It is a background service
that waits for commands to play videos that are received over wifi.   When a command to play a video
is received, the screen is switched on and the video played.

The commands to play videos are sent from another device using the BenPlayerRemote app, which is the
remote control for Benjamin's tablet computer.   The app allows 7 videos to be selected, and these are
shown as thumbnail images on the app screen.  The carer presses the image for the video that Benjamin
wants to watch and its sends the command to BenPlayer, which plays the video.

Instructions
============
Install BenPlayer on the device that will display the videos.
Install BenPlayerRemote on the device that will be used to select which video to play.

Start BenPlayer.  It will show a Notification icon in the top of the screen.  Dag down the top of 
the screen - the notification text will show "BenPlayer - Listening on http://xxx.xxx.xxx.xxx:8080
Where xxx.xxx.xxx.xxx is the IP address of the device.  Make a note of this IP Address.

Start BenPlayerRemote on the other device.   Scroll to the bottom of the screen and press the 'Edit Settings'
button.   Select the 'Ip Address of Ben Player Device' option, and enter the IP address obtained above
and press OK.
Return to the BenPlayerRemote main screen with the back button.
Press one of the thumbnail images of videos - the video should play on the BenPlayer device.



Credits
=======
Based to a large extent on the OpenSeizureDetector application framework (http://openseizuredetector.org.uk).


Copyright Graham Jones, 2015 (email graham@openseizuredetector.org.uk).



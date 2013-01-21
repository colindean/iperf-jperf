********************************************
*        INSTALLATION INSTRUCTIONS         *
********************************************

* On windows systems,
	- Download and install Java JRE 1.5+ on http://java.sun.com
	- run the following script : jperf.bat

* On Linux / OS X systems, run the following script : 
	- The 'java' (JRE 1.5+) executable have to be into the system path
	- Don't forget to set execution permissions on the jperf.sh script (execute 'chmod u+x jperf.sh')
	- run the following script : jperf.sh

* To use the OS X app, simply open the DMG file and copy jperf.app to Applications.
  If when run, jperf complains about not having iperf installed and you're sure
  that it is installed, you may need to add the location of iperf to your global
  PATH variable. Execute this in the Terminal and reboot:

  > echo "setenv PATH /usr/bin:/bin:/usr/sbin:/sbin:$(dirname `which iperf`)" | sudo tee /etc/launchd.conf

********************************************
*        COMPILATION INSTRUCTIONS          *
********************************************

* Go to the 'utilities' directory
* run the following command (Apache ANT has to be installed on the system) :  

	> ant release

This script will create a JPerf distribution into the 'release' directory.

* To build the OS X distributable disk image, execute:

  > ant macdist

The resulting .dmg file will be in the 'release/jperf-<version>-mac' directory.

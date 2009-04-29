#!/usr/bin/python

import sys,os,datetime,subprocess,time

################################################################################
# Configuration
################################################################################
LOCKFILE = "/var/lock/itmill-toolkit-server"

################################################################################
# Tools
################################################################################
def execute(cmd):
	# print cmd
	return os.system(cmd)

# Unpacks the Toolkit installation package to a target location.
# The package file name must have absolute path.
def unpackToolkit(packagefile, targetdir):
	cmd = "tar zxf %{PF} -C ${TD}" % {"PF": packagefile, "TD": targetdir}
	if execute(cmd):
		print "Unpacking Toolkit installation package %{PF} to ${TD} failed." % {"PF": packagefile, "TD": targetdir}
		sys.exit(1)

def startToolkit(packagename, testarea):
	print "Starting Toolkit server in %s/%s" % (testarea, packagename)
	os.chdir ("%s/%s" % (testarea, packagename));

    # All the stdin, stdout, and stderr must be redirected
    # or otherwise Ant will hang when this start script returns.
	if execute("ITMILLTOOLKIT_PARAMETERS='--nogui=1' nohup sh start.sh </dev/null >WebContent/nohup.txt 2>&1 &"):
		print "Launching Toolkit server failed."

	# Wait a little to let it start.
	time.sleep(5)
	
def stopToolkit():
	pin = os.popen("ps -do pid,args | grep ITMillToolkit | grep -v grep | grep java | sed -e 's/^ \\+//' | cut -d ' ' -f 1", "r")
	if pin:
		pid = pin.read()
		pin.close()
		pid = pid.rstrip('\n')
		if len(pid)>0:
			print "Killing existing Toolkit demo, PID [" + pid + "]"
			execute("kill -9 " + pid)
			time.sleep(5);
			print "Killing existing Toolkit demo, PID [" + pid + "]"
			execute("kill -9 " + pid)
			time.sleep(2);

################################################################################
# Commands
################################################################################

def commandStart(packagename, packagefile, testarea):
	# Remove old build
	if len(testarea) < 3:
		print "The test area directory may not be too short: %s" % (testarea)
		sys.exit(1)
	execute("rm -rf %s/itmill-toolkit-*" % (testarea))
		
	print "Creating test area '%s' if it does not already exist." % (testarea)
	if execute ("mkdir -p %s" % testarea):
		print "Creation of test area '%s' failed." % (testarea)
		sys.exit(1)
		
	print "Extracting Toolkit package '%s' to test area '%s'..." % (packagefile, testarea)
	if execute ("tar zxf %s -C %s" % (packagefile, testarea)):
		print "Extracting Toolkit package failed."
		sys.exit(1)
	
	# Start new Toolkit demo service
	startToolkit(packagename, testarea)
	
	# Wait for the service to start
	print "Waiting a bit for Servlet Container to start up."
	time.sleep(5);

def commandStop():
	stopToolkit()

	# allow next instance to run
	execute("rm -f %s" % (LOCKFILE))
	
################################################################################
# Testing
################################################################################

command = sys.argv[1]

# Always try to stop.
commandStop()
	
if command == "start" or command == "restart":
	packagename = sys.argv[2]
	packagefile = sys.argv[3]
	testarea    = sys.argv[4]
	commandStart(packagename, packagefile, testarea)

print "Done."

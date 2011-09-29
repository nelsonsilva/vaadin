#!/usr/bin/python

import sys,os,datetime,subprocess,time,glob

################################################################################
# Configuration
################################################################################
LOCKFILE = "/var/lock/vaadin-server"

################################################################################
# Tools
################################################################################
def execute(cmd):
	# print cmd
	return os.system(cmd)

################################################################################
# startVaadin()
# Starts the Vaadin server process.
################################################################################
def startVaadin(packagename, testarea):
	print "Starting Vaadin server in %s/%s" % (testarea, packagename)
	os.chdir ("%s/%s" % (testarea, packagename));

	libdir = testarea + "/" + packagename + "/WebContent/WEB-INF/lib"

	classpath = "WebContent/tests/lib/jetty/jetty-6.1.7.jar:"+\
				"WebContent/tests/lib/jetty/jetty-util-6.1.7.jar:"+\
				"WebContent/tests/lib/jetty/servlet-api-2.5-6.1.7.jar:"+\
				"WebContent/WEB-INF/classes:WebContent/WEB-INF/src"
	
    # add *.jar from libdir on the classpath
	for jarname in glob.glob(libdir+"/*.jar"):
		classpath = classpath + ":" + jarname

	javacmd = "java -cp %(classpath)s com.vaadin.launcher.DemoLauncher --nogui=1" % {"classpath": classpath}
	print javacmd

    # All the stdin, stdout, and stderr must be redirected
    # or otherwise Ant will hang when this start script returns.
	if execute(javacmd + " </dev/null >WebContent/nohup.txt 2>&1 &"):
		print "Launching Vaadin server failed."

	# Wait a little to let it start.
	time.sleep(5)

def stopProcess(pin):
	if pin:
		pid = pin.read()
		pin.close()
		pid = pid.rstrip('\n')
		if len(pid)>0:
			print "Killing existing Vaadin test server, PID [" + pid + "]"
			execute("kill -9 " + pid)
			time.sleep(5);
			print "Killing existing Vaadin test server, PID [" + pid + "]"
			execute("kill -9 " + pid)
			time.sleep(2);
	
def stopVaadin():
	pin = os.popen("ps -do pid,args | grep DemoLauncher | grep -v grep | grep java | sed -e 's/^ \\+//' | cut -d ' ' -f 1", "r")
	stopProcess(pin)
	pin = os.popen("ps -do pid,args | grep DevelopmentServerLauncher | grep -v grep | grep java | sed -e 's/^ \\+//' | cut -d ' ' -f 1", "r")
	stopProcess(pin)

################################################################################
# Commands
################################################################################

def commandStart(packagename, packagefile, outputdir, testarea):
	# Remove old build
	print "Cleaning test area '%s'..." % (testarea)
	if len(testarea) < 3:
		print "The test area directory may not be too short: %s" % (testarea)
		sys.exit(1)
	execute("rm -rf %s/vaadin-*" % (testarea))

	installationpath = testarea + "/" + packagename + "/WebContent"
		
	print "Creating test area '%s' if it does not already exist..." % (testarea)
	if execute ("mkdir -p %s" % installationpath):
		print "Creation of test area '%s' failed." % (testarea)
		sys.exit(1)
		
	print "Extracting Vaadin package '%s' to test area '%s'..." % (packagefile, installationpath)
	if execute ("unzip %s -d %s" % (packagefile, installationpath)):
		print "Extracting Vaadin package failed."
		sys.exit(1)

	resultpath       = outputdir[:outputdir.rfind("/")]

	# Start new Vaadin test service
	startVaadin(packagename, testarea)
	
	# Wait for the service to start
	print "Waiting a bit for Servlet Container to start up."
	time.sleep(5);

def commandStop():
	stopVaadin()

	# allow next instance to run
	execute("rm -f %s" % (LOCKFILE))
	
################################################################################
# Testing
################################################################################

command = sys.argv[1]

# Always try to stop.
commandStop()
	
if command == "start" or command == "restart":
	packagename  = sys.argv[2]
	packagefile  = sys.argv[3]
	outputdir    = sys.argv[4]
	testarea     = sys.argv[5]
	commandStart(packagename, packagefile, outputdir, testarea)

print "Done."

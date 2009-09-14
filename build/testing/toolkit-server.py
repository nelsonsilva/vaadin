#!/usr/bin/python

import sys,os,datetime,subprocess,time

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

# Unpacks the Vaadin installation package to a target location.
# The package file name must have absolute path.
def unpackVaadin(packagefile, targetdir):
	cmd = "tar zxf %{PF}s -C ${TD}s" % {"PF": packagefile, "TD": targetdir}
	if execute(cmd):
		print "Unpacking Vaadin installation package %{PF} to ${TD} failed." % {"PF": packagefile, "TD": targetdir}
		sys.exit(1)

################################################################################
# startVaadin()
# Starts the Vaadin server process.
################################################################################
def startVaadin(packagename, testarea):
	print "Starting Vaadin server in %s/%s" % (testarea, packagename)
	os.chdir ("%s/%s" % (testarea, packagename));

	libdir = testarea + "/" + packagename + "/WebContent/WEB-INF/lib"

	classpath = "WebContent/demo/lib/jetty/jetty-6.1.7.jar:"+\
				"WebContent/demo/lib/jetty/jetty-util-6.1.7.jar:"+\
				"WebContent/demo/lib/jetty/servlet-api-2.5-6.1.7.jar:"+\
				"WebContent/WEB-INF/classes:WebContent/WEB-INF/src"
	classpath += ":%(libdir)s/testbench/*:%(libdir)s/testbench/lib/*" % {"libdir": libdir}

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
			print "Killing existing Vaadin demo, PID [" + pid + "]"
			execute("kill -9 " + pid)
			time.sleep(5);
			print "Killing existing Vaadin demo, PID [" + pid + "]"
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
		
	print "Creating test area '%s' if it does not already exist..." % (testarea)
	if execute ("mkdir -p %s" % testarea):
		print "Creation of test area '%s' failed." % (testarea)
		sys.exit(1)
		
	print "Extracting Vaadin package '%s' to test area '%s'..." % (packagefile, testarea)
	if execute ("tar zxf %s -C %s" % (packagefile, testarea)):
		print "Extracting Vaadin package failed."
		sys.exit(1)

	resultpath       = outputdir[:outputdir.rfind("/")]
	installationpath = testarea + "/" + packagename

	# Copy extra class files from the output directory to the test area. (#3325)
	classsrc  = resultpath + "/classes/com/vaadin/tests"
	classtrg  = installationpath + "/WebContent/WEB-INF/classes/com/vaadin/tests"
	print "Copying all class files from %s to %s..." % (classsrc, classtrg)
	if execute ("cp -r %s %s" % (classsrc, classtrg)):
		print "Copying class files failed."
		sys.exit(1)

	# TODO (#3325): Copy test themes, etc.
	webcontent = "WebContent"
	themepath  = webcontent + "/VAADIN/themes"
	testthemes = themepath + "/tests-*"
	themetrg   = installationpath + "/WebContent/VAADIN/themes/"
	print "Copying themes from %s to %s..." % (testthemes, themetrg)
	if execute ("cp -r %s %s" % (testthemes, themetrg)):
		print "Copying theme files failed."
		sys.exit(1)

	# Copy testbench libraries.
	testbenchdir = "build/lib/testbench"
	libdir       = installationpath + "/WebContent/WEB-INF/lib"
	print "Copying testbench libraries from '%s' to '%s'" % (testbenchdir, libdir)
	if execute ("cp -r  %s %s/" % (testbenchdir, libdir)):
		print "Copying testbench libraries to test installation failed."
		sys.exit(1)
	
	# Start new Vaadin demo service
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
	packagename = sys.argv[2]
	packagefile = sys.argv[3]
	outputdir   = sys.argv[4]
	testarea    = sys.argv[5]
	commandStart(packagename, packagefile, outputdir, testarea)

print "Done."

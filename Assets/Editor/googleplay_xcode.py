#!/opt/local/bin/python

import sys,os,plistlib


# settings
bundleId = "com.nerdiacs.testapp"
gpgAppId = "YOUR_GOOGLEPLAY_SERVICES_APPID"

xcode_proj_path = sys.argv[1]
target = sys.argv[2]
if(target!="iPhone"):
	exit(0) # the script is only for xcode/iphone build type.

installPath = sys.argv[1]
patchFile = os.path.join(os.path.dirname(sys.argv[0]),"googleplay_data","gpg_appcontroller.patch")
appControllerFile = os.path.join(installPath,"Classes","AppController.mm")
os.system("patch -p 0 %s %s" % (appControllerFile, patchFile))

# set plist file for faceboook
plistFilePath = os.path.join(installPath,"Info.plist")
pl = plistlib.readPlist(plistFilePath)
new_settings = {
	"CFBundleURLSchemes": ["%s" % (bundleId)]
}
if "CFBundleURLTypes" in pl:
	pl["CFBundleURLTypes"].extend(new_settings)
else:
	pl["CFBundleURLTypes"] = [new_settings]

# set fbappid in main dict
pl["GPGApplicationID"] = gpgAppId
plistlib.writePlist(pl, plistFilePath)


# PBX Proj patching


gpg_xcode_dir1 = os.path.join(os.path.dirname(sys.argv[0]),"../../GooglePlaySDKs","google-plus-ios-sdk-1.3.0")
gpg_xcode_dir = os.path.relpath(gpg_xcode_dir1,xcode_proj_path)
gpg_play_xcode_dir1 = os.path.join(os.path.dirname(sys.argv[0]),"../../GooglePlaySDKs")
gpg_play_xcode_dir = os.path.relpath(gpg_play_xcode_dir1,xcode_proj_path)


# List of all the frameworks to be added to the project
# format of list
# Frameworkname, FRAMEWORKID for pbxproj, FRAMEWORK_FILEREFID, FRAMEWORKPATH, "SDKROOT" if framework is part of SDK else "<group>"
# REFIDS are taken by creating an xcode project and then later on reading the ref ids from there.
# TODO: Add weak attribute
frameworks = [
		["GooglePlus.framework", 'B3A0CBD51745890E00654FC4', 'B3A0CBD21745890E00654FC4',gpg_xcode_dir+"/", "\"<group>\"",''], \
		["GoogleOpenSource.framework", 'B3A0CBD31745890E00654FC4', 'B3A0CBD01745890E00654FC4',gpg_xcode_dir+"/", "\"<group>\"",''], \
		["PlayGameServices.framework", 'B3A0CBDD1745899200654FC4', 'B3A0CBDB1745899200654FC4',gpg_play_xcode_dir+"/", "\"<group>\"",''], \
		["CoreData.framework", 'B3A0CBD71745895C00654FC4', 'B3A0CBD61745895C00654FC4', "System/Library/Frameworks/", "SDKROOT", ''],\
		["Security.framework", 'B3A0CBD91745896200654FC4', 'B3A0CBD81745896200654FC4', "System/Library/Frameworks/", "SDKROOT", '']
#		["libsqlite3.0.dylib", 'B3C95B0417398F950000D92B', 'B3C95B0317398F950000D92B', "usr/lib/", "SDKROOT"],\
#		["Accounts.framework", 'B3790A8917395DB400083999', 'B3790A8817395DB400083999', "System/Library/Frameworks/", "SDKROOT", '']
]


# List of data files to be added to the app bundle, see RESFILE_* constants for array order (currently hasn't been tested but should work 6.23.2012)
resfiles = [
	["GooglePlus.bundle","B3A0CBD41745890E00654FC4","B3A0CBD11745890E00654FC4","wrapper.plug-in",gpg_xcode_dir+"/"],\
	["PlayGameServices.bundle","B3A0CBDC1745899200654FC4","B3A0CBDA1745899200654FC4","wrapper.plug-in",gpg_play_xcode_dir+"/"]
]

# List of header search paths to add to the project (paths will automatically be wrapped in quotes)
#searchPaths = [gpg_xcode_dir+"/FacebookSDK"]
searchPaths = []

# List of additional linker flags to add to the project
linkerFlags = ["-ObjC"]

# Frameowrk search path
frameworkSearchPaths = []

# ****** END SCRIPT CUSTOMIZATION ********
# Don't edit the stuff below unless you know what you are doing.


# Frameworks array index constants
FRAMEWORK_NAME = 0
FRAMEWORK_ID = 1
FRAMEWORK_FILEREFID = 2
FRAMEWORK_BASEPATH = 3
FRAMEWORK_SOURCETREE = 4
FRAMEWORK_SETTINGS = 5

# Resources array index constants
RESFILE_NAME = 0
RESFILE_ID = 1
RESFILE_FILEREFID = 2
RESFILE_LASTKNOWNTYPE = 3
RESFILE_BASEPATH = 4

# Adds a line into the PBXBuildFile section
def add_build_file(pbxproj, id, name, fileref, settings=None):
	subsection = 'Resources'
	if name[-9:] == 'framework':
		subsection = 'Frameworks'
	print "Adding build file " + name + '\n'
	if settings!=None:
		pbxproj.write('\t\t' + id + ' /* ' + name  + ' in ' + subsection + ' */ = {isa = PBXBuildFile; fileRef = ' + fileref +	' /* ' + name + ' */;' + settings + ' };\n')
	else:	
		pbxproj.write('\t\t' + id + ' /* ' + name  + ' in ' + subsection + ' */ = {isa = PBXBuildFile; fileRef = ' + fileref +	' /* ' + name + ' */; };\n')

#Adds a line to the PBXFileReference to add a resource file
def add_res_file_reference(pbxproj, id, name, last_known_file_type, base_path):
	print "Adding data file reference " + name + "\n"
	pbxproj.write('\t\t' + id + ' /* ' + name + ' */ = {isa = PBXFileReference; fileEncoding = 4; lastKnownFileType = ' + last_known_file_type + '; name = ' + name + '; path = ' + base_path + name + '; sourceTree = \"<group>\"; };\n')

# Adds a line into the PBXFileReference section to add a framework
def add_framework_file_reference(pbxproj, id, name, base_path, source_tree):
	print "Adding framework file reference " + name + '\n'
	pbxproj.write('\t\t' + id + ' /* ' + name + ' */ = {isa = PBXFileReference; lastKnownFileType = wrapper.framework; name = ' + name + '; path = ' + base_path + name + '; sourceTree = ' + source_tree + '; };\n')

# Adds a line into the PBXFrameworksBuildPhase section
def add_frameworks_build_phase(pbxproj, id, name):
	print "Adding build phase " + name + '\n'
	pbxproj.write('\t\t\t\t' + id + ' /* ' + name + ' in Frameworks */,\n')

# Adds a line into the PBXResourcesBuildPhase section
def add_resources_build_phase(pbxproj, id, name):
	print "Adding build phase " + name + '\n'
	pbxproj.write('\t\t\t\t' + id + ' /* ' + name + ' in Resources */,\n')

# Adds a line into the PBXGroup section
def add_group(pbxproj, id, name):
	print "Add group " + name + '\n'
	pbxproj.write('\t\t\t\t' + id + ' /* ' + name + ' */,\n')


# Processes the given xcode project to add or change the supplied parameters
#	xcodeproj_filename - filename of the Xcode project to change
#	frameworks - list of Apple standard frameworks to add to the project
#	resfiles - list resource files added to the project
def process_pbxproj(xcodeproj_filename, frameworks, resfiles):

	# Open up the file generated by Unity and read into memory as
	# a list of lines for processing
	pbxproj_filename = xcodeproj_filename + '/project.pbxproj'
	pbxproj = open(pbxproj_filename, 'r')
	lines = pbxproj.readlines()
	pbxproj.close()

	# Check if file has already been processed and only proceed if it hasn't,
	# we'll do this by looping through the build files and see if first framework is already present
	# is there
	i = 0
	found = False
	while (not found) and (lines[i][3:6] != 'End'):
#		found = lines[i].find('ExternalAccessory.framework') > 0
		found = lines[i].find(frameworks[0][0]) > 0 # check with first framework name
		i = i+1

	if found:
		print "%s have already been added to XCode project" % (frameworks[0][0])
	else:
		# Next open up an empty project.pbxproj for writing and iterate over the old
		# file copying the original file and inserting anything extra we need
		pbxproj = open(pbxproj_filename, 'w')

		# As we iterate through the list we'll record which section of the
		# project.pbxproj we are currently in
		section = ''

		# We use these booleans to decide whether we have already added the list of
		# build files to the link line.	 This is needed because there could be multiple
		# build targets and they are not named in the project.pbxproj
		frameworks_build_added = False
		res_build_added = False

		i = 0
		for i in range(0, len(lines)):
			line = lines[i]
			# Write the current line to the file
			if line.strip().startswith("DEBUG_INFORMATION_FORMAT"):
				pbxproj.write("\t\t\t\tDEBUG_INFORMATION_FORMAT = dwarf-with-dsym;\n")
			else:		
				pbxproj.write(line)

			# Each section starts with a comment such as
			# /* Begin PBXBuildFile section */'
			if line[3:8] == 'Begin':
				section = line.split(' ')[2]
				if section == 'PBXBuildFile':
					for framework in frameworks:
						add_build_file(pbxproj, framework[FRAMEWORK_ID], framework[FRAMEWORK_NAME], framework[FRAMEWORK_FILEREFID],framework[FRAMEWORK_SETTINGS])
					for resfile in resfiles:
						add_build_file(pbxproj, resfile[RESFILE_ID], resfile[RESFILE_NAME], resfile[RESFILE_FILEREFID])

				if section == 'PBXFileReference':
					for framework in frameworks:
						add_framework_file_reference(pbxproj, framework[FRAMEWORK_FILEREFID], framework[FRAMEWORK_NAME], framework[FRAMEWORK_BASEPATH], framework[FRAMEWORK_SOURCETREE])
					for resfile in resfiles:
						add_res_file_reference(pbxproj, resfile[RESFILE_FILEREFID], resfile[RESFILE_NAME], resfile[RESFILE_LASTKNOWNTYPE], resfile[RESFILE_BASEPATH])



			if line[3:6] == 'End':
				section = ''

			if section == 'PBXFrameworksBuildPhase':
				if line.strip()[0:5] == 'files':
					if not frameworks_build_added:
						for framework in frameworks:
							add_frameworks_build_phase(pbxproj, framework[FRAMEWORK_ID], framework[FRAMEWORK_NAME])
						frameworks_build_added = True

			# The PBXResourcesBuildPhase section is what appears in XCode as 'Link
			# Binary With Libraries'.  As with the frameworks we make the assumption the
			# first target is always 'Unity-iPhone' as the name of the target itself is
			# not listed in project.pbxproj
			if section == 'PBXResourcesBuildPhase':
				if line.strip()[0:5] == 'files':
					if not res_build_added:
						for resfile in resfiles:
							add_resources_build_phase(pbxproj,resfile[RESFILE_ID], resfile[RESFILE_NAME])
						res_build_added = True

			# The PBXGroup is the section that appears in XCode as 'Copy Bundle Resources'. 
			if section == 'PBXGroup':
				if (line.strip()[0:8] == 'children') and (lines[i-2].strip().split(' ')[2] == 'CustomTemplate'):
					for resfile in resfiles:
						add_group(pbxproj, resfile[RESFILE_FILEREFID], resfile[RESFILE_NAME])
					for framework in frameworks:
						add_group(pbxproj, framework[FRAMEWORK_FILEREFID], framework[FRAMEWORK_NAME])

			#Add the additional header search paths and linker flags
			if section == 'XCBuildConfiguration':
				if line.strip().startswith('buildSettings'):
					if(len(searchPaths) > 1):
						pbxproj.write('\t\t\t\tHEADER_SEARCH_PATHS = (\n')
						for path in searchPaths:
							pbxproj.write('\t\t\t\t\t\"\\\"' + path + '\\\"/**\",\n')
						pbxproj.write('\t\t\t\t);\n')
					elif(len(searchPaths)==1):
						pbxproj.write('\t\t\t\tHEADER_SEARCH_PATHS = ')
						pbxproj.write('\"' + searchPaths[0] + '/**\";\n')
					# Add framework search paths for any frameworks not in SDKROOT
					frameworkHeaderWritten = False
					for framework in frameworks:
						if framework[FRAMEWORK_SOURCETREE] != "SDKROOT":
							if frameworkHeaderWritten == False:
								pbxproj.write('\t\t\t\tFRAMEWORK_SEARCH_PATHS = (\n')
								pbxproj.write('\t\t\t\t\t\"$(inherited)\",\n')
								frameworkHeaderWritten = True
							if framework[FRAMEWORK_BASEPATH][0]!='/':	
								pbxproj.write('\t\t\t\t\t\"\\\"$(SRCROOT)/' + framework[FRAMEWORK_BASEPATH] + '\\\"\",\n')
							else:
								# Tili SRCROOT should not be there as we have full path
								pbxproj.write('\t\t\t\t\t\"\\\"' + framework[FRAMEWORK_BASEPATH] + '\\\"\",\n')
					for path in frameworkSearchPaths:
						if frameworkHeaderWritten == False:
							pbxproj.write('\t\t\t\tFRAMEWORK_SEARCH_PATHS = (\n')
							pbxproj.write('\t\t\t\t\t\"$(inherited)\",\n')
							frameworkHeaderWritten = True
						pbxproj.write('\t\t\t\t\t\"\\\"' + path + '\\\"\",\n')
					if frameworkHeaderWritten == True:
						pbxproj.write('\t\t\t\t);\n')
				#Add other linker flags
				if line.strip() == 'OTHER_LDFLAGS = (':
					for flag in linkerFlags:
						pbxproj.write('\t\t\t\t\t\"' + flag + '\",\n')
 
		#close the project, we are done writing to it
		pbxproj.close()

xcode_proj_path = os.path.join(installPath,'Unity-iPhone.xcodeproj')
process_pbxproj(xcode_proj_path, frameworks, resfiles)


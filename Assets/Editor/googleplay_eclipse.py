#!/opt/local/bin/python

import xml.etree.ElementTree as etree
import sys,os,shutil

googleplay_app_id = 'YOUR_GOOGLEPLAY_APPID'
eclipse_proj_path = sys.argv[1]
target = sys.argv[2]
if(target!="android"):
	exit(0) # the script is only for eclipse/android build type.

editor_dir = os.path.dirname(sys.argv[0])
# Update Android Manifest with our plugin dummy activity and googleplay game services meta data
android_manifest_file = os.path.join(eclipse_proj_path,"AndroidManifest.xml")
print "Android file is %s" % (android_manifest_file)
tree = etree.parse(android_manifest_file)
elem = tree.find('//application')
elem.append(etree.Element("meta-data",{'ns0:name':'com.google.android.gms.games.APP_ID', 'ns0:value':'@string/app_id'}))
elem.append(etree.Element("meta-data",{'ns0:name':'com.google.android.gms.appstate.APP_ID', 'ns0:value':'@string/app_id'}))
elem.append(etree.Element("activity",{'ns0:name':'com.nerdiacs.nerdgpgplugin.DummyActivity', 'ns0:label':'@string/app_name'}))
#etree.ElementTree(tree).write(sys.stdout, encoding='utf-8')
tree.write(android_manifest_file)


# Update strings.xml with our app_id
strings_file = os.path.join(eclipse_proj_path,'res','values','strings.xml')
#print "Strings file %s" % (strings_file)
tree = etree.parse(strings_file)
#tree.write(sys.stdout)
elem = tree.find('.')
appelem = etree.Element("string",{'name':'app_id'})
appelem.text = googleplay_app_id
elem.append(appelem)
#etree.ElementTree(tree).write(sys.stdout, encoding='utf-8')
tree.write(strings_file)


# copy android code for library part
gpgplugin_code = os.path.join(editor_dir,"googleplay_data","android_code","com","nerdiacs")
dstgpg_code = os.path.join(eclipse_proj_path,"src","com","nerdiacs")
if os.path.exists(dstgpg_code):
	print "File exists. Adding subdirs to path"
	gpgplugin_code = os.path.join(gpgplugin_code,"nerdgpgplugin")
	dstgpg_code = os.path.join(dstgpg_code,"nerdgpgplugin")

print "Copynig %s to %s\n" %(gpgplugin_code,dstgpg_code)
shutil.copytree(gpgplugin_code,dstgpg_code)

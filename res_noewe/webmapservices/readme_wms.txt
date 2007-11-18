# This is an example and explantion of an wms definition file.
# copy and rename this file to the extension .wms
# You can add your own .wms file to the program directory of
# cachwolf and the new service will be available in the download maps dialog
# name the file this way: <internet domain of the covered region, replace "." by "-" and inverse the order>_<type of map>_<original scale>.wms
# e.g. "de-ni_topo_25", meaning germany (de), Niedersachen (ni), topographical map, original scale 1:50000.
# Please don't use capital letter or special characters (only "a-z0-9" and "-_" are allowed) 
# in the filename in order to avoid problems on different platforms
# Please include in every .wms you create a reference to this explanation file
#
# For a list of WMS Services available in Germany see: http://deutschlandviewer.bayern.de/deutschlandviewer/D_Viewer_Hilfe/Hilfe_D_Viewer.htm#Geodaten
# This example is made up from there "Digitales Orthophoto" under section "Nordrhein-Westfalen"
# download the getCapabilieties URL, save and open it in a text editor. 
# used here: http://www.gis2.nrw.de/wmsconnector/wms/luftbild?REQUEST=GetCapabilities&VERSION=1.1.0&SERVICE=WMS
#
# You can delete and add comments if you like. A comment starts with "#" in the first coloumn of the line.
#
# Please include the two lines (TakenFromUrl: and GetCapabilitiesUrl:)
# and fill in accordingly in any .wms file you make. They
# are (still) not used by CacheWolf but they are usefull
# for someone who wants to make his own changes to your file.
# 
# Url where the GetCapabilitiesUrl is taken from, in order to be able
# to gather some information about the map
TakenFromUrl:	http://deutschlandviewer.bayern.de/deutschlandviewer/D_Viewer_Hilfe/Hilfe_D_Viewer.htm#Geodaten
GetCapabilitiesUrl:	http://www.gis2.nrw.de/wmsconnector/wms/luftbild?REQUEST=GetCapabilities&VERSION=1.1.0&SERVICE=WMS	
#
# friendly name, choose yourself. By convention start with the internet domanin
# of the covered region and add the type of map and the scale of the original map 
# multiplied by 1000, e.g. "de.nrw Luftbild" or
# "en Airial photo" or "de.th Topo 1:50"
Name:	de.nrw Luftbild
# taken from getCapabilieties answer: <HTTP><GET><OnlineResource xlink:href=
MainUrl:	http://www.gis2.nrw.de/wmsconnector/wms/luftbild? 
# if this service delivers topografical maps, fill in here "topo"
# if it delivers aerial photographs fill in "photo".
# Please use only lower case letters and no special characters
# in order to avoid problems using this file on different platforms
# CacheWolf will store all maps of the same Type in the same 
# directory.
MapType: photo
# this is fix, dont change it
ServiceTypeUrlPart:	SERVICE=WMS 
# taken from the getCapabilities request: <WMT_MS_Capabilities version=
VersionUrlPart:	VERSION=1.1.0 
# The EPSG-Code, supported by cachewolf: german gauß-krüger (31466, 31467, 31468, 
# 31469) and WGS84 (4326)
# You get a list of supported coordinate systems from the WMS in the getCapabilieties 
# answer under <Layer><SRS> or <CRS>
# Plases feel free to ask for another coordinate system to be supported by cachewolf 
# if you need it
# In case the wms server accepts coordinates in more than one Gauß-Krüger stripe
# you can list the epsg codes here, seperated by a space. CacheWolf will
# automatically make use of the correct stripe. 
# The Strings in the UrlPart must match the corresponding number here
# Sometimes the wms-Server provides only one stripe, in spite of the fact, that
# the map it provides is not completely within this stripe. In this case
# just list only this epsg code. CacheWolf will automatically calculate the
# Gauß-Krüger coordinates for that stripe.
# The automatic for the stripe selection only works if a german EPSG code
# is the first one in the space seperated list
# remark: some WMS offer WGS84 (EPSG 4326), but they are sometimes working not
# correctly (for example the WMS of the Landesvermessungsamt NRW as of nov. 2007)
# In this case don't list it.
CoordinateReferenceSystemCacheWolf:	31466 31467
# this usually will match the number above
CoordinateReferenceSystemUrlPart:	SRS=EPSG:31466 SRS=EPSG:31467
# Post not supported by Cachewolf --> dont change this
RequestUrlPart:	REQUEST=GetMap
# comma seperated (without spaces) list of layers to combine
# all of supported layers you get from the getCapabilities request <Layer><Name>
# these names are to be used. Special characters must be URL-encode
LayersUrlPart:	LAYERS=Orthophoto%20Str.%202,Orthophoto%20Str.%203
# if the WMS supports different rendering styles, select the one you need here
# comma seperated (without spaces) list of style commands for map rendering (do not delete this item even if it is empty
StylesUrlPart:	STYLES=
# format, dont forget to set ImageFileExtension accordingly
# you get a list of supported image formats from getCapabilieties answer: <GetMap><Format>
ImageFormatUrlPart:	FORMAT=image/png
# Limits of the service in WGS84 coordinates. 
# You can use any format here, which is accepted by the input coordinates dialog in cachewolf
# taken from getCapabilieties answer: <BoundingBox SRS="EPSG:4326", dont forget to add "N"/"S" and "E"/"W"
BoundingBoxTopLeftWGS84:	N 52.7691 E 5.673
BoundingBoxButtomRightWGS84:	N 49.9944 E 10.142
# scale range that the service supports in meters per pixel (measured diagonal)
# Please don't wonder that they do mot match the scale given in
# the map download dialog as that scale is measured vertically 
# (multiply it ba sqrt(2) and you get the scale used here
# taken from the getCapabilities request "<Layer><ScaleHint min="
MinScale:	0.1795783591567183
MaxScale:	5.611823723647454
# Plaes recommend a scale for this WMS. This scale will appear in the
# map download dialog as default. Scale is measured in meters per pixel
# vertical, so, multiply it by 1.41 (=sqrt(2)) to get the scale as measured
# above in MinScale and MaxScale
RecommendedScale:	0.5
# set this according to ImageFormatUrlPart (must start with ".")
ImageFileExtension: .png


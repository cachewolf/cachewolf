REM clean up
del .\work\*.png
del .\work\*.gif
del .\work\*.html
del .\work\*.tpl
del .\work\*.ico
REM get ressources
copy .\resources\*.* .\work\*.*
copy .\res_noewe\*.* .\work\*.*
mkdir .\work\attributes
copy .\resources\attributes\*.* .\work\attributes\*.*
mkdir .\work\webmapservices
copy .\res_noewe\webmapservices\*.* .\work\webmapservices\*.*
copy .\lib\*.dll .\work\
move .\work\cachewolf.Languages.cfg .\work\_config\

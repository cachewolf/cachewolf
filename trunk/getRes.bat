REM clean up
del .\work\*.png
del .\work\*.gif
del .\work\*.html
del .\work\*.tpl
del .\work\*.ico
REM get ressources
copy .\resources\*.* .\work\*.*
copy .\resources\attributes\*.* .\work\attributes\*.*
copy .\lib\*.dll .\work\
move .\work\cachewolf.Languages.cfg .\work\_config\

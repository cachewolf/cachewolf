REM clean up
cd %~d0%~p0
del .\work\*.png
del .\work\*.gif
del .\work\*.html
del .\work\*.tpl
del .\work\*.ico
REM get ressources
copy .\resources\*.* .\work\*.*
copy .\res_noewe\*.* .\work\*.*
mkdir .\work\attributes
copy .\resources\attributes-big\*.* .\work\attributes\*.*
copy .\resources\attributes\*-non.gif .\work\attributes\*.*
rem
mkdir .\work\mmcDefault
copy .\res_noewe\mmcDefault\*.* .\work\mmcDefault\*.*
mkdir .\work\mmc240x320
copy .\res_noewe\mmc240x320\*.* .\work\mmc240x320\*.*
mkdir .\work\mmc480x640
copy .\res_noewe\mmc480x640\*.* .\work\mmc480x640\*.*
rem
mkdir .\work\webmapservices
copy .\res_noewe\webmapservices\*.* .\work\webmapservices\*.*
mkdir .\work\languages
copy .\res_noewe\languages\*.* .\work\languages\*.*
mkdir .\work\symbols
copy .\res_noewe\symbols\*.* .\work\symbols\*.*
copy .\lib\*.dll .\work\

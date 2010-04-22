REM clean up
cd %~d0%~p0
del .\work\*.png
del .\work\*.gif
del .\work\*.html
del .\work\*.tpl
del .\work\*.ico
REM
REM fill .\work root
REM
copy .\resources\*.* .\work\*.*
REM
copy .\res_noewe\*.* .\work\*.*
REM
REM fill .\work subdirs
REM
mkdir .\work\attributes
copy .\res_noewe\attributes\*.* .\work\attributes\*.*
REM
mkdir .\work\mmcDesktop
copy .\res_noewe\mmcDesktop\*.* .\work\mmcDesktop\*.*
mkdir .\work\mmc240x320
copy .\res_noewe\mmc240x320\*.* .\work\mmc240x320\*.*
mkdir .\work\mmc480x640
copy .\res_noewe\mmc480x640\*.* .\work\mmc480x640\*.*
REM
mkdir .\work\webmapservices
copy .\res_noewe\webmapservices\*.* .\work\webmapservices\*.*
REM
mkdir .\work\languages
copy .\res_noewe\languages\*.* .\work\languages\*.*
REM
mkdir .\work\symbols
copy .\res_noewe\symbols\*.* .\work\symbols\*.*
REM
copy .\lib\*.dll .\work\

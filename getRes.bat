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
mkdir .\work\mmc
copy .\res_noewe\mmc\*.* .\work\mmc\*.*
mkdir .\work\mmc\Desktop
copy .\res_noewe\mmc\Desktop\*.* .\work\mmc\Desktop\*.*
mkdir .\work\mmc\pda
copy .\res_noewe\pda\*.* .\work\pda\*.*
mkdir .\work\mmc\pda_vga
copy .\res_noewe\mmc\pda_vga\*.* .\work\mmc\pda_vga\*.*
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
mkdir .\work\templates
copy .\res_noewe\templates\*.* .\work\templates\*.*
REM

copy .\lib\*.dll .\work\

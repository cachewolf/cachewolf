The Problem:
Recently a lot of WINCE 5.0 based PNA - devices are on the market. The most important difference to "complete" PDAs is the reduced 
system core. Normally nobody needs the missing funtions if the device is used as intended by the manufacturer.
But...well...it would be nice to use the little computer also for other things than navigation.
Unfortunatly most wince applications are written for wince, not wince-core.
As a result one only get a error box starting such program.


Attached there are 2 patches wich eliminate calls to on my wince 5.0 core based device unavailable dll.
On my device winsock.dll and aygshell.dll were missing. 
As a result there will be not network support and (perhaps more important) no support for showing/hiding the keyboard.
My solution was to use another program to show the taskbar. If somebody know how to emulate the functions from aygshell.dll
then it would be possible to support these features.

The modifications were developed and testet on a Vista ultimate machine with MSVC 2005.
usage:

- unpack Ewe14[8|9]-Source-Win32.zip
- copy the appropriate patch to the directory ewe-win32-source
- apply the patch with   
		#> patch -i ewe_1.48_cemod.diff -p 1

The tricky part may be the adjustments needed by the project files. Until now I have only managed to get 
the debug version running. I'm shure this only depends on some wrong configs...perhaps somebody can look into this.


Have fun!

rheinkraxler@online.de
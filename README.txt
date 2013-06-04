porting Location Privacy Framework to CyanogenMod 10.1 (Android 4.2 Jelly Bean)

   +++ work in progress +++

Seems working right now, but have to be checked in more detail.
Settings is working, obfuscation worked for test apps
like Lokus free, but not for Maps as in other OS versions.
For building, see other doc, e.g. those of cm-9.1.0.
See TODO.txt for things that have to be done. 


How to create a patch for Android code?
 # Create a diff with the very first commit
 git diff --patch --no-prefix 63edfc494d9bdf4afd4cbae01ec16cff45099153 HEAD -- build/core/pathmap.mk frameworks/base/ packages/apps/Settings/ >cm101-lpf.patch
 # including local changes
 git diff --patch --no-prefix 63edfc494d9bdf4afd4cbae01ec16cff45099153 -- build/core/pathmap.mk frameworks/base/ packages/apps/Settings/ >cm101-lpf.patch

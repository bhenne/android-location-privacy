porting Location Privacy Framework to CyanogenMod 10.1 (Android 4.2 Jelly Bean)

   +++ work in progress +++

Seems working right now (tested with CM 10.1 rc4), but have to be checked in more detail.
Settings is working, obfuscation worked for tested apps like Lokus free, but not for Maps as in other OS versions -- those apps using Google Play Services.
For building, see other docs, e. g. those of branch cm-9.1.0.
See TODO.txt for things that have to be done. 


How to create a patch for Android code?
 # Create a diff with the very first commit
 git diff --patch --no-prefix 63edfc494d9bdf4afd4cbae01ec16cff45099153 HEAD -- build/core/pathmap.mk frameworks/base/ packages/apps/Settings/ >cm101-lpf.patch
 # including local changes
 git diff --patch --no-prefix 63edfc494d9bdf4afd4cbae01ec16cff45099153 -- build/core/pathmap.mk frameworks/base/ packages/apps/Settings/ >cm101-lpf.patch


echo "Press any key to copy files to $1/"
echo "Use Ctrl-C to cancel."
read
 
cp -v packages/apps/Settings/mapquest-android-sdk-1.0.4.jar $1/packages/apps/Settings/
cp -v packages/apps/Settings/res/drawable/point.png $1/packages/apps/Settings/res/drawable/
cp -v packages/apps/Settings/res/drawable-hdpi/ic_settings_locationprivacy.png $1/packages/apps/Settings/res/drawable-hdpi/
cp -v packages/apps/Settings/res/drawable-mdpi/ic_settings_locationprivacy.png $1/packages/apps/Settings/res/drawable-mdpi/
cp -v packages/apps/Settings/res/drawable-xhdpi/ic_settings_locationprivacy.png $1/packages/apps/Settings/res/drawable-xhdpi/

echo "Done."
echo "Have you already done 'cd $1; patch -p0 < patchfile'?"

set -e

#echo "ANDROID_SDK_ROOT = $ANDROID_SDK_ROOT"
echo "ANDROID_HOME = $ANDROID_HOME"
echo "PATH = $PATH"

export PATH=$PATH:$ANDROID_HOME/emulator

echo "PATH = $PATH"

# Start the Android emulator
echo "list of AVDs: (1)"
emulator -list-avds

# List available system images
#echo "list of system images:"
#sdkmanager --list | grep system-images



sdkmanager --install "system-images;android-35;google_apis_playstore;arm64-v8a"

echo "listing devices"
avdmanager list device

# Example
avdmanager create avd -n "pixel_5-Android35" -k "system-images;android-35;google_apis_playstore;arm64-v8a" --device "pixel_5"

echo "list of AVDs: (2)"
emulator -list-avds

scho "starting emulator"
emulator -avd pixel_5-Android35 -no-audio -no-window &

# Wait for the emulator to fully boot
adb wait-for-device

scho "starting emulator - after wait-for-device"
adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done;'
adb shell input keyevent 82
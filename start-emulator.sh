set -e

#echo "ANDROID_SDK_ROOT = $ANDROID_SDK_ROOT"
echo "ANDROID_HOME = $ANDROID_HOME"
echo "PATH = $PATH"

export PATH=$PATH:$ANDROID_HOME/emulator

echo "PATH = $PATH"

# Start the Android emulator
emulator -list-avds

# Wait for the emulator to fully boot
adb wait-for-device
adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done;'
adb shell input keyevent 82
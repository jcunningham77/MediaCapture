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
echo "list of system images:"
sdkmanager --list | grep system-images



sdkmanager --install "system-images;android-27;default;x86"

echo "listing devices"
avdmanager list device

# Example
avdmanager create avd -n "pixel_5-Android27" -k "system-images;android-27;default;x86" --device "pixel_5"

echo "list of AVDs: (2)"
emulator -list-avds

echo "current user: $USER"
sudo gpasswd -a $USER kvm

echo "current processor:"
lscpu

echo "Check whether a hypervisor is installed"
emulator -accel-check

echo "starting emulator"
emulator -avd pixel_5-Android27 -no-audio -no-window &

# Wait for the emulator to fully boot
#timeout 40 adb wait-for-device

#echo "starting emulator - after wait-for-device"
#adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done;'
#adb shell input keyevent 82

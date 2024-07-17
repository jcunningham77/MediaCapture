set -e

# Start the Android emulator
emulator -avd test -no-audio -no-window &

# Wait for the emulator to fully boot
adb wait-for-device
adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done;'
adb shell input keyevent 82
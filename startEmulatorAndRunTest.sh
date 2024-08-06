echo "starting emulator"
emulator -avd Pixel7-Pro-Android35 -no-audio -no-window &
echo "emulator start command issued"
echo "Current running devices:"
adb devices
echo "ADB Wait for device issued"
adb wait-for-device
echo "after wait for device emulator..."
echo "Current running devices:"


adb devices | while IFS= read -r line; do
    # Skip the first line (header)
    if [[ "$line" == *"List of devices attached"* || -z "$line" ]]; then
        echo "skipping header"
        continue
    fi

    # Extract device name (first column)
    device=$(echo $line | awk '{print $1}')

    # Do something with each device
    echo "Killing device: $device"
    adb -s $device emu kill
done

echo "current running devices:"
adb devices







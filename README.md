# GrooveController

GrooveController is an Android virtual controller specifically designed for the music game **Groove Coaster**. It utilizes Android's Bluetooth HID (Human Interface Device) capability to simulate your phone as a standard Bluetooth keyboard or gamepad, enabling you to play the game using touch gestures on your smartphone.

## 💡 Features

- **Bluetooth HID Simulation**: Works as a standard Bluetooth peripheral. No additional client software required on the host side (PC, Consoles, etc.).
- **Optimized for Groove Coaster**:
  - Supports 8-direction **Slide** and **Tap** operations.
  - Split-screen design: The screen is divided into two halves to simulate the left and right "Boosters" independently.
- **Adjustable Sensitivity**: Customize **Slide Stroke** (minimum distance for a slide) and **Debounce Value** to match your play style.
- **Multiple Modes**: Supports both **Keyboard** and **Gamepad** HID profiles.
- **Modern UI**: Developed with Jetpack Compose for a clean and responsive experience.
- **Multilingual Support**: Supports English and Chinese based on system settings.

## ⚠️ Important Notes

- **Recommended Mode**: It is highly recommended to use **Keyboard Mode**.
- **Compatibility**: **Gamepad Mode** may not work on some devices or systems. If you experience issues, please switch to Keyboard Mode.

## 🛠️ System Requirements

- **Android Version**: Android 9 (API 28) or higher (Minimum requirement for Bluetooth HID Device Profile).
- **Hardware Support**: Your phone must support the Bluetooth HID Device role (most modern Android devices do).

## 🚀 How to Use

1. **Pairing**:
   - Enable Bluetooth on your phone and make it discoverable.
   - Search for Bluetooth devices on your host (PC/Console) and pair with your phone.
2. **Setup**:
   - Open GrooveController and grant necessary permissions (Bluetooth and Location).
3. **Registration**:
   - Select your preferred simulation mode (**Keyboard recommended**).
   - Tap the **"Register"** button.
   - Once successfully registered and connected, the status will show the connected host.
4. **Play**:
   - Enter the controller interface.
   - The top half controls one booster, and the bottom half controls the other.
   - Just like the arcade: tap for Tap, slide for Slide.
5. **Exit**:
   - To return to the setup screen, press and hold both the **top-right** and **bottom-right** corners of the screen for 1.5 seconds.

## ⚙️ Configuration

- **Slide Stroke**: The minimum movement distance to trigger a "Slide" event.
- **Debounce Value**: Threshold to filter out tiny finger shakes.
- **Orientation**: Switch physical mapping for left/right hands if needed.

## 📂 Project Structure

- `Bluetooth.kt`: Handles Bluetooth HID registration and callbacks.
- `Control.kt`: Core gesture logic for Tap and 8-direction Slide detection.
- `Descriptors.kt`: HID Report Descriptors for Keyboard and Gamepad.
- `Canvas.kt`: UI components for drawing directional arrows.

## 📄 License

This project is licensed under the **MIT License**.

## ⚠️ Disclaimer

This software is for educational and personal use only. Please be careful not to damage your phone screen during intense gameplay.

---
Created by [tryanks](https://github.com/tryanks)

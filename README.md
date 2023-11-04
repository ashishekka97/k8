# k8 (Kate)

---

A multi-platform CHIP-8 emulator written in Kotlin
Supports Android, iOS Desktop and Web.

<img src="cover.png" width="320">

## Table of Contents

- [Introduction](#introduction)
- [Spec](#spec)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Features](#features)
- [Controls](#controls)
- [Contributing](#contributing)
- [License](#license)


## Introduction

The Chip8 is an interpreted programming language that was initially designed for the COSMAC VIP computer.
Kate allows you to run and play games or applications originally created for the Chip8 platform on your modern system.

## Spec
Kate leverages the [Kotlin multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) to have the core emulation logic in the `common` module.
All the target platforms then consume the common module to render the emulation output.

As of now the frontend for android, desktop and web is written in [compose](https://www.jetbrains.com/lp/compose-multiplatform/), while the iOS is written in [SwiftUI](https://developer.apple.com/xcode/swiftui/).
This is extensible though, and you can contribute to add more frontends.

<img src="kate_spec.png" width="640">

## Getting Started

To get started with this Chip8 emulator, follow these steps:

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/ashishekka97/k8.git
   cd k8
   ```
   
2. **Build Kate:**

   Open the project in IntelliJ Idea.
   Once opened, the gradle sync will start. After the sync is successful, you should automatically get run configurations
   for `andorid`, `desktop` and `web`.

   In order to build for `iOS`, you must have a Mac setup with XCode installed. This is mandate from Apple.
   Open the `iosApp` module in XCode to automatically trigger the build process. Else you can trigger it manually from menu.
   You must build the project in IntelliJ first before jumping to XCode, as IntelliJ will run the corresponding gradle
   build files to produce `pods` and other dependencies for `iOS`.

3. **Download Chip8 ROMs:** 

   You'll need Chip8 ROMs to run with the emulator. These ROMs contain the programs and games you want to emulate. You can find a variety of Chip8 ROMs on the internet. Place them in a directory accessible to the emulator. `android`
   comes with a bunch of inbuilt rom files.

4.  **Run Kate:**

    Select the run configuration from `android`, `desktop` and `web` in IntelliJ and click Run.
    For `iOS`, just click run from XCode.

## Usage

The usage of k8 is straightforward. You load a Chip8 ROM file using the inbuilt file picker,
and the emulator provides a virtual environment in which the ROM can run. Use the controls mentioned below to interact
with the emulator.

## Features

* **Chip8 Compatibility:** The emulator is designed to be compatible with most Chip8 programs and games.
* **Keyboard and Sound Support:** It emulates the original Chip8 input and sound system.
* **Themes:** There a couple of retro inspired monochrome color schemes to change the display color of the screen.

## Controls

The original Chip8 had a 16-key keypad. K8 for `desktop` and `web` maps these keys to your computer keyboard.
The mapping typically looks like this:

```text
Original Chip8 Keypad       Your Keyboard
1 2 3 C                     1 2 3 4
4 5 6 D                     Q W E R
7 8 9 E                     A S D F
A 0 B F                     Z X C V
```

On `android` and `iOS`, there's a virtual keyboard rendered just like the original Chip8 had.

## Contributing
Contributions from the open-source community are much appreciated. Feel free to submit bug reports, feature requests, or pull requests to help improve the emulator.

## License
This Chip8 emulator project is licensed under the MIT License, which means you are free to use, modify, and distribute the software as long as you provide the appropriate attribution and include the license file.
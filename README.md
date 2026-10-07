# DrawMotion - 2D Skill

Draw. Animate. Create.

DrawMotion is a focused Android app for 2D drawing and frame-by-frame animation.

## Launch scope

- Free at launch
- Drawing canvas with brushes, colors, eraser and undo/redo
- Frame-by-frame animation
- Onion skin
- FPS control and playback
- PNG export
- Sprite sheet export for 2D game assets
- PNG frame sequence export

## Build

The project uses Android Gradle Plugin 8.5.2, Java 17 and compile/target SDK 35.

From Android Studio or a machine with Gradle 8.7, run:

gradle assembleDebug assembleRelease

The debug APK is the easiest build for device testing.

## GitHub Actions

Every push to main builds the app. The first successful build also creates the GitHub Release v0.1.0 and attaches the debug and unsigned release APKs.

The unsigned release APK is a build artifact only. For store distribution, the app must later be signed with the production keystore.

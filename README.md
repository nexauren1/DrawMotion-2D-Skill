# DrawMotion Kotlin

A clean Kotlin-first rebuild of DrawMotion, focused on 2D drawing and frame-by-frame animation on Android.

## Architecture

The previous Java application layer is removed in the rebuild. The new editor is organized into typed Kotlin components:

- DrawCanvasView.kt — canvas rendering, gestures, drawing tools, layers, animation and transformations.
- Model.kt — project, frame, layer, brush, tool, ruler and blend state.
- Catalogs.kt — procedural assets, textures and ready animation templates.
- ProjectStore.kt — portable .drawmotion project save/load.
- ImageFilters.kt — pixel-level image filters.
- VideoExporter.kt — MP4 export through Android MediaRecorder.
- MainActivity.kt — native Android navigation and editor UI.

## Included

Brush presets, eraser, line, rectangle, ellipse, fill, picker, move, text, layers, opacity, blend modes, undo/redo, onion skin, rulers, grid, symmetry, procedural materials, textures, ready projects, image import, PNG export, sprite sheets, frame sequences and MP4 export.

## Build

Android Gradle Plugin 8.5.2, Kotlin 2.0.21, Java 17 and compile/target SDK 35.

Run `gradle assembleDebug assembleRelease --no-daemon`.

Application ID: `com.nexauren.drawmotion2d`
Version: `1.0.0`

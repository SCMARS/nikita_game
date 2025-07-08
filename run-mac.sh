#!/bin/bash

# Enhanced macOS launcher script for NikitaEscape LibGDX game
# This script includes various compatibility fixes for macOS

echo "🍎 Starting NikitaEscape on macOS..."
echo "Java version:"
java -version

echo ""
echo "System information:"
sw_vers
echo ""

# Check if we're on Apple Silicon or Intel
ARCH=$(uname -m)
echo "Architecture: $ARCH"

# Set JVM arguments for macOS
JVM_ARGS=(
    "-XstartOnFirstThread"
    "-Djava.awt.headless=false"
    "-Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true"
    "-Dcom.apple.mrj.application.apple.menu.about.name=NikitaEscape"
    "-Dfile.encoding=UTF-8"
    "-Dorg.lwjgl.util.Debug=true"
    "-Dorg.lwjgl.util.DebugLoader=true"
    "-Djava.library.path=$JAVA_LIBRARY_PATH"
)

# Additional arguments for Apple Silicon Macs
if [[ "$ARCH" == "arm64" ]]; then
    echo "🔧 Detected Apple Silicon Mac - adding Rosetta compatibility flags"
    JVM_ARGS+=(
        "-Dorg.lwjgl.system.allocator=system"
        "-Dorg.lwjgl.util.NoChecks=true"
    )
fi

# Additional arguments for Intel Macs
if [[ "$ARCH" == "x86_64" ]]; then
    echo "🔧 Detected Intel Mac - adding Intel-specific flags"
    JVM_ARGS+=(
        "-Dorg.lwjgl.opengl.maxVersion=3.2"
    )
fi

echo ""

# Check if user wants test mode
if [[ "$1" == "test" ]]; then
    echo "🧪 Starting in simple test mode..."
    JVM_ARGS+=("-Dnikita.test.mode=simple")
fi

echo "🚀 Launching game with enhanced macOS compatibility..."
echo "JVM Arguments: ${JVM_ARGS[*]}"
echo ""

# Try different launch methods
echo "Method 1: Using Gradle with enhanced JVM args..."
if ./gradlew lwjgl3:run "${JVM_ARGS[@]/#/-D}"; then
    echo "✅ Game launched successfully!"
    exit 0
fi

echo ""
echo "Method 1 failed. Trying Method 2: Direct JAR execution..."
if [ -f "lwjgl3/build/libs/NikitaEscape-1.0.0.jar" ]; then
    java "${JVM_ARGS[@]}" -jar lwjgl3/build/libs/NikitaEscape-1.0.0.jar
    if [ $? -eq 0 ]; then
        echo "✅ Game launched successfully via JAR!"
        exit 0
    fi
else
    echo "JAR file not found. Building..."
    ./gradlew lwjgl3:jar
    if [ -f "lwjgl3/build/libs/NikitaEscape-1.0.0.jar" ]; then
        java "${JVM_ARGS[@]}" -jar lwjgl3/build/libs/NikitaEscape-1.0.0.jar
    fi
fi

echo ""
echo "Method 2 failed. Trying Method 3: Clean build and run..."
./gradlew clean
./gradlew lwjgl3:run

echo ""
echo "If all methods failed, try:"
echo "1. Restart your Mac"
echo "2. Update Java: brew install openjdk"
echo "3. Update graphics drivers"
echo "4. Check Console.app for error messages"
echo "5. Run simple test: ./run-mac.sh test"
echo ""
echo "📖 For detailed troubleshooting, see: MACOS_TROUBLESHOOTING.md"

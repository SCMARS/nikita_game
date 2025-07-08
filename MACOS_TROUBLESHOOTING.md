# macOS Troubleshooting Guide for NikitaEscape

## 🍎 Quick Start

1. **First, restart your Mac** - this often resolves OpenGL context issues
2. **Use the enhanced launcher**: `./run-mac.sh`
3. **Or use standard Gradle**: `./gradlew lwjgl3:run`

## 🔧 Common Issues and Solutions

### Issue 1: Black/Purple Screen with Red Square
**Symptoms**: Game starts but only shows colored background, no map tiles
**Solutions**:
1. Restart your Mac
2. Update Java: `brew install openjdk`
3. Try software OpenGL: `./gradlew lwjgl3:run -Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true`
4. Check graphics drivers in System Preferences > Software Update

### Issue 2: Game Won't Start
**Symptoms**: Gradle builds but game window doesn't appear
**Solutions**:
1. Check Java version: `java -version` (should be 8 or higher)
2. Install/update Java: `brew install openjdk@17`
3. Set JAVA_HOME: `export JAVA_HOME=$(/usr/libexec/java_home -v 17)`

### Issue 3: OpenGL Errors
**Symptoms**: Console shows OpenGL-related errors
**Solutions**:
1. Force software rendering: Add `-Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true`
2. Limit OpenGL version: Add `-Dorg.lwjgl.opengl.maxVersion=3.2`
3. Check GPU compatibility in About This Mac > Graphics

### Issue 4: Apple Silicon (M1/M2) Specific Issues
**Solutions**:
1. Use Rosetta if needed: `arch -x86_64 ./gradlew lwjgl3:run`
2. Install x86_64 Java: `arch -x86_64 brew install openjdk`
3. Use native ARM Java: `brew install openjdk` (latest versions support ARM)

## 🚀 Launch Methods (Try in Order)

### Method 1: Enhanced macOS Script
```bash
./run-mac.sh
```

### Method 2: Gradle with Debug Flags
```bash
./gradlew lwjgl3:run -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true
```

### Method 3: Direct JAR Execution
```bash
./gradlew lwjgl3:jar
java -XstartOnFirstThread -Djava.awt.headless=false -jar lwjgl3/build/libs/NikitaEscape-1.0.0.jar
```

### Method 4: IDE Launch
1. Open project in IntelliJ IDEA
2. Navigate to `lwjgl3/src/main/java/com/nikita/game/lwjgl3/Lwjgl3Launcher.java`
3. Right-click and "Run Lwjgl3Launcher.main()"

## 🔍 Debugging Steps

### Check System Information
```bash
# Java version
java -version

# macOS version
sw_vers

# Architecture (Intel vs Apple Silicon)
uname -m

# Graphics information
system_profiler SPDisplaysDataType
```

### Enable Detailed Logging
Add these JVM arguments for detailed debugging:
```
-Dorg.lwjgl.util.Debug=true
-Dorg.lwjgl.util.DebugLoader=true
-Dorg.lwjgl.util.DebugAllocator=true
```

### Check Console for Errors
1. Open Console.app
2. Filter by "NikitaEscape" or "java"
3. Look for OpenGL, graphics, or JVM errors

## 🛠 System Requirements

### Minimum Requirements
- macOS 10.14 (Mojave) or later
- Java 8 or higher
- OpenGL 3.0 compatible graphics
- 4GB RAM

### Recommended
- macOS 12 (Monterey) or later
- Java 17 LTS
- Dedicated graphics card
- 8GB RAM

## 📞 Getting Help

If none of these solutions work:

1. **Check the Console.app** for detailed error messages
2. **Try on another Mac** to isolate hardware issues
3. **Update macOS** to the latest version
4. **Reset graphics preferences**: Delete `~/Library/Preferences/com.apple.opengl.plist`
5. **Contact support** with:
   - macOS version (`sw_vers`)
   - Java version (`java -version`)
   - Graphics card info (`system_profiler SPDisplaysDataType`)
   - Console.app error logs

## 🔄 Reset Everything

If all else fails, try a complete reset:
```bash
# Clean all build files
./gradlew clean

# Reset Gradle daemon
./gradlew --stop

# Rebuild everything
./gradlew build

# Try launching again
./run-mac.sh
```

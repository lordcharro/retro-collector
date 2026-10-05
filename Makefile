# ==============================================================================
# RetroCollector Makefile
# ==============================================================================

# Auto-detect suitable Java 21+ Home if not explicitly set in environment
JAVA_HOME ?= $(shell /usr/libexec/java_home -v 21 2>/dev/null || echo "/Users/ivolopes/Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home")
GRADLE = JAVA_HOME="$(JAVA_HOME)" ./gradlew

.PHONY: help check detekt lint test test-arch build run-desktop run-app package-app package-dmg clean

## 📖 help: Show this help message
help:
	@echo "RetroCollector Build & Quality Tooling"
	@echo "======================================"
	@echo "Available commands:"
	@echo "  make detekt        Run Detekt static code analysis and linting"
	@echo "  make test          Run Kotlin Multiplatform desktop unit tests"
	@echo "  make test-arch     Run Konsist clean architecture validation tests"
	@echo "  make check         Run both linting (detekt) and all unit tests"
	@echo "  make run-desktop   Launch RetroCollector Desktop application"
	@echo "  make build         Build all targets"
	@echo "  make clean         Clean Gradle build caches and outputs"
	@echo ""

## 🔍 detekt: Run Detekt static code analysis
detekt:
	@echo "==> Running Detekt static analysis..."
	$(GRADLE) detektAll

## 🔍 lint: Alias for detekt
lint: detekt

## 🧪 test: Run Desktop unit tests
test:
	@echo "==> Running unit tests..."
	$(GRADLE) desktopTest

## 🏛️ test-arch: Run Konsist architecture tests
test-arch:
	@echo "==> Running Konsist architecture tests..."
	$(GRADLE) desktopTest --tests "*CleanArchitectureKonsistTest*"

## 🛡️ check: Run full CI validation (linting + tests)
check: detekt test test-arch
	@echo "==> All quality checks passed successfully!"

## 🚀 run-desktop: Run Compose Multiplatform Desktop app
run-desktop:
	@echo "==> Launching Desktop application..."
	$(GRADLE) :composeApp:run

## 🍎 run-app: Launch packaged native app (shows RetroCollector in Dock & Menu Bar)
run-app:
	@echo "==> Launching native Desktop bundle..."
	$(GRADLE) :composeApp:runDistributable

## 📦 package-app: Build standalone macOS .app bundle
package-app:
	@echo "==> Building standalone .app bundle..."
	$(GRADLE) :composeApp:createDistributable
	@echo "==> App created at: composeApp/build/compose/binaries/main/app/RetroCollector.app"

## 💿 package-dmg: Build installable macOS .dmg disk image
package-dmg:
	@echo "==> Building installer .dmg..."
	$(GRADLE) :composeApp:packageDmg
	@echo "==> DMG created at: composeApp/build/compose/binaries/main/dmg/"


## 🔨 build: Build all targets
build:
	@echo "==> Building project..."
	$(GRADLE) build

## 🧹 clean: Clean build directory
clean:
	@echo "==> Cleaning build artifacts..."
	$(GRADLE) clean

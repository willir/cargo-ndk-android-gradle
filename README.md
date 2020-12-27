# Cargo NDK for Android projects

Allows building Rust code via `cargo ndk` command in android projects.

It is somewhat similar to the Mozilla 
[Rust Android Gradle Plugin](https://github.com/mozilla/rust-android-gradle),
however, it uses [`cargo ndk`](https://github.com/bbqsrc/cargo-ndk) 
to find the right  `linker` and `ar` and
build the project. Also, it allows configuring rust release profile (`dev` vs `release`)
for each gradle `buildType`. Actually, any options can be configured per gradle `buildType`,
it works similar to `android` configuration.

[Gradle Plugin Page](https://plugins.gradle.org/plugin/com.github.willir.rust.cargo-ndk-android).

## Usage

Add the plugin to your root `build.gradle`, like:

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "gradle.plugin.com.github.willir.rust:plugin:0.3.1"
    }
}
```

In your _project's_ `build.gradle`, `apply plugin` and
add the `cargoNdk` configuration (optionally):

```groovy
android { ... }

apply plugin: "com.github.willir.rust.cargo-ndk-android"

// The following configuration is optional and works the same way by default
cargoNdk {
    buildTypes {
        release {
            buildType = "release"
        }
        debug {
            buildType = "debug"
        }
    }
}
```

Install rust toolchains:

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

Install [`cargo-ndk`](https://github.com/bbqsrc/cargo-ndk):

```bash
cargo install cargo-ndk
```

If you already have `cargo-ndk`, please make sure it is up to date:

```bash
cargo install --force cargo-ndk
```

This plugin adds the following targets: `buildCargoNdkDebug`,
`buildCargoNdkRelease`, however they should be run automatically building your
android project as usual. So:

1. `./gradlew assembleDebug` will build `dev` (`debug`) profile.
   Depends on, and so will run `buildCargoNdkDebug`.
1. `./gradlew assembleRelease` will build `release` profile.
   Depends on, and so will run `buildCargoNdkRelease`.

## Configuration

### All options

```groovy
cargoNdk {
    // List of all targets
    // By default: ["arm64", "arm", "x86", "x86_64"]
    targets = ["arm64", "arm", "x86", "x86_64"]

    // Path to directory with rust project
    // By default: "app/src/main/rust"
    module = "../rust"

    // Path to rust 'target' dir (the dir where build happens), relative to module
    // By default: "target"
    targetDirectory = "target"

    // List of all library names to copy from target to jniLibs
    // By default parses Cargo.toml and gets all dynamic libraries
    librariesNames = ["libmy_library.so"]

    // The apiLevel to build link with
    // By default: android.defaultConfig.minSdkVersion
    apiLevel = 19

    // Whether to build cargo with --offline flag
    // By default: false
    offline = true

    // The rust profile to build
    // Possible values: "debug", "release", "dev" (an alias for "debug")
    // By default: "release" for release gradle builds,
    //             "debug"   for debug   gradle builds
    buildType = "release"
 
    // Extra arguments to pass to cargo command
    // By default: []
    extraCargoBuildArguments = ["--offline"]

    // Extra environment variables
    // By default: [:]
    extraCargoEnv = ["foo": "bar"]

    // Whether to pass --verbose flag to cargo command
    // By default: false
    verbose = true
}
```

As it was already mentioned, any of those options can be configured
separately for each buildTypes:

```groovy
cargoNdk {
    apiLevel = 19  // default

    buildTypes {
        debug {
            apiLevel = 26  // overwrite for debug
        }
    }
}
```

### Specify target via gradle property

You can also compile only one target by specifying the `rust-target` property to gradle.
E.g. to build only `arm64` target you can: `gradle assembleDebug -Prust-target=arm64`.
It can be useful during development in order to speed up each build 
via not rebuilding targets that are not used during testing.

package com.github.willir.rust

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import groovy.json.JsonSlurper

class CargoNdkBuildTask extends DefaultTask {
    @Input String variant
    @Input CargoNdkBuildPluginExtension extension

    private CargoNdkConfig args

    @TaskAction
    void buildRust() {
        args = new CargoNdkConfig(
                "", extension.buildTypeContainer.findByName(variant), extension)

        RustTargetType rustTargetName = null

        if (project.hasProperty("rust-target")) {
            rustTargetName = RustTargetType.fromId((String) project.findProperty("rust-target"))
        }

        if (rustTargetName != null) {
            buildTarget(rustTargetName)
        } else {
            for (target in args.getTargetTypes()) {
                buildTarget(target)
            }
        }
    }

    private void buildTarget(RustTargetType target) {
        int ndkVersion = getNdkVersion(target)

        def cmd = ["cargo", "ndk",
                   "--target", target.rustTarget,
                   "--android-platform", ndkVersion.toString(),
                   "--", "build"]
        if (args.offline) {
            cmd.add("--offline")
        }
        if (isRelease()) {
            cmd.add("--release")
        }
        if (isVerbose()) {
            cmd.add("--verbose")
        }
        if (args.extraCargoBuildArguments != null) {
            cmd.addAll(args.extraCargoBuildArguments)
        }

        logger.info("Executing: " + cmd)
        cwd = getCargoPath()
        project.exec {
            workingDir = cwd
            commandLine = cmd
        }.assertNormalExitValue()

        copyTarget(target)
    }

    private void copyTarget(RustTargetType target) {
        for (libName in listLibraryNames()) {
            def copyFrom = getRustLibOutPath(target, libName)
            def copyTo = getJniLibPath(target, libName)

            Files.createDirectories(copyTo.getParent())

            Files.deleteIfExists(copyTo)
            Files.copy(copyFrom, copyTo)
            logger.info("Copy ${copyFrom} -> ${copyTo}")
        }
    }

    private Path getRustLibOutPath(RustTargetType target, String libName) {
        return Paths.get(
                getRustTargetPath().toString(), target.rustTarget, args.buildType, libName)
    }

    private Path getRustTargetPath() {
        if (args.targetDirectory != null) {
            return args.targetDirectory
        } else {
            return Paths.get(getCargoPath().toString(), "target")
        }
    }

    private Path getJniLibPath(RustTargetType target, String libName) {
        return Paths.get(
                getSrcRootPath().toString(),
                "jniLibs",
                target.jniLibDirName, libName)
    }

    private Path getCargoPath() {
        if (args.module) {
            return args.module
        } else {
            return Paths.get(getSrcRootPath().toString(), "rust")
        }
    }

    private Path getSrcRootPath() {
        return Paths.get(project.rootDir.getPath(), "app", "src", "main")
    }

    private ArrayList<String> listLibraryNames() {
        if (args.librariesNames != null) {
            return args.librariesNames
        } else {
            return listCargoTargets(getCargoPath())
        }
    }

    private ArrayList<String> listCargoTargets(Path cargoDirPath) {
        def cmd = ["cargo", "metadata", "--format-version", "1"]
        if (args.offline) {
            cmd.add("--offline")
        }

        def os = new ByteArrayOutputStream()
        project.exec {
            workingDir = cargoDirPath
            commandLine = cmd
            standardOutput = os
        }.assertNormalExitValue()

        def metadata = new JsonSlurper().parseText(os.toString())
        def rootId = metadata.resolve.root
        if (rootId == null) {
            throw new GradleException(
                    "cargo workspace at: '" + cargoDirPath + "' is a virtual workspace")
        }

        def rootPackage = metadata.packages.find { it.id == rootId }
        if (rootPackage == null) {
            throw new GradleException(
                    "Cannot find root package for cargo workspace at: '" + cargoDirPath + "'")
        }

        return rootPackage.targets
                .findAll { it.kind.indexOf("dylib") != -1 }
                .collect { "lib" + it.name + ".so" }
    }

    private int getNdkVersion(RustTargetType target) {
        int ndkVersion = (args.apiLevel != null)
                ? args.apiLevel
                : project.android.defaultConfig.minSdkVersion.getApiLevel()
        if (target.is64Bit() && ndkVersion < 21) {
            logger.warn(
                    "" + target + " doesn't support " + ndkVersion +
                            " NDK version. Changing to 21")
            ndkVersion = 21
        }
        return ndkVersion
    }

    private boolean isRelease() {
        return args.buildType == "release"
    }

    private boolean isVerbose() {
        return args.verbose || project.logger.isEnabled(LogLevel.INFO)
    }
}

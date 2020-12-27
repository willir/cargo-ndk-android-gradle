package com.github.willir.rust

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import groovy.json.JsonSlurper

class CargoNdkBuildTask extends DefaultTask {
    @Input String variant
    @Input CargoNdkExtension extension

    private CargoNdkConfig config

    @TaskAction
    void buildRust() {
        config = new CargoNdkConfig(
                project,
                extension.buildTypeContainer.findByName(variant),
                extension)

        RustTargetType rustTargetName = null

        if (project.hasProperty("rust-target")) {
            rustTargetName = RustTargetType.fromId((String) project.findProperty("rust-target"))
        }

        if (rustTargetName != null) {
            buildTarget(rustTargetName)
        } else {
            for (target in config.getTargetTypes()) {
                buildTarget(target)
            }
        }
    }

    private void buildTarget(RustTargetType target) {
        if (!isFoundInPath("cargo")) {
            throw new GradleException(
                    "Cannot find 'cargo' executable in PATH: " + System.getenv("PATH"))
        }

        int ndkVersion = getNdkVersion(target)

        def cmd = ["cargo", "ndk",
                   "--target", target.rustTarget,
                   "--android-platform", ndkVersion.toString(),
                   "--", "build"]
        if (config.offline) {
            cmd.add("--offline")
        }
        if (config.isRelease()) {
            cmd.add("--release")
        }
        if (config.isVerbose()) {
            cmd.add("--verbose")
        }
        if (config.extraCargoBuildArguments != null) {
            cmd.addAll(config.extraCargoBuildArguments)
        }

        Path cwd = config.getCargoPath()
        if (!cwd.toFile().isDirectory()) {
            throw new GradleException(
                    "Rust root directory doesn't exist, or not a directory: '${cwd}'")
        }

        def extraEnv = getExtraCargoNdkEnvironments()
        if (config.extraCargoEnv != null) {
            extraEnv.putAll(config.extraCargoEnv)
        }
        logger.info("Executing: ${cmd} from ${cwd} with extra env: ${extraEnv}")

        project.exec {
            workingDir = cwd
            commandLine = cmd
            environment extraEnv
        }.assertNormalExitValue()

        copyTarget(target)
    }

    private void copyTarget(RustTargetType target) {
        for (libName in listLibraryNames()) {
            def copyFrom = config.getRustLibOutPath(target, libName)
            def copyTo = config.getJniLibPath(target, libName)

            Files.createDirectories(copyTo.getParent())

            Files.deleteIfExists(copyTo)
            Files.copy(copyFrom, copyTo)
            logger.info("Copy ${copyFrom} -> ${copyTo}")
        }
    }

    private ArrayList<String> listLibraryNames() {
        if (config.librariesNames != null) {
            return config.librariesNames
        } else {
            return listCargoTargets(config.getCargoPath())
        }
    }

    private ArrayList<String> listCargoTargets(Path cargoDirPath) {
        def cmd = ["cargo", "metadata", "--format-version", "1"]
        if (config.offline) {
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
                    "cargo workspace at: '${cargoDirPath}' is a virtual workspace")
        }

        def rootPackage = metadata.packages.find { it.id == rootId }
        if (rootPackage == null) {
            throw new GradleException(
                    "Cannot find root package for cargo workspace at: '${cargoDirPath}'")
        }

        return rootPackage.targets
                .findAll { it.kind.contains("dylib") || it.kind.contains("cdylib") }
                .collect { "lib" + (String) it.name + ".so" }
    }

    private int getNdkVersion(RustTargetType target) {
        int ndkVersion = config.apiLevel
        if (target.is64Bit() && ndkVersion < 21) {
            logger.warn("${target} doesn't support ${ndkVersion} NDK version. Changing to 21")
            ndkVersion = 21
        }
        return ndkVersion
    }

    private Map<String, Object> getExtraCargoNdkEnvironments() {
        Properties properties = new Properties()
        properties.load(project.rootProject.file('local.properties').newDataInputStream())
        def ndkDir = properties.getProperty('ndk.dir', null)

        if (ndkDir != null) {
            return ['ANDROID_NDK_HOME': ndkDir]
        } else {
            return [:]
        }
    }

    /**
     * Checks if a file/command exists in PATH environment and can be executed
     * @param file Name of the file/command to search
     * @return true if found, else false
     */
    private boolean isFoundInPath(String file) {
        def pathEnv = System.getenv('PATH')
        def fileFound = pathEnv.split(File.pathSeparator).find{ folder ->
            def f = Paths.get(folder, file).toFile()
            return f.exists() && f.canExecute()
        }
        return fileFound != null
    }
}

package com.github.willir.rust

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CargoNdkConfig {
    private ArrayList<String> targets = null
    private String module = null
    private String targetDirectory = null
    ArrayList<String> librariesNames = null
    private Integer apiLevel = null
    Boolean offline = null
    private String buildType = null
    ArrayList<String> extraCargoBuildArguments = null
    private Boolean verbose = null

    private Project project

    CargoNdkConfig(Project project,
                   final CargoNdkBuildTypeExtension buildTypeExt,
                   final CargoNdkExtension ext) {
        this.project = project

        this.targets = ext.targets
        this.module = ext.module
        this.targetDirectory = ext.targetDirectory
        this.librariesNames = ext.librariesNames
        this.apiLevel = ext.apiLevel
        this.offline = ext.offline
        this.buildType = ext.buildType
        this.extraCargoBuildArguments = ext.extraCargoBuildArguments
        this.verbose = ext.verbose

        if (buildTypeExt == null) {
            validate()
            return
        }
        if (buildTypeExt.targets != null) {
            this.targets = buildTypeExt.targets
        }
        if (buildTypeExt.module != null) {
            this.module = buildTypeExt.module
        }
        if (buildTypeExt.targetDirectory != null) {
            this.targetDirectory = buildTypeExt.targetDirectory
        }
        if (buildTypeExt.librariesNames != null) {
            this.librariesNames = buildTypeExt.librariesNames
        }
        if (buildTypeExt.apiLevel != null) {
            this.apiLevel = buildTypeExt.apiLevel
        }
        if (buildTypeExt.offline != null) {
            this.offline = buildTypeExt.offline
        }
        if (buildTypeExt.buildType != null) {
            this.buildType = buildTypeExt.buildType
        }
        if (buildTypeExt.extraCargoBuildArguments != null) {
            this.extraCargoBuildArguments = buildTypeExt.extraCargoBuildArguments
        }
        if (buildTypeExt.verbose != null) {
            this.verbose = buildTypeExt.verbose
        }
        validate()
    }

    ArrayList<RustTargetType> getTargetTypes() {
        return targets.collect { RustTargetType.fromId(it) }
    }

    int getApiLevel() {
        return (apiLevel != null)
                ? apiLevel
                : project.android.defaultConfig.minSdkVersion.getApiLevel()
    }

    boolean isVerbose() {
        return verbose || project.logger.isEnabled(LogLevel.INFO)
    }

    boolean isRelease() {
        return buildType == "release"
    }

    Path getRustLibOutPath(RustTargetType target, String libName) {
        return Paths.get(
                getRustTargetPath().toString(), target.rustTarget, buildType, libName)
    }

    Path getRustTargetPath() {
        def targetDir = (targetDirectory != null) ? targetDirectory : "target"
        return Paths.get(getCargoPath().toString(), targetDir)
    }

    Path getJniLibPath(RustTargetType target, String libName) {
        return Paths.get(
                getProjectSrcMainRootPat().toString(),
                "jniLibs",
                target.jniLibDirName, libName)
    }

    String getProjectRootDir() {
        return project.rootDir.getPath()
    }

    Path getCargoPath() {
        if (module) {
            return Paths.get(getProjectRootDir(), module)
        } else {
            return Paths.get(getSrcRootPath().toString(), "rust")
        }
    }

    Path getProjectSrcMainRootPat() {
        return Paths.get(project.projectDir.getPath(), "src", "main")
    }

    Path getSrcRootPath() {
        return Paths.get(project.rootDir.getPath(), "app", "src", "main")
    }

    private void validate() {
        if (buildType == "dev") {
            buildType = "debug"
        }
        if (!["release", "debug"].contains(buildType)) {
            throw new IllegalArgumentException(
                    "buildType must be either 'release', 'debug', or 'dev'. " +
                    "Where 'dev' is synonym for debug")
        }

        RustTargetType.validateTargetIds(targets)

        def cargoTomlPath = Paths.get(getCargoPath().toString(), "Cargo.toml")
        if (!Files.isRegularFile(cargoTomlPath)) {
            throw new IllegalArgumentException(
                    "Cannot find 'Cargo.toml' file in '${getCargoPath()}'.\n" +
                            "Please set the 'cargoNdk.module' property " +
                            "as a valid path to cargo project,\n" +
                            "relative to the project root '${getProjectRootDir()}'.")
        }
    }
}

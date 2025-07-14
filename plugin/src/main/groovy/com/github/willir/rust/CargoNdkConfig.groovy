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
    String profile = null
    ArrayList<String> extraCargoBuildArguments = null
    Map<String, String> extraCargoEnv = null
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
        this.profile = ext.profile
        this.extraCargoBuildArguments = ext.extraCargoBuildArguments
        this.extraCargoEnv = ext.extraCargoEnv
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
        if (buildTypeExt.profile != null) {
            this.profile = buildTypeExt.profile
        }
        if (buildTypeExt.extraCargoBuildArguments != null) {
            this.extraCargoBuildArguments = buildTypeExt.extraCargoBuildArguments
        }
        if (buildTypeExt.extraCargoEnv != null) {
            this.extraCargoEnv = buildTypeExt.extraCargoEnv
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

    Path getRustLibOutPath(RustTargetType target, String libName) {
        def targetDir = (profile == "dev") ? "debug" : profile
        return Paths.get(
                getRustTargetPath().toString(), target.rustTarget, targetDir, libName)
    }

    Path getRustTargetPath() {
        def targetDir = (targetDirectory != null) ? targetDirectory : "target"
        return Paths.get(getCargoPath().toString(), targetDir)
    }

    Path getJniLibPath(RustTargetType target, String libName) {
        return Paths.get(
                getProjectSrcMainRootPath().toString(),
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

    Path getProjectSrcMainRootPath() {
        return Paths.get(project.projectDir.getPath(), "src", "main")
    }

    Path getSrcRootPath() {
        return Paths.get(project.rootDir.getPath(), "app", "src", "main")
    }

    private void validate() {
        if (profile == "debug") {
            profile = "dev"
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

package org.willir29.rust

import java.nio.file.Path

class CargoNdkBuildType {
    final String name

    ArrayList<RustTargetType> supportedTypes = null
    Path module = null
    Path targetDirectory = null
    ArrayList<String> librariesNames = null
    Integer apiLevel = null
    Boolean offline = null
    String buildType = null
    ArrayList<String> extraCargoBuildArguments = null
    Boolean verbose = null

    CargoNdkBuildType(final String name) {
        this.name = name
        if (name in ["release", "debug"]) {
            buildType = name
        }
    }

    CargoNdkBuildType(final String name,
                      final CargoNdkBuildType that,
                      CargoNdkBuildPluginExtension ext) {
        this.name = name
        this.supportedTypes = ext.supportedTypes
        this.module = ext.module
        this.targetDirectory = ext.targetDirectory
        this.librariesNames = ext.librariesNames
        this.apiLevel = ext.apiLevel
        this.offline = ext.offline
        this.buildType = ext.buildType
        this.extraCargoBuildArguments = ext.extraCargoBuildArguments
        this.verbose = ext.verbose

        if (that == null) {
            validate()
            return
        }
        if (that.supportedTypes != null) {
            this.supportedTypes = that.supportedTypes
        }
        if (that.module != null) {
            this.module = that.module
        }
        if (that.targetDirectory != null) {
            this.targetDirectory = that.targetDirectory
        }
        if (that.librariesNames != null) {
            this.librariesNames = that.librariesNames
        }
        if (that.apiLevel != null) {
            this.apiLevel = that.apiLevel
        }
        if (that.offline != null) {
            this.offline = that.offline
        }
        if (that.buildType != null) {
            this.buildType = that.buildType
        }
        if (that.extraCargoBuildArguments != null) {
            this.extraCargoBuildArguments = that.extraCargoBuildArguments
        }
        if (that.verbose != null) {
            this.verbose = that.verbose
        }
        validate()
    }

    private void validate() {
        if (!["release", "debug"].contains(buildType)) {
            throw new IllegalArgumentException("buildType must be either 'relase' or 'debug'")
        }
    }
}

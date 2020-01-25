package com.github.willir.rust


class CargoNdkConfig {
    final String name

    ArrayList<String> targets = null
    String module = null
    String targetDirectory = null
    ArrayList<String> librariesNames = null
    Integer apiLevel = null
    Boolean offline = null
    String buildType = null
    ArrayList<String> extraCargoBuildArguments = null
    Boolean verbose = null

    CargoNdkConfig(final String name) {
        this.name = name
        if (name in ["release", "debug"]) {
            buildType = name
        }
    }

    CargoNdkConfig(final String name,
                   final CargoNdkConfig that,
                   final CargoNdkBuildPluginExtension ext) {
        this.name = name
        this.targets = ext.targets
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
        if (that.targets != null) {
            this.targets = that.targets
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

    void setTargets(ArrayList<String> targets) {
        RustTargetType.validateTargetIds(targets)
        this.targets = targets
    }

    ArrayList<RustTargetType> getTargetTypes() {
        return targets.collect { RustTargetType.fromId(it) }
    }

    private void validate() {
        if (buildType == "dev") {
            buildType = "debug"
        }
        if (!["release", "debug"].contains(buildType)) {
            throw new IllegalArgumentException("buildType must be either 'release', 'debug', or 'dev'. " +
                    "Where 'dev' is synonym for debug")
        }

        RustTargetType.validateTargetIds(targets)
    }
}

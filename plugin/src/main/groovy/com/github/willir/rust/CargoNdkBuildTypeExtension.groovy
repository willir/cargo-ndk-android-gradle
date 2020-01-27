package com.github.willir.rust

class CargoNdkBuildTypeExtension {
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

    CargoNdkBuildTypeExtension(final String name) {
        this.name = name
        if (name in ["release", "debug"]) {
            buildType = name
        }
    }
}

package org.willir29.rust

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

import java.nio.file.Path

class CargoNdkBuildPluginExtension {
    ArrayList<RustTargetType> supportedTypes = RustTargetType.values()
    Path module = null
    Path targetDirectory = null
    ArrayList<String> librariesNames = null
    Integer apiLevel = null
    boolean offline = false
    String buildType = "release"
    ArrayList<String> extraCargoBuildArguments = null
    boolean verbose = false

    NamedDomainObjectContainer<CargoNdkBuildType> buildTypeContainer

    CargoNdkBuildPluginExtension(Project project) {
        buildTypeContainer = project.container(CargoNdkBuildType)
        buildTypeContainer.create("release")
        buildTypeContainer.create("debug")
    }

    void buildTypes(Action<? super NamedDomainObjectContainer<CargoNdkBuildType>> action) {
        action.execute(buildTypeContainer)
    }
}

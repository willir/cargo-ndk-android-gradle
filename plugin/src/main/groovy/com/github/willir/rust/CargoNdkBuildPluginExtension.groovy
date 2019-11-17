package com.github.willir.rust

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

import java.nio.file.Path

class CargoNdkBuildPluginExtension {
    ArrayList<String> targets = RustTargetType.values().collect { it.id }
    Path module = null
    Path targetDirectory = null
    ArrayList<String> librariesNames = null
    Integer apiLevel = null
    boolean offline = false
    String buildType = "release"
    ArrayList<String> extraCargoBuildArguments = null
    boolean verbose = false

    void setTargets(ArrayList<String> targets) {
        RustTargetType.validateTargetIds(targets)
        this.targets = targets
    }

    NamedDomainObjectContainer<CargoNdkConfig> buildTypeContainer

    CargoNdkBuildPluginExtension(Project project) {
        buildTypeContainer = project.container(CargoNdkConfig)
        buildTypeContainer.create("release")
        buildTypeContainer.create("debug")
    }

    void buildTypes(Action<? super NamedDomainObjectContainer<CargoNdkConfig>> action) {
        action.execute(buildTypeContainer)
    }
}

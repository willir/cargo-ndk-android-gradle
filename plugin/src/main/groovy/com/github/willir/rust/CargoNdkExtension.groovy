package com.github.willir.rust

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

class CargoNdkExtension {
    ArrayList<String> targets = RustTargetType.values().collect { it.id }
    String module = null
    String targetDirectory = null
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

    NamedDomainObjectContainer<CargoNdkBuildTypeExtension> buildTypeContainer

    CargoNdkExtension(Project project) {
        buildTypeContainer = project.container(CargoNdkBuildTypeExtension)
        buildTypeContainer.create("release")
        buildTypeContainer.create("debug")
    }

    void buildTypes(
            Action<? super NamedDomainObjectContainer<CargoNdkBuildTypeExtension>> action) {
        action.execute(buildTypeContainer)
    }
}

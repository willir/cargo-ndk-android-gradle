package com.github.willir.rust

import java.lang.IllegalArgumentException

enum RustTargetType {
    ARM64("arm64", "aarch64-linux-android", "arm64-v8a"),
    ARM_V7("arm", "armv7-linux-androideabi", "armeabi-v7a"),
    X86("x86", "i686-linux-android", "x86"),
    X86_64("x86_64", "x86_64-linux-android", "x86_64"),

    final String id
    final String rustTarget
    final String jniLibDirName

    private RustTargetType(String id, String rustTarget, String jniLibDirName) {
        this.id = id
        this.rustTarget = rustTarget
        this.jniLibDirName = jniLibDirName
    }

    static void validateTargetIds(ArrayList<String> ids) {
        ids.each { fromId(it) }
    }

    static RustTargetType fromId(String id) {
        String idLow = id.toLowerCase()
        RustTargetType res = values().find {
            it.id == idLow || it.rustTarget == idLow || it.jniLibDirName == idLow
        }
        if (res != null) {
            return res
        } else if (idLow == "arm_v7" || idLow == "armv7") {
            return ARM_V7
        } else {
            throw new IllegalArgumentException(
                    "Wrong rust target: '" + id + "' supported: " + values().collect { it.id })
        }
    }

    boolean is64Bit() {
        return this == ARM64 || this == X86_64
    }
}

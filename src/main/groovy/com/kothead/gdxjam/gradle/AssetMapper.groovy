package com.kothead.gdxjam.gradle

import org.gradle.util.Configurable

class AssetMapper implements Configurable<AssetMapper> {

    String name
    Closure mapper

    AssetMapper(String name) {
        this.name = name
    }

    AssetMapper configure(Closure closure) {
        mapper = closure 
        return this
    }

    def getAssets(File file) {
        if (!mapper) return []
        toMap(mapper(file)).findAll { it.key && it.value }
    }

    private def toMap(asset) {
        switch (asset) {
            case Map:
                println "it's a map"
                return asset

            case List:
                println "it's a list"
                return asset
                        .collectEntries {[(it): it]};

            case String[]:
                println "it's an array"
                return asset.toList()
                        .collectEntries {[(it): it]};

            case String:
                println "it's a string"
                return [(asset): asset] 

            case true:
                println "it's a plane"
                return [(asset.name.take(asset.name.lastIndexOf('.'))): asset.path]

            default:
                println "nothin I know" 
                return [:]
        }
    }
}


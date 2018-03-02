package com.kothead.gdxjam.gradle

import groovy.transform.Synchronized

import org.gradle.util.Configurable

class AssetMapper implements Configurable<AssetMapper> {

    private String name
    private Closure mapper
    private List<AssetMapping> mappings 
    private File file

    AssetMapper(String name) {
        this.name = name
    }

    AssetMapper configure(Closure closure) {
        mapper = closure 
        mapper.delegate = this
        return this
    }

    def asset(Closure closure) {
        AssetMapping mapping = new AssetMapping()
        mapping.setFile(file)

        closure.delegate = mapping
        closure()
        mappings << mapping
    }

    @Synchronized
    def getAssets(File file) {
        this.file = file
        if (!mapper) return []

        mappings = []
        mapper(file)
        return mappings
    }
}


package com.kothead.gdxjam.gradle

import groovy.transform.Synchronized

import org.gradle.util.Configurable

class AssetMapper implements Configurable<AssetMapper> {

    private String name
    private Closure mapper
    private List<AssetMapping> mappings 

    private File inputDir
    private File file

    AssetMapper(String name) {
        this.name = name
    }

    AssetMapper configure(Closure closure) {
        mapper = closure 
        mapper.delegate = this
        return this
    }

    def getFile() {
        return file
    }

    def asset(Closure closure) {
        AssetMapping mapping = new AssetMapping()
        mapping.setFile(file)
        mapping.setFileName(inputDir
                .toPath()
                .relativize(file.toPath())
                .toString())

        closure.delegate = mapping
        closure()
        mappings << mapping
    }

    @Synchronized
    def getAssets(File inputDir, File file) {
        this.inputDir = inputDir
        this.file = file
        if (!mapper) return []

        mappings = []
        mapper(file)
        return mappings
    }
}


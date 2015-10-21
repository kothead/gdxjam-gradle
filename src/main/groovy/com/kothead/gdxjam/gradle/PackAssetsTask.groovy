package com.kothead.gdxjam.gradle

import java.io.File

import com.badlogic.gdx.tools.texturepacker.TexturePacker
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class PackAssetsTask extends DefaultTask {
    @InputDirectory
    def File inputDir 

    @OutputDirectory
    def File outputDir
    
    @Input
    def Settings settings = project.configure(new Settings()) {
        maxWidth = 2048
        maxHeight = 2048
        edgePadding = true
        duplicatePadding = true
        paddingX = 4
        paddingY = 4
    }
    
    @TaskAction
    def pack(IncrementalTaskInputs inputs) {
        def type = inputs.incremental ? "CHANGED" : "ALL"
        println "$type inputs considered out of date"

        if (!inputs.incremental) {
            project.delete(outputDir.listFiles())
        }
        
        def outdated = [] 
        def removed = []
        inputs.outOfDate { outdated << it.file.path }
        inputs.removed { removed << it.file.path }

        inputDir.listFiles().findAll { 
            boolean hasOutdated = hasOutdatedFile(it, outdated)
            boolean hasRemoved = hasRemovedFile(it, removed)
            if (hasOutdated) println "${it.getName()} has outdated files"
            if (hasRemoved) println "${it.getName()} has removed files"
            it.isDirectory() && (hasOutdated || hasRemoved)
        }.collect {
            TexturePacker.process(settings, it.path, outputDir.path, it.name)
        }
    }
    
    boolean hasOutdatedFile(File directory, List outdated) {
        directory.listFiles().find {
            it.path in outdated
        }
    }

    boolean hasRemovedFile(File directory, List removed) {
        removed.find {
            def path = it.substring(0, it.lastIndexOf(File.pathSeparatorChar))
            path == directory.getPath()
        }
    }

}

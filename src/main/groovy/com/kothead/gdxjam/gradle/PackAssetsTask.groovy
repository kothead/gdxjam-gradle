package com.kothead.gdxjam.gradle

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings

class PackAssetsTask extends DefaultTask {
    @InputDirectory
    File inputDir

    @OutputDirectory
    File outputDir
    
    @Input
    Settings settings = configure(new Settings()) {
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
        inputs.outOfDate { outdated << it }
        inputs.removed { removed << it }

        inputDir.listFiles().findAll { 
            boolean hasOutdated = hasOutdatedFile(it, outdated)
            boolean hasRemoved = hasRemovedFile(it, removed)
            it.isDirectory() && (hasOutdated || hasRemoved)
        }.collect {
            TexturePacker.process(settings, it.getPath(), outDir, it.getName())
        }
    }
    
    private boolean hasOutdatedFile(File directory, List outdated) {
        directory.listFiles().find {
            it.getName() in outdated
        }
    }

    private boolean hasRemovedFile(File directory, List removed) {
        removed.find {
            def path = it.substring(0, it.lastIndexOf(File.pathSeparatorChar))
            path == directory.getPath()
        }
    }
}

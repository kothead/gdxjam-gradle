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
    File inputDir = new File('input')

    @OutputDirectory
    File outputDir = new File('output')
    
    @Input
    Settings settings = new Settings()

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
            TexturePacker.process(settings.build(), it.path, outputDir.path, it.name)
        }
    }
    
    boolean hasOutdatedFile(File directory, List outdated) {
        directory.listFiles().find {
            it.path in outdated
        }
    }

    boolean hasRemovedFile(File directory, List removed) {
        removed.find {
            def path = it.substring(0, it.lastIndexOf(File.separator))
            path == directory.getPath()
        }
    }

}

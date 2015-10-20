package com.kothead.gdxjam.gradle

import org.junit.Test

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

import static org.junit.Assert.*

class PackAssetsTaskTest {
    @Test
    public void packsAssetsIntoBatches() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'gdxjam'

        def task = project.tasks.packAssets
        task.inputDir = project.file(getClass().getClassLoader().getResource("input"))
        task.outputDir = project.file("output")
        task.execute()

        boolean noBatch = inputDir.listFiles().find {
            (project.file(outputDir, it + ".atlas").exists()
            && project.file(outputDir, it + ".png").exists())
        } 
        assertFalse(noBatch)
    }
}

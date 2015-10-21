package com.kothead.gdxjam.gradle

import java.io.File

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

        boolean noBatch = task.inputDir.listFiles().find {
            !(new File(task.outputDir, it.getName() + ".atlas").exists()
            && new File(task.outputDir, it.getName() + ".png").exists())
        } 
        assertFalse(noBatch)
    }
}

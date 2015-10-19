package com.kothead.gdxjam.gradle

import org.junit.Test

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

import static org.junit.Assert.*

class GdxJamPluginTest {
    @Test
    public void gdxJamPluginAddsPackAssetsTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'gdxjam'

        assertTrue(project.tasks.packAssets instanceof PackAssetsTask)
    }
}

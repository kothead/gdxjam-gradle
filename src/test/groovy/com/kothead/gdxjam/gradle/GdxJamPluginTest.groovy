package com.kothead.gdxjam.gradle

import org.junit.Test

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import spock.lang.Specification

class GdxJamPluginTest extends Specification {
    
    def "gdxjam tasks are available"() {
        given:
            Project project = ProjectBuilder.builder().build()

        when: 
            project.pluginManager.apply "gdxjam"

        then:
            project.tasks.packAssets instanceof PackAssetsTask
    }
}

package com.kothead.gdxjam.gradle

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.*

class GatherAssetsTaskTest extends Specification {

    @Rule TemporaryFolder projectDir = new TemporaryFolder()
    File buildFile
    File inputDir 
    File outputDir

    def setup() {
        buildFile = projectDir.newFile("build.gradle")
        outputDir = projectDir.newFolder("output")

        ClassLoader loader = getClass().classLoader
        inputDir = new File(loader.getResource("input").getFile())
    }

    def "gather assets in resources class"() {
        given:
            buildFile << """
                plugins {
                    id "gdxjam"
                }

                gatherAssets {
                    assetsPackage = 'com.kothead.gdxjam.gradle.test'
                    inputDir = file('$inputDir.absolutePath')
                    outputDir = file('$outputDir.absolutePath')

                    mappers {
                        "batch1/*" {
                            [(filename(it)): relative(it)]
                        }
                        "batch2/*" {
                            [(filename(it)): "VALUE_" + filename(it).toUpperCase()]
                        }
                    }
                }
            """

        when:
            def result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments("gatherAssets")
                .withPluginClasspath()
                .build()

        then:
            outputDir.traverse(type: groovy.io.FileType.FILES) { it -> println it }
            println result.output

            File resources = new File(outputDir, "com/kothead/gdxjam/gradle/test/Assets.java")
            resources.exists()
    }
}

package com.kothead.gdxjam.gradle

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.*

class PackAssetsTaskTest extends Specification {

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

    def "pack assets into batches"() {
        given:
            buildFile << """
                plugins {
                    id "gdxjam"
                }

                packAssets {
                    inputDir = file('$inputDir.absolutePath')
                    outputDir = file('$outputDir.absolutePath')
                }
            """

        when:
            def result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments("packAssets")
                .withPluginClasspath()
                .build()

        then:
            result.task(":packAssets").outcome == SUCCESS
            inputDir.listFiles().find {
                new File(outputDir, it.name + ".atlas").exists() ||
                new File(outputDir, it.name + ".png").exists()
            }
    }

    List parseSprites(String name) {
        def list = new File(outputDir, name).readLines()[2..-1].findAll {
            it.length() > 0 && !it.contains(":")
        }
        list.collect { println "batch $name contains " + it }
        return list
    }
}

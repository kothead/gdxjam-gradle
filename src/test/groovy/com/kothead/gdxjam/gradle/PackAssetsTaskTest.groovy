package com.kothead.gdxjam.gradle

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset

import org.junit.Test
import org.junit.Before
import org.junit.After

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.GradleTask

import static org.junit.Assert.*

class PackAssetsTaskTest {

    ProjectConnection connection
    BuildLauncher launcher
    File inputDir 
    File outputDir
    ByteArrayOutputStream stdout 

    @Before
    void connectToProject() {
        ClassLoader loader = getClass().classLoader
        def projectDir = new File(loader.getResource("consumer").getFile()) 
        inputDir = new File(loader.getResource("input").getFile())
        outputDir = new File(projectDir, "output")
        stdout = new ByteArrayOutputStream()

        GradleConnector connector = GradleConnector.newConnector()
        connector.forProjectDirectory(projectDir)
        connection = connector.connect()
        launcher = connection.newBuild()
        launcher.setStandardOutput(stdout)
        launcher.forTasks("packAssets")
    }

    @Test
    void packAssetsIntoBatches() {
        launcher.run()

        boolean noBatch = inputDir.listFiles().find {
            !(new File(outputDir, it.name + ".atlas").exists()
            && new File(outputDir, it.name + ".png").exists())
        }
        assertFalse(noBatch)
    }

    @Test
    void repacksAssetsAfterFileRemove() {
        def from = new File(inputDir, "batch1/pic1.png")
        def to = new File(inputDir, "batch1/copy.png")
        to << from.bytes 

        launcher.run()
        assertTrue('copy' in parseSprites("batch1.atlas"))

        to.delete()
        launcher.run()
        assertFalse('copy' in parseSprites("batch1.atlas"))
    }

    List parseSprites(String name) {
        def list = new File(outputDir, name).readLines()[2..-1].findAll {
            it.length() > 0 && !it.contains(':')
        }
        list.collect { println "batch $name contains " + it }
        return list
    }

    @After
    void disconnectFromProject() {
        connection.close()
        println "task output: ${stdout.toString()}"
    }
}

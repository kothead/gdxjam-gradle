package com.kothead.gdxjam.gradle

import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.badlogic.gdx.assets.AssetDescriptor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeName

import groovy.io.FileType
import groovy.util.FileNameFinder

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import static javax.lang.model.element.Modifier.PUBLIC
import static javax.lang.model.element.Modifier.STATIC
import static javax.lang.model.element.Modifier.FINAL

class GatherAssetsTask extends DefaultTask {

    NamedDomainObjectContainer<AssetMapper> mappers = project.container(AssetMapper)

    @InputDirectory
    File inputDir = new File('input')

    @Input
    String assetsPackage 

    @Input
    String assetsClass = "Assets"

    @OutputDirectory
    File outputDir = new File('output')

    @TaskAction
    def gather(IncrementalTaskInputs inputs) {
        def type = inputs.incremental ? "CHANGED" : "ALL"
        println "$type inputs considered out of date"
        //println "group is $project.android.applicationId"

        def tree = [:]
        mappers.each {
            def files = new FileNameFinder().getFileNames(inputDir.absolutePath, it.name)
            merge(tree, collectAssetTree(it, inputDir, files))
        }
        
        def (TypeSpec spec) = generateAssetsType(assetsClass, tree)
        JavaFile file = JavaFile.builder(assetsPackage, spec).build() 
        file.writeTo(System.out)

        try {
            file.writeTo(outputDir)
        } catch (IOException e) {
            def path = assetsPackage.replace(".", "/") + assetsClass + ".java"
            throw new GradleException("Could not write $assetsClass to $path")
        }

        outputDir.eachFileRecurse {
            println it.path
        }
    }

    def mappers(Closure configureClosure) {
        mappers.configure(configureClosure)    
    }

    def filename(File file) {
        file.name.take(file.name.lastIndexOf('.'))
    }
    
    def relative(File file) {
        inputDir.toPath()
                .relativize(file.toPath())
                .toFile()
    }

    protected def generateAssetsType(String name, Map assetTree) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(PUBLIC, FINAL)

        String[] fieldNames = []

        assetTree.each {
            if (!it.key || !it.value) return
            if (it.value in Map) {
                def (typeSpec, subfields) = generateAssetsType(it.key, it.value)
                builder.addType(typeSpec)

                subfields = subfields.collect { "${typeSpec.name}.$it" }
                fieldNames += subfields
            } else {
                AssetMapping mapping = it.value
                ClassName descriptorClassName = ClassName.get(AssetDescriptor)
                ClassName assetClassName = ClassName.get(mapping.assetType)
                TypeName descriptorType = ParameterizedTypeName.get(
                        descriptorClassName,
                        assetClassName);

                def initializer = CodeBlock.builder()
                        .add("new \$T<>(\$S, \$T.class", 
                                descriptorClassName,
                                relative(mapping.file),
                                mapping.assetType)
                if (mapping.paramType) {
                    //ClassName paramClassName = ClassName.get(mapping.paramType)
                    def parameters = ("\$L" * mapping.params.length).join(", ")
                    initializer.add(", new \$T(", mapping.paramType)
                            .add(parameters, mapping.params)
                            .add(")")
                }
                initializer.add(")")

                def field = FieldSpec.builder(descriptorType, mapping.fieldName.toUpperCase())
                        .addModifiers(PUBLIC, STATIC, FINAL)
                        .initializer(initializer.build())
                        .build()
                builder.addField(field)
                fieldNames += field.name
            }
        }

        //def constants = collectAssetList(assetTree)
        //CodeBlock array = CodeBlock.builder()
        //        .add("{\$N}", constants.join(", "))
        //        .build()

        builder.addField(FieldSpec.builder(String[], "ALL")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .initializer("{" + (["\$N"] * fieldNames.length).join(", ") + "}", fieldNames)
                .build())
        
        return [builder.build(), fieldNames]
    }

    protected def collectAssetList(Map assetTree) {
        def assets = []
        assetTree.each {
            if (it.value in Map) {
                assets += collectAssetList(it.value)
            } else {
                assets << it.value.fieldName.toUpperCase()
            }
        }
        return assets
    }

    protected def collectAssetTree(AssetMapper mapper, File dir, List files) {
        def tree = [:]

        dir.eachDir {
            def subTree = collectAssetTree(mapper, it, files)
            if (subTree) {
                tree[it.name] = subTree
            }
        }

        dir.eachFile(FileType.FILES) {
            if (it.absolutePath in files) {
                mapper.getAssets(it).each {
                    tree[it.fieldName] = it
                }
            }
        }

        return tree
    }

    protected def merge(Map first, Map second) {
        second.each {
            def value = first[it.key]
            if (value in Map && it.value in Map) {
                merge(value, it.value)
            } else {
                first << it 
            }
        }
    }
}

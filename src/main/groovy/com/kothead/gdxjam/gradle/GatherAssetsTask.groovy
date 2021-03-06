package com.kothead.gdxjam.gradle

import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.badlogic.gdx.assets.AssetDescriptor

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeName

import groovy.io.FileType
import groovy.util.FileNameFinder

import javax.lang.model.element.Modifier

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import static javax.lang.model.element.Modifier.PUBLIC
import static javax.lang.model.element.Modifier.STATIC
import static javax.lang.model.element.Modifier.FINAL

class GatherAssetsTask extends DefaultTask {

    protected static final String FIELD_ALL = "ALL"

    NamedDomainObjectContainer<AssetMapper> mappers = project.container(AssetMapper)

    @InputFiles
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
        
        TypeSpec spec = generateAssetsType(assetsClass, tree, PUBLIC, FINAL)
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

    protected TypeSpec generateAssetsType(String name, Map assetTree, Modifier... modifiers) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(modifiers)

        assetTree.each {
            if (!it.key || !it.value) return
            if (it.value in Map) {
                builder.addType(generateAssetsType(it.key, it.value, PUBLIC, STATIC, FINAL))
                //subfields = subfields.collect { "${typeSpec.name}.$it" }
            } else {
                AssetMapping mapping = it.value
                ClassName descriptorClassName = ClassName.get(AssetDescriptor)
                ClassName assetClassName = ClassName.get(mapping.assetType)
                TypeName descriptorType = ParameterizedTypeName.get(
                        descriptorClassName,
                        assetClassName);

                def initializer = CodeBlock.builder()
                        .add("new \$T(\$S, \$T.class", 
                                descriptorType,
                                mapping.fileName,
                                mapping.assetType)
                if (mapping.paramType) {
                    ClassName paramClassName = ClassName.get(mapping.paramType)
                    initializer.add(", new \$T(\$L)", mapping.paramType, mapping.params)
                }
                initializer.add(")")

                builder.addField(FieldSpec.builder(descriptorType, mapping.fieldName.toUpperCase())
                        .addModifiers(PUBLIC, STATIC, FINAL)
                        .initializer(initializer.build())
                        .build())
            }
        }

        def prebuild = builder.build()
        def all = collectAssetList(prebuild) 
        if (all) {
            all = toArrayInitializer(all as String[])
            def type = ArrayTypeName.of(TypeName.get(AssetDescriptor))

            builder.addField(FieldSpec.builder(type, FIELD_ALL)
                    .addModifiers(PUBLIC, STATIC, FINAL)
                    .initializer(all)
                    .build())
        }

        builder.build()
    }

    private TypeName getCommonTypeName(TypeSpec typeSpec) {
        def typeNames = []

        typeSpec.fieldSpecs.each { 
            typeNames << it.type
        }

        typeSpec.typeSpecs.each {
            typeNames << it.fieldSpecs.find {
                it.name == FIELD_ALL
            }.type.componentType
        }

        return getCommon(typeNames, TypeName.get(AssetDescriptor))
    }

    private def getCommon(List values, def defaultValue) {
        def common = values ? values[0] : defaultValue
        for (value in values) {
            if (value != common) return defaultValue
        }
        return common
    }

    protected List<String> collectAssetList(TypeSpec typeSpec) {
        def assets = []
        typeSpec.fieldSpecs
                .findAll { it.name != FIELD_ALL }
                .each { assets << it.name }
        typeSpec.typeSpecs.each {
            def typeName = it.name
            assets += collectAssetList(it).collect { "$typeName.$it" }
        }
        return assets
    }

    private CodeBlock toArrayInitializer(String[] names) {
        CodeBlock.builder()
                .add("{")
                .add((["\$N"] * names.length).join(", "), names)
                .add("}")
                .build()
    }

    protected Map collectAssetTree(AssetMapper mapper, File dir, List files) {
        def tree = [:]

        dir.eachDir {
            def subTree = collectAssetTree(mapper, it, files)
            if (subTree) {
                tree[it.name] = subTree
            }
        }

        dir.eachFile(FileType.FILES) {
            if (it.absolutePath in files) {
                mapper.getAssets(inputDir, it).each {
                    tree[it.fieldName] = it
                }
            }
        }

        return tree
    }

    protected Map merge(Map first, Map second) {
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

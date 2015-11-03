package com.kothead.gdxjam.gradle

import java.io.Serializable

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.Texture.TextureWrap
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import org.codehaus.groovy.runtime.InvokerHelper

@ToString
@EqualsAndHashCode
class Settings implements Serializable {
    private static final long serialVersionUID = 5568529547642028198L 

    Settings() {
        TexturePacker.Settings settings = new TexturePacker.Settings()
        def fields = settings.class.declaredFields.findAll { 
            it.modifiers == java.lang.reflect.Modifier.PUBLIC
        }.collectEntries { [it.name, settings[it.name]] }
        use(InvokerHelper) {
            this.setProperties(fields)
        }
    }

    boolean pot
    int paddingX
    int paddingY
    boolean edgePadding
    boolean duplicatePadding
    boolean rotation
    int minWidth
    int minHeight
    int maxWidth
    int maxHeight
    boolean square
    boolean stripWhitespaceX
    boolean stripWhitespaceY
    int alphaThreshold
    TextureFilter filterMin
    TextureFilter filterMag
    TextureWrap wrapX
    TextureWrap wrapY
    Format format
    boolean alias
    String outputFormat
    float jpegQuality
    boolean ignoreBlankImages
    boolean fast
    boolean debug
    boolean silent
    boolean combineSubdirectories
    boolean flattenPaths
    boolean premultiplyAlpha
    boolean useIndexes
    boolean bleed
    boolean limitMemory
    boolean grid
    float[] scale
    String[] scaleSuffix
    String atlasExtension

    TexturePacker.Settings build() {
        TexturePacker.Settings settings = new TexturePacker.Settings()
        use(InvokerHelper) {
            settings.setProperties(properties)
        }
        return settings
    }
}

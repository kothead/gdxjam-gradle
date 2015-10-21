package com.kothead.gdxjam.gradle

import java.io.Serializable

import com.badlogic.gdx.tools.texturepacker.TexturePacker

class Settings extends TexturePacker.Settings implements Serializable {
    private static final long serialVersionUID = 5568529547642028198L 

    public Settings() {
    }

    public Settings(Settings settings) {
        super(settings)
    }
}

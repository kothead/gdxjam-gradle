package com.kothead.gdxjam.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

class GdxJamPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.task('packAssets', type: PackAssetsTask)
        project.task('gatherAssets', type: GatherAssetsTask)
    }
}

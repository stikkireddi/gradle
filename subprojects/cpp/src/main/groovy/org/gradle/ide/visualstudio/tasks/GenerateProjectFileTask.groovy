/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.ide.visualstudio.tasks
import org.gradle.api.Incubating
import org.gradle.ide.visualstudio.internal.VisualStudioProject
import org.gradle.ide.visualstudio.tasks.internal.AbsoluteFileNameTransformer
import org.gradle.ide.visualstudio.tasks.internal.VisualStudioProjectFile
import org.gradle.plugins.ide.api.GeneratorTask
import org.gradle.plugins.ide.internal.generator.generator.PersistableConfigurationObject
import org.gradle.plugins.ide.internal.generator.generator.PersistableConfigurationObjectGenerator

@Incubating
class GenerateProjectFileTask extends GeneratorTask<PersistableConfigurationObject> {
    VisualStudioProject vsProject

    GenerateProjectFileTask() {
        generator = new ConfigurationObjectGenerator()
    }

    void setVisualStudioProject(VisualStudioProject vsProject) {
        this.vsProject = vsProject
        setOutputFile(vsProject.getProjectFile())
    }

    @Override
    File getInputFile() {
        return null
    }

    private class ConfigurationObjectGenerator extends PersistableConfigurationObjectGenerator<PersistableConfigurationObject> {
        public PersistableConfigurationObject create() {
            return new VisualStudioProjectFile(new AbsoluteFileNameTransformer())
        }

        public void configure(PersistableConfigurationObject object) {
            VisualStudioProjectFile projectFile = object as VisualStudioProjectFile;
            VisualStudioProject vsProject = GenerateProjectFileTask.this.vsProject

            projectFile.setProjectUuid(vsProject.uuid)
            vsProject.sourceFiles.each {
                projectFile.addSourceFile(it)
            }
            vsProject.resourceFiles.each {
                projectFile.addResource(it)
            }
            vsProject.headerFiles.each {
                projectFile.addHeaderFile(it)
            }

            vsProject.configurations.each {
                projectFile.addConfiguration(it)
            }

            vsProject.projectReferences.each { VisualStudioProject projectReference ->
                projectFile.addProjectReference(projectReference)
            }
        }
    }
}
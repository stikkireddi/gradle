/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.ide.visualstudio.tasks.internal
import org.gradle.api.internal.file.FileResolver
import org.gradle.ide.visualstudio.fixtures.SolutionFile
import org.gradle.ide.visualstudio.internal.VisualStudioProject
import org.gradle.ide.visualstudio.internal.VisualStudioProjectConfiguration
import org.gradle.ide.visualstudio.internal.VisualStudioProjectResolver
import org.gradle.nativebinaries.NativeBinary
import org.gradle.nativebinaries.internal.DefaultBuildType
import org.gradle.nativebinaries.internal.DefaultPlatform
import org.gradle.nativebinaries.internal.NativeComponentInternal
import org.gradle.test.fixtures.file.TestDirectoryProvider
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import spock.lang.Specification

class VisualStudioSolutionFileTest extends Specification {
    TestDirectoryProvider testDirectoryProvider = new TestNameTestDirectoryProvider()
    def fileResolver = Mock(FileResolver)
    def projectResolver = Mock(VisualStudioProjectResolver)
    def solutionFile = new VisualStudioSolutionFile()

    def "setup"() {
        solutionFile.loadDefaults()
    }

    def "empty solution file"() {
        when:
        solutionFile.solutionConfiguration = "CONFIG"
        then:
        generatedSolution.content ==
"""Microsoft Visual Studio Solution File, Format Version 11.00
# Visual C++ Express 2010

Global
    GlobalSection(SolutionConfigurationPlatforms) = preSolution
        CONFIG=CONFIG
    EndGlobalSection
    GlobalSection(ProjectConfigurationPlatforms) = postSolution
    EndGlobalSection
    GlobalSection(SolutionProperties) = preSolution
        HideSolutionNode = FALSE
    EndGlobalSection
EndGlobal
"""
    }

    def "includes project references"() {
        when:
        final project1File = new File("project1")
        fileResolver.resolve("visualStudio/project1.vcxproj") >> project1File
        def binary1 = binary("one")
        def project1 = new VisualStudioProject("project1", binary1.component, fileResolver, projectResolver)
        def configuration1 = new VisualStudioProjectConfiguration(project1, binary1, "type")
        solutionFile.addProjectConfiguration(configuration1)

        final project2File = new File("project2")
        fileResolver.resolve("visualStudio/project2.vcxproj") >> project2File
        def binary2 = binary("two")
        def project2 = new VisualStudioProject("project2", binary2.component, fileResolver, projectResolver)
        def configuration2 = new VisualStudioProjectConfiguration(project2, binary2, "type")
        solutionFile.addProjectConfiguration(configuration2)

        then:
        with (generatedSolution.projects['project1']) {
            file == project1File.absolutePath
            uuid == project1.uuid
            configurations == ['debug|Win32':'debug|Win32']
        }
        with (generatedSolution.projects['project2']) {
            file == project2File.absolutePath
            uuid == project2.uuid
            configurations == ['debug|Win32':'debug|Win32']
        }
    }

    private NativeBinary binary(def name) {
        def component = Mock(NativeComponentInternal)
        def binary = Mock(NativeBinary)
        component.name >> "${name}Component"
        component.projectPath >> "project-path"
        binary.name >> name
        binary.component >> component
        binary.buildType >> new DefaultBuildType("debug")
        binary.targetPlatform >> new DefaultPlatform("win32")
        return binary
    }

    private SolutionFile getGeneratedSolution() {
        def file = testDirectoryProvider.testDirectory.file("solution.txt")
        solutionFile.store(file)
        return new SolutionFile(file)
    }
}
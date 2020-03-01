/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.yamlpatch

import static org.assertj.core.api.Assertions.assertThat

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import groovy.transform.CompileStatic
import java.util.function.UnaryOperator
import org.junit.jupiter.api.Test

@CompileStatic
class YamlPatcherTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()

    private final YamlPatcher yamlPatcher = new YamlPatcher({ ObjectMapper objectMapper ->
        objectMapper.registerModule(new Jdk8Module());
    })

    @Test
    void 'can patch a simple string value replacement'() {
        // language=yaml
        String input = """
            # a comment
            foo:
              # another comment
              bar: baz # so many comment
        """.stripIndent()

        String output = yamlPatcher.patchYaml(
                input,
                TestObjects.Foo,
                TestObjects.Foo.withValues(
                        { baz -> baz + "andmore" },
                        { quux -> Optional.empty() }))

        // language=yaml
        assertThat(output).isEqualTo """
            # a comment
            foo:
              # another comment
              bar: bazandmore # so many comment
        """.stripIndent()
    }

    @Test
    void 'can patch multi string value replacements'() {
        // language=yaml
        String input = """
            # a comment
            foo:
              # another comment
              bar: baz # so many comment
              quux: goop # even more comment
        """.stripIndent()

        String output = yamlPatcher.patchYaml(
                input,
                TestObjects.Foo,
                TestObjects.Foo.withValues(
                        { baz -> baz + "andmore" },
                        { quux -> Optional.of("abcdef") }))

        // language=yaml
        assertThat(output).isEqualTo """
            # a comment
            foo:
              # another comment
              bar: bazandmore # so many comment
              quux: abcdef # even more comment
        """.stripIndent()
    }

    @Test
    void 'can patch remove a mapping from an object'() {
        // language=yaml
        String input = """
            # a comment
            foo:
              # another comment
              bar: baz # so many comment
              # multiline fun comment
              # it's really multiline
              quux: goop # even more comment
        """.stripIndent()

        String output = yamlPatcher.patchYaml(
                input,
                TestObjects.Foo,
                TestObjects.Foo.withValues(
                         { baz -> baz },
                        { quux -> Optional.empty() }))

        // language=yaml
        assertThat(output).isEqualTo """
            # a comment
            foo:
              # another comment
              bar: baz # so many comment
        """.stripIndent()
    }

    @Test
    void 'can add a mapping to an object'() {
        // language=yaml
        String input = '''
          # a comment
          foo:
            # before
            bar: baz # trailing
            # random other comment
        '''.stripIndent()

        String output = yamlPatcher.patchYaml(input,
                TestObjects.Foo,
                TestObjects.Foo.withValues(
                        { baz -> baz },
                        { quux -> Optional.of("now exists") }))

        // language=yaml
        assertThat(output).isEqualTo '''
          # a comment
          foo:
            # before
            bar: baz # trailing
            # random other comment
            quux: now exists
        '''.stripIndent()
    }

    @Test
    void 'can insert a complex object with one key value mapping into an existing object'() {
        // language=yaml
        String input = '''
          # comment
          foo:
          # comment
        '''.stripIndent()

        String output = yamlPatcher.patchYaml(
                input,
                TestObjects.Foo,
                (UnaryOperator<TestObjects.Foo>) { foo -> ImmutableFoo.builder()
                        .foo(ImmutableBar.builder()
                                .bar("baz")
                                .build())
                        .build() })

        // language=yaml
        assertThat(output).isEqualTo '''
          # comment
          foo:
            bar: baz
          # comment
        '''.stripIndent()
    }

    @Test
    void 'can insert a complex object with multiple key value mappings into an existing object'() {
        // language=yaml
        String input = '''
          # comment
          foo:
          # comment
        '''.stripIndent()

        String output = yamlPatcher.patchYaml(
                input,
                TestObjects.Foo,
                (UnaryOperator<TestObjects.Foo>) { foo -> ImmutableFoo.builder()
                        .foo(ImmutableBar.builder()
                                .bar("baz")
                                .quux("goop")
                                .build())
                        .build() })

        // language=yaml
        assertThat(output).isEqualTo '''
          # comment
          foo:
            bar: baz
            quux: goop
          # comment
        '''.stripIndent()
    }

    @Test
    void 'can insert a complex object into an already nested object'() {
        // language=yaml
        String input = '''
          one:
            two:
              # comment
              three: 3
              # other comment
              threePrime: 3'
        '''.stripIndent()

        String output = yamlPatcher.patchYaml(input, JsonNode.class, { JsonNode json ->
            ((ObjectNode) json.get("one").get("two"))
                    .replace('three', OBJECT_MAPPER.createObjectNode()
                            .put("a", 1)
                            .put("b", 2))
            return json
        } as UnaryOperator<JsonNode>)

        // language=yaml
        assertThat(output).isEqualTo '''
          one:
            two:
              # comment
              three:
                a: 1
                b: 2
              # other comment
              threePrime: 3\'
        '''.stripIndent()
    }
}

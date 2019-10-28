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

package com.palantir.yamlpatch;

import org.immutables.value.Value;

@Value.Immutable
abstract class Patch {
    public abstract Range range();
    public abstract String replacement();

    public static class Builder extends ImmutablePatch.Builder { }

    public static Builder builder() {
        return new Builder();
    }

    public static Patch of(int startIndex, int endIndex, String replacement) {
        return builder()
                .range(Range.builder()
                        .startIndex(startIndex)
                        .endIndex(endIndex)
                        .build())
                .replacement(replacement)
                .build();
    }
}

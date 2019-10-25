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

package com.palantir.gradle.revapi.config.v1;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.gradle.revapi.config.FlattenedBreak;
import com.palantir.gradle.revapi.config.JustificationAndVersion;
import com.palantir.gradle.revapi.config.PerProject;
import com.palantir.gradle.revapi.config.Version;
import com.palantir.gradle.revapi.config.v2.AcceptedBreak;
import com.palantir.gradle.revapi.config.v2.BreakCollection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import one.util.streamex.EntryStream;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableDeprecatedAcceptedBreaks.class)
public abstract class DeprecatedAcceptedBreaks {
    @JsonValue
    protected abstract Map<Version, PerProject<AcceptedBreakV1>> acceptedBreakV1s();

    private Map<JustificationAndVersion, List<FlattenedBreak>> flattenedBreaks() {
        return EntryStream.of(acceptedBreakV1s())
                .flatMapKeyValue((version, perProjectAcceptedBreaks) ->
                        perProjectAcceptedBreaks.flatten((groupAndName, acceptedBreakV1) ->
                                acceptedBreakV1.flatten(version, groupAndName)))
                .collect(Collectors.groupingBy(FlattenedBreak::justificationAndVersion));
    }

    public final Set<BreakCollection> upgrade() {
        return EntryStream.of(flattenedBreaks()).mapKeyValue((justificationAndVersion, flattenedBreaks) -> {
            PerProject<AcceptedBreak> perProjectAcceptedBreaks = PerProject
                    .groupingBy(flattenedBreaks, FlattenedBreak::groupAndName)
                    .map(FlattenedBreak::acceptedBreak);

            return BreakCollection.builder()
                    .justification(justificationAndVersion.justification())
                    .afterVersion(justificationAndVersion.version())
                    .breaks(perProjectAcceptedBreaks)
                    .build();
        }).collect(Collectors.toSet());
    }

    public static final class Builder extends ImmutableDeprecatedAcceptedBreaks.Builder { }

    public static Builder builder() {
        return new Builder();
    }

    public static DeprecatedAcceptedBreaks empty() {
        return builder().build();
    }
}
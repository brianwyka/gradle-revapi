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

package com.palantir.gradle.revapi;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

final class GradleUtils {
    private GradleUtils() {}

    public static <T> Provider<T> memoisedProvider(Project project, Supplier<T> supplier) {
        Supplier<T> memoised = Suppliers.memoize(supplier::get);
        return project.provider(memoised::get);
    }
}
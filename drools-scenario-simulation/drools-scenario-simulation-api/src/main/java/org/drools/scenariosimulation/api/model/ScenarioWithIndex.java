/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.drools.scenariosimulation.api.model;

import java.util.Objects;

/**
 * Tuple with Scenario and its index
 */
public class ScenarioWithIndex {

    protected Scenario scenario;
    protected int index;

    public ScenarioWithIndex() {
        // CDI
    }

    public ScenarioWithIndex(int index, Scenario scenario) {
        this.scenario = scenario;
        this.index = index;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScenarioWithIndex that = (ScenarioWithIndex) o;
        return index == that.index &&
                Objects.equals(scenario, that.scenario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenario, index);
    }
}
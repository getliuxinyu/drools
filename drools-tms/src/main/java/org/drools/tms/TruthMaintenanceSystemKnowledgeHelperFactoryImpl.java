/*
 * Copyright (c) 2021. Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.tms;

import org.drools.core.common.ReteEvaluator;
import org.drools.core.spi.KnowledgeHelper;
import org.drools.kiesession.factory.KnowledgeHelperFactory;

public class TruthMaintenanceSystemKnowledgeHelperFactoryImpl implements KnowledgeHelperFactory {

    @Override
    public KnowledgeHelper createKnowledgeHelper(ReteEvaluator reteEvaluator) {
        return new TruthMaintenanceSystemKnowledgeHelper( reteEvaluator );
    }
}

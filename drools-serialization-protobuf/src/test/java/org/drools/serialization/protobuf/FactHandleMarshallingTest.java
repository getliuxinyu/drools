/*
 * Copyright (c) 2020. Red Hat, Inc. and/or its affiliates.
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

package org.drools.serialization.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;

import org.drools.core.SessionConfiguration;
import org.drools.core.WorkingMemoryEntryPoint;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.kiesession.entrypoints.NamedEntryPoint;
import org.drools.core.common.RuleBasePartitionId;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.kiesession.rulebase.InternalKnowledgeBase;
import org.drools.core.impl.RuleBaseFactory;
import org.drools.serialization.protobuf.marshalling.InputMarshaller;
import org.drools.serialization.protobuf.marshalling.ObjectMarshallingStrategyStoreImpl;
import org.drools.core.reteoo.CoreComponentFactory;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.builder.NodeFactory;
import org.drools.core.rule.EntryPointId;
import org.drools.kiesession.rulebase.KnowledgeBaseFactory;
import org.drools.kiesession.session.StatefulKnowledgeSessionImpl;
import org.drools.mvel.compiler.Person;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.marshalling.MarshallerFactory;

import static org.junit.Assert.assertTrue;

public class FactHandleMarshallingTest {

    private InternalKnowledgeBase createKnowledgeBase() {
        KieBaseConfiguration config = RuleBaseFactory.newKnowledgeBaseConfiguration();
        config.setOption( EventProcessingOption.STREAM );
        return KnowledgeBaseFactory.newKnowledgeBase( config );
    }
    
    private InternalFactHandle createEventFactHandle(StatefulKnowledgeSessionImpl wm, InternalKnowledgeBase kBase) {
        // EntryPointNode
        Rete rete = kBase.getRete();

        NodeFactory nFacotry = CoreComponentFactory.get().getNodeFactoryService();

        RuleBasePartitionId partionId = RuleBasePartitionId.MAIN_PARTITION;
        EntryPointNode entryPointNode = nFacotry.buildEntryPointNode(1, partionId, false, rete , EntryPointId.DEFAULT);
        WorkingMemoryEntryPoint wmEntryPoint = new NamedEntryPoint( EntryPointId.DEFAULT, entryPointNode, wm);

        EventFactHandle factHandle = new EventFactHandle(1, new Person(),0, (new Date()).getTime(), 0, wmEntryPoint);
        
        return factHandle;
    }
       
    private StatefulKnowledgeSessionImpl createWorkingMemory(InternalKnowledgeBase kBase) {
        // WorkingMemoryEntryPoint
        KieSessionConfiguration ksconf = RuleBaseFactory.newKnowledgeSessionConfiguration();
        ksconf.setOption( ClockTypeOption.PSEUDO );
        SessionConfiguration sessionConf = ((SessionConfiguration) ksconf);
        StatefulKnowledgeSessionImpl wm = new StatefulKnowledgeSessionImpl(1L, kBase, true, sessionConf, EnvironmentFactory.newEnvironment());
        
        return wm;
    }
    
    @Test
    public void backwardsCompatibleEventFactHandleTest() throws IOException, ClassNotFoundException {
        InternalKnowledgeBase kBase = createKnowledgeBase();
        StatefulKnowledgeSessionImpl wm = createWorkingMemory(kBase);
        InternalFactHandle factHandle = createEventFactHandle(wm, kBase);
        
        // marshall/serialize workItem
        byte [] byteArray;
        {
            ObjectMarshallingStrategy[] strats 
                = new ObjectMarshallingStrategy[] { 
                    MarshallerFactory.newSerializeMarshallingStrategy(), 
                    new MarshallerProviderImpl().newIdentityMarshallingStrategy() };
    
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ProtobufMarshallerWriteContext outContext = new ProtobufMarshallerWriteContext( baos, null, null, null,
                    new ObjectMarshallingStrategyStoreImpl(strats), true, true, null);
            OldOutputMarshallerMethods.writeFactHandle_v1(outContext, (ObjectOutputStream) outContext, 
                    outContext.getObjectMarshallingStrategyStore(), 2, factHandle);
            outContext.close();
            byteArray = baos.toByteArray();
        }
        
        // unmarshall/deserialize workItem
        InternalFactHandle newFactHandle;
        {
            // Only put serialization strategy in 
            ObjectMarshallingStrategy[] newStrats 
                = new ObjectMarshallingStrategy[] { 
                    MarshallerFactory.newSerializeMarshallingStrategy()  };
    
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            ProtobufMarshallerReaderContext inContext = new ProtobufMarshallerReaderContext( bais, null, null,
                new ObjectMarshallingStrategyStoreImpl(newStrats), Collections.EMPTY_MAP,
                true, true, null);
            inContext.setWorkingMemory( wm );
            newFactHandle = InputMarshaller.readFactHandle(inContext);
            inContext.close();
        }

        assertTrue( "Serialized FactHandle not the same as the original.", compareInstances(factHandle, newFactHandle) );
    }

    private boolean compareInstances(Object objA, Object objB) { 
        boolean same = true;
        if( objA != null && objB != null ) { 
            if( ! objA.getClass().equals(objB.getClass()) ) { 
                return false;
            }
            String className = objA.getClass().getName();
            if( className.startsWith("java") ) { 
                return objA.equals(objB);
            }
            
            try { 
                Field [] fields = objA.getClass().getDeclaredFields();
                if( fields.length == 0 ) { 
                    same = true;
                }
                else { 
                    for( int i = 0; same && i < fields.length; ++i ) { 
                        fields[i].setAccessible(true);
                        Object subObjA = fields[i].get(objA);
                        Object subObjB = fields[i].get(objB);
                        if( ! compareInstances(subObjA, subObjB) ) { 
                           return false; 
                        }
                    }
                }
            }
            catch( Exception e ) { 
                same = false;
                Assert.fail(e.getClass().getSimpleName() + ":" + e.getMessage() );
            }
        }
        else if( objA != objB ) { 
            return false;
        }
        
        return same;
    }
}

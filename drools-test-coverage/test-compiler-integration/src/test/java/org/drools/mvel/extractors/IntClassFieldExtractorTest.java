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

package org.drools.mvel.extractors;

import org.drools.core.base.ClassFieldAccessorCache;
import org.drools.mvel.accessors.ClassFieldAccessorStore;
import org.drools.core.base.TestBean;
import org.drools.core.spi.InternalReadAccessor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntClassFieldExtractorTest extends BaseClassFieldExtractorsTest {
    private static final int VALUE = 4;

    InternalReadAccessor     reader;
    TestBean                 bean  = new TestBean();

    @Before
    public void setUp() throws Exception {
        ClassFieldAccessorStore store = new ClassFieldAccessorStore();
        store.setClassFieldAccessorCache( new ClassFieldAccessorCache( Thread.currentThread().getContextClassLoader() ) );
        store.setEagerWire( true );
        this.reader = store.getReader( TestBean.class,
                                       "intAttr" );
    }

    @Test
    public void testGetBooleanValue() {
        try {
            this.reader.getBooleanValue( null,
                                         this.bean );
            fail( "Should have throw an exception" );
        } catch ( final Exception e ) {
            // success
        }
    }

    @Test
    public void testGetByteValue() {
        assertEquals( IntClassFieldExtractorTest.VALUE,
                             this.reader.getByteValue( null,
                                                       this.bean ) );
    }

    @Test
    public void testGetCharValue() {
        try {
            this.reader.getCharValue( null,
                                      this.bean );
            fail( "Should have throw an exception" );
        } catch ( final Exception e ) {
            // success
        }
    }

    @Test
    public void testGetDoubleValue() {
        assertEquals( IntClassFieldExtractorTest.VALUE,
                             this.reader.getDoubleValue( null,
                                                         this.bean ),
                             0.01 );
    }

    @Test
    public void testGetFloatValue() {
        assertEquals( IntClassFieldExtractorTest.VALUE,
                             this.reader.getFloatValue( null,
                                                        this.bean ),
                             0.01 );
    }

    @Test
    public void testGetIntValue() {
        assertEquals( IntClassFieldExtractorTest.VALUE,
                             this.reader.getIntValue( null,
                                                      this.bean ) );
    }

    @Test
    public void testGetLongValue() {
        assertEquals( IntClassFieldExtractorTest.VALUE,
                             this.reader.getLongValue( null,
                                                       this.bean ) );
    }

    @Test
    public void testGetShortValue() {
        assertEquals( IntClassFieldExtractorTest.VALUE,
                             this.reader.getShortValue( null,
                                                        this.bean ) );
    }

    @Test
    public void testGetValue() {
        assertEquals( IntClassFieldExtractorTest.VALUE,
                             ((Number) this.reader.getValue( null,
                                                             this.bean )).intValue() );
    }

    @Test
    public void testIsNullValue() {
        assertFalse( this.reader.isNullValue( null,
                                                     this.bean ) );
    }
}

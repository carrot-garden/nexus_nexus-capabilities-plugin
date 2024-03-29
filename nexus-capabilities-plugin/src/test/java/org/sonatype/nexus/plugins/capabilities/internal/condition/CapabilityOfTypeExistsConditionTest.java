/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.condition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.EventBusTestSupport;
import org.sonatype.nexus.plugins.capabilities.internal.DefaultCapabilityReference;

/**
 * {@link CapabilityOfTypeExistsCondition} UTs.
 *
 * @since 2.0
 */
public class CapabilityOfTypeExistsConditionTest
    extends EventBusTestSupport
{

    @Mock
    private CapabilityReference ref1;

    @Mock
    private CapabilityReference ref2;

    @Mock
    private CapabilityRegistry capabilityRegistry;

    private CapabilityOfTypeExistsCondition underTest;

    @Before
    public final void setUpCapabilityOfTypeExistsCondition()
        throws Exception
    {
        final CapabilityType capabilityType = capabilityType( this.getClass().getName() );

        when( ref1.context() ).thenReturn( mock( CapabilityContext.class ) );
        when( ref1.context().type() ).thenReturn( capabilityType );

        when( ref2.context() ).thenReturn( mock( CapabilityContext.class ) );
        when( ref2.context().type() ).thenReturn( capabilityType );

        final CapabilityDescriptorRegistry descriptorRegistry = mock( CapabilityDescriptorRegistry.class );
        final CapabilityDescriptor descriptor = mock( CapabilityDescriptor.class );

        when( descriptor.name() ).thenReturn( this.getClass().getSimpleName() );
        when( descriptorRegistry.get( capabilityType ) ).thenReturn( descriptor );

        underTest = new CapabilityOfTypeExistsCondition(
            eventBus, descriptorRegistry, capabilityRegistry, capabilityType
        );
        underTest.bind();

        verify( eventBus ).register( underTest );
    }

    /**
     * Initially, condition should be unsatisfied.
     */
    @Test
    public void initiallyNotSatisfied()
    {
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * Condition should become satisfied if an active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeExists01()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( true );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should become satisfied if a non active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeExists02()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        when( ref1.context().isActive() ).thenReturn( false );
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should not be re-satisfied if a new active capability of specified type is added.
     */
    @Test
    public void capabilityOfTypeExists03()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref1, ref2 ) ).when( capabilityRegistry ).getAll();
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref2 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should remain satisfied if another capability of the specified type is removed.
     */
    @Test
    public void capabilityOfTypeExists04()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref1, ref2 ) ).when( capabilityRegistry ).getAll();
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref2 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Arrays.asList( ref2 ) ).when( capabilityRegistry ).getAll();
        underTest.handle( new CapabilityEvent.AfterRemove( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * Condition should become unsatisfied when all capabilities have been removed.
     */
    @Test
    public void capabilityOfTypeExists05()
    {
        doReturn( Arrays.asList( ref1 ) ).when( capabilityRegistry ).getAll();
        underTest.handle( new CapabilityEvent.Created( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( true ) );

        doReturn( Collections.emptyList() ).when( capabilityRegistry ).getAll();
        underTest.handle( new CapabilityEvent.AfterRemove( capabilityRegistry, ref1 ) );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
    }

    /**
     * Event bus handler is removed when releasing.
     */
    @Test
    public void releaseRemovesItselfAsHandler()
    {
        underTest.release();

        verify( eventBus ).unregister( underTest );
    }

}

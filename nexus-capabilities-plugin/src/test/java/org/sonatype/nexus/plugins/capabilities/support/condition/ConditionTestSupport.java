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
package org.sonatype.nexus.plugins.capabilities.support.condition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.nexus.plugins.capabilities.EventBusTestSupport;

/**
 * {@link ConditionSupport} UTs.
 *
 * @since 2.0
 */
public class ConditionTestSupport
    extends EventBusTestSupport
{

    private TestCondition underTest;

    @Before
    public final void setUpTestCondition()
        throws Exception
    {
        underTest = new TestCondition( eventBus );
        underTest.bind();
    }

    /**
     * On creation, condition is not satisfied.
     */
    @Test
    public void notSatisfiedInitially()
    {
        assertThat( underTest.isSatisfied(), is( false ) );
    }

    /**
     * When satisfied set to true, condition is satisfied and notification is sent.
     */
    @Test
    public void satisfied()
    {
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * When satisfied set to true after condition is already satisfied, condition is satisfied and notification is sent
     * only once.
     */
    @Test
    public void satisfiedOnAlreadySatisfied()
    {
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ) );
    }

    /**
     * When satisfied set to false after condition is already unsatisfied, condition is unsatisfied and notification
     * is sent only once.
     */
    @Test
    public void unsatisfiedOnAlreadyUnsatisfied()
    {
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ) );
    }

    /**
     * When satisfied set to true after condition is unsatisfied, condition is satisfied and notifications are sent.
     */
    @Test
    public void satisfiedOnUnsatisfied()
    {
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );
        underTest.setSatisfied( true );
        assertThat( underTest.isSatisfied(), is( true ) );

        verifyEventBusEvents( satisfied( underTest ), unsatisfied( underTest ), satisfied( underTest ) );
    }

    /**
     * Calling setSatisfied() after release does not send events but sets status.
     */
    @Test
    public void setSatisfiedAfterRelease()
    {
        underTest.release();
        underTest.setSatisfied( false );
        assertThat( underTest.isSatisfied(), is( false ) );

        verifyNoEventBusEvents();
    }

    /**
     * Activation context getter returns the passed in value.
     */
    @Test
    public void getActivationContext()
    {
        assertThat( underTest.getEventBus(), is( equalTo( eventBus ) ) );
    }

    private static class TestCondition
        extends ConditionSupport
    {

        public TestCondition( final EventBus eventBus )
        {
            super( eventBus );
        }

        @Override
        protected void doBind()
        {
            // do nothing
        }

        @Override
        protected void doRelease()
        {
            // do nothing
        }
    }

}

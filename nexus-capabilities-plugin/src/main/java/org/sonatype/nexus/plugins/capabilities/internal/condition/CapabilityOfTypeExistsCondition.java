/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.condition;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.support.condition.ConditionSupport;
import com.google.common.eventbus.Subscribe;

/**
 * A condition that is satisfied when a capability of a specified type exists.
 *
 * @since 2.0
 */
public class CapabilityOfTypeExistsCondition
    extends ConditionSupport
{

    private final CapabilityRegistry capabilityRegistry;

    private final ReentrantReadWriteLock bindLock;

    final CapabilityType type;

    final String typeName;

    public CapabilityOfTypeExistsCondition( final NexusEventBus eventBus,
                                            final CapabilityDescriptorRegistry descriptorRegistry,
                                            final CapabilityRegistry capabilityRegistry,
                                            final CapabilityType type )
    {
        super( eventBus );
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.type = checkNotNull( type );
        final CapabilityDescriptor descriptor = checkNotNull( descriptorRegistry ).get( type );
        typeName = descriptor == null ? type.toString() : descriptor.name();
        bindLock = new ReentrantReadWriteLock();
    }

    @Override
    protected void doBind()
    {
        try
        {
            bindLock.writeLock().lock();
            for ( final CapabilityReference reference : capabilityRegistry.getAll() )
            {
                handle( new CapabilityEvent.Created( capabilityRegistry, reference ) );
            }
        }
        finally
        {
            bindLock.writeLock().unlock();
        }
        getEventBus().register( this );
    }

    @Override
    public void doRelease()
    {
        getEventBus().unregister( this );
    }

    @Subscribe
    public void handle( final CapabilityEvent.Created event )
    {
        if ( !isSatisfied() && type.equals( event.getReference().context().type() ) )
        {
            checkAllCapabilities();
        }
    }

    @Subscribe
    public void handle( final CapabilityEvent.AfterRemove event )
    {
        if ( isSatisfied() && type.equals( event.getReference().context().type() ) )
        {
            checkAllCapabilities();
        }
    }

    void checkAllCapabilities()
    {
        for ( final CapabilityReference ref : capabilityRegistry.getAll() )
        {
            if ( isSatisfiedBy( ref ) )
            {
                setSatisfied( true );
                return;
            }
        }
        setSatisfied( false );
    }

    boolean isSatisfiedBy( final CapabilityReference reference )
    {
        return type.equals( reference.context().type() );
    }

    @Override
    protected void setSatisfied( final boolean satisfied )
    {
        try
        {
            bindLock.readLock().lock();
            super.setSatisfied( satisfied );
        }
        finally
        {
            bindLock.readLock().unlock();
        }
    }

    @Override
    public String toString()
    {
        return type + " exists";
    }

    @Override
    public String explainSatisfied()
    {
        return typeName + " exists";
    }

    @Override
    public String explainUnsatisfied()
    {
        return typeName + " does not exist";
    }

}
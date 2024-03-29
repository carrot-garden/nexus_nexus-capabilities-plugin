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
package org.sonatype.nexus.plugins.capabilities.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.inject.name.Names.named;
import static org.sonatype.appcontext.internal.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.inject.BeanEntry;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactoryRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import com.google.common.collect.Maps;
import com.google.inject.ConfigurationException;
import com.google.inject.Key;

/**
 * Default {@link CapabilityFactoryRegistry} implementation.
 *
 * @since 2.0
 */
@Named
@Singleton
class DefaultCapabilityFactoryRegistry
    extends AbstractLoggingComponent
    implements CapabilityFactoryRegistry
{

    private final Map<String, CapabilityFactory> factories;

    private final Map<String, CapabilityFactory> dynamicFactories;

    private final CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    private final BeanLocator beanLocator;

    @Inject
    DefaultCapabilityFactoryRegistry( final Map<String, CapabilityFactory> factories,
                                      final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                                      final BeanLocator beanLocator )
    {
        this.beanLocator = checkNotNull( beanLocator );
        this.capabilityDescriptorRegistry = checkNotNull( capabilityDescriptorRegistry );
        this.factories = checkNotNull( factories );
        this.dynamicFactories = Maps.newConcurrentMap();
    }

    @Override
    public CapabilityFactoryRegistry register( final CapabilityType type, final CapabilityFactory factory )
    {
        checkNotNull( factory );
        checkArgument( !factories.containsKey( type ), "Factory already registered for %s", type );
        checkArgument( !dynamicFactories.containsKey( type ), "Factory already registered for %s", type );

        dynamicFactories.put( type.toString(), factory );
        getLogger().debug( "Added {} -> {}", type, factory );

        return this;
    }

    @Override
    public CapabilityFactoryRegistry unregister( final CapabilityType type )
    {
        if ( type != null )
        {
            final CapabilityFactory factory = dynamicFactories.remove( type );
            getLogger().debug( "Removed {} -> {}", type, factory );
        }

        return this;
    }

    @Override
    public CapabilityFactory get( final CapabilityType type )
    {
        CapabilityFactory factory = factories.get( checkNotNull( type ).toString() );
        if ( factory == null )
        {
            factory = dynamicFactories.get( checkNotNull( type ).toString() );
        }
        if ( factory == null )
        {
            final CapabilityDescriptor descriptor = capabilityDescriptorRegistry.get( type );
            if ( descriptor != null && descriptor instanceof CapabilityFactory )
            {
                factory = (CapabilityFactory) descriptor;
            }
            if ( factory == null )
            {
                try
                {
                    final Iterable<BeanEntry<Annotation, Capability>> entries = beanLocator.locate(
                        Key.get( Capability.class, named( type.toString() ) )
                    );
                    if ( entries != null && entries.iterator().hasNext() )
                    {
                        factory = new CapabilityFactory()
                        {
                            @Override
                            public Capability create()
                            {
                                return entries.iterator().next().getValue();
                            }
                        };
                    }
                }
                catch ( ConfigurationException ignore )
                {
                    // ignore
                }
            }
        }
        return factory;
    }

}

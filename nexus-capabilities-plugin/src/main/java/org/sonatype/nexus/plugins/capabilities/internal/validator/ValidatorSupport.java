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
package org.sonatype.nexus.plugins.capabilities.internal.validator;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import javax.inject.Provider;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import com.google.common.collect.Maps;

/**
 * Validators support.
 *
 * @since 2.0
 */
class ValidatorSupport
{

    private final Provider<CapabilityDescriptorRegistry> capabilityDescriptorRegistryProvider;

    private final CapabilityType type;

    ValidatorSupport( final Provider<CapabilityDescriptorRegistry> capabilityDescriptorRegistryProvider,
                      final CapabilityType type )
    {
        this.capabilityDescriptorRegistryProvider = checkNotNull( capabilityDescriptorRegistryProvider );
        this.type = checkNotNull( type );
    }

    CapabilityDescriptorRegistry capabilityDescriptorRegistry()
    {
        return capabilityDescriptorRegistryProvider.get();
    }

    CapabilityType capabilityType()
    {
        return type;
    }

    CapabilityDescriptor capabilityDescriptor()
    {
        return capabilityDescriptorRegistry().get( type );
    }

    String typeName()
    {
        final CapabilityDescriptor descriptor = capabilityDescriptor();
        if ( descriptor != null )
        {
            return descriptor.name();
        }
        return capabilityType().toString();
    }

    String propertyName( final String propertyKey )
    {
        String name = extractNames().get( propertyKey );
        if ( name == null )
        {
            name = propertyKey;
        }
        return name;
    }

    private Map<String, String> extractNames()
    {
        final Map<String, String> keyToName = Maps.newHashMap();
        final List<FormField> formFields = capabilityDescriptor().formFields();
        if ( formFields != null )
        {
            for ( final FormField formField : formFields )
            {
                keyToName.put( formField.getId(), formField.getLabel() );
            }
        }
        return keyToName;
    }

}

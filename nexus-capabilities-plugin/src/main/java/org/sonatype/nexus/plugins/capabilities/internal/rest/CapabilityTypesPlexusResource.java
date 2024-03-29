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
package org.sonatype.nexus.plugins.capabilities.internal.rest;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityFormFieldResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityTypeResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityTypeResourceResponse;
import org.sonatype.nexus.rest.formfield.AbstractFormFieldResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Singleton
@Path( CapabilityTypesPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class CapabilityTypesPlexusResource
    extends AbstractFormFieldResource
    implements PlexusResource
{

    public static final String RESOURCE_URI = "/capabilityTypes";

    public static final String $INCLUDE_NOT_EXPOSED = "$includeNotExposed";

    private final CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    @Inject
    public CapabilityTypesPlexusResource( final CapabilityDescriptorRegistry capabilityDescriptorRegistry )
    {
        this.capabilityDescriptorRegistry = capabilityDescriptorRegistry;
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:capabilityTypes]" );
    }

    /**
     * Retrieve a list of capability types available.
     */
    @Override
    @GET
    public Object get( final Context context, final Request request, final Response response, final Variant variant )
        throws ResourceException
    {
        final boolean includeNotExposed = Boolean.parseBoolean(
            request.getResourceRef().getQueryAsForm().getFirstValue( $INCLUDE_NOT_EXPOSED, true, "false" )
        );
        final CapabilityTypeResourceResponse envelope = new CapabilityTypeResourceResponse();

        final CapabilityDescriptor[] descriptors = capabilityDescriptorRegistry.getAll();

        if ( descriptors != null )
        {
            for ( final CapabilityDescriptor descriptor : descriptors )
            {
                if ( ( includeNotExposed || descriptor.isExposed() ) )
                {
                    final CapabilityTypeResource capabilityTypeResource = new CapabilityTypeResource();
                    capabilityTypeResource.setId( descriptor.type().toString() );
                    capabilityTypeResource.setName( descriptor.name() );
                    capabilityTypeResource.setAbout( descriptor.about() );

                    envelope.addData( capabilityTypeResource );

                    final List<FormField> formFields = descriptor.formFields();

                    capabilityTypeResource.setFormFields(
                        (List<CapabilityFormFieldResource>) formFieldToDTO(
                            formFields, CapabilityFormFieldResource.class
                        )
                    );
                }

            }
        }

        return envelope;
    }

}

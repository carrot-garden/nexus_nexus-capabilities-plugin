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
package org.sonatype.nexus.plugins.capabilities.internal.storage;

import static org.sonatype.nexus.plugins.capabilities.CapabilityIdentity.capabilityIdentity;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.Configuration;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.io.xpp3.NexusCapabilitiesConfigurationXpp3Reader;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.io.xpp3.NexusCapabilitiesConfigurationXpp3Writer;
import com.google.common.collect.Lists;

/**
 * Handles persistence of capabilities configuration.
 */
@Singleton
@Named
public class DefaultCapabilityStorage
    extends AbstractLoggingComponent
    implements CapabilityStorage
{

    private final File configurationFile;

    private final ReentrantLock lock = new ReentrantLock();

    private Configuration configuration;

    @Inject
    public DefaultCapabilityStorage( final ApplicationConfiguration applicationConfiguration )
    {
        configurationFile = new File( applicationConfiguration.getWorkingDirectory(), "conf/capabilities.xml" );
    }

    @Override
    public void add( final CapabilityStorageItem item )
        throws IOException
    {
        try
        {
            lock.lock();

            load().addCapability( asCCapability( item ) );
            save();
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public boolean update( final CapabilityStorageItem item )
        throws IOException
    {
        try
        {
            lock.lock();

            final CCapability capability = asCCapability( item );

            final CCapability stored = getInternal( capability.getId() );

            if ( stored == null )
            {
                return false;
            }
            load().removeCapability( stored );
            load().addCapability( capability );
            save();
            return true;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public boolean remove( final CapabilityIdentity id )
        throws IOException
    {
        try
        {
            lock.lock();

            final CCapability stored = getInternal( id.toString() );
            if ( stored == null )
            {
                return false;
            }
            load().removeCapability( stored );
            save();
            return true;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public Collection<CapabilityStorageItem> getAll()
        throws IOException
    {
        Collection<CapabilityStorageItem> items = Lists.newArrayList();
        final List<CCapability> capabilities = load().getCapabilities();
        if ( capabilities != null )
        {
            for ( final CCapability capability : capabilities )
            {
                items.add( asCapabilityStorageItem( capability ) );
            }
        }
        return items;
    }

    private Configuration load()
        throws IOException
    {
        if ( configuration != null )
        {
            return configuration;
        }

        lock.lock();

        Reader fr = null;
        FileInputStream is = null;

        try
        {
            final Reader r = new FileReader( configurationFile );

            Xpp3DomBuilder.build( r );

            is = new FileInputStream( configurationFile );

            final NexusCapabilitiesConfigurationXpp3Reader reader = new NexusCapabilitiesConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );
        }
        catch ( final FileNotFoundException e )
        {
            // This is ok, may not exist first time around
            configuration = new Configuration();

            configuration.setVersion( Configuration.MODEL_VERSION );

            save();
        }
        catch ( final IOException e )
        {
            getLogger().error( "IOException while retrieving configuration file", e );
        }
        catch ( final XmlPullParserException e )
        {
            getLogger().error( "Invalid XML Configuration", e );
        }
        finally
        {
            IOUtil.close( fr );
            IOUtil.close( is );

            lock.unlock();
        }

        return configuration;
    }

    private void save()
        throws IOException
    {
        lock.lock();

        configurationFile.getParentFile().mkdirs();

        Writer fw = null;

        try
        {
            fw = new OutputStreamWriter( new FileOutputStream( configurationFile ) );

            final NexusCapabilitiesConfigurationXpp3Writer writer = new NexusCapabilitiesConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        finally
        {
            if ( fw != null )
            {
                try
                {
                    fw.flush();

                    fw.close();
                }
                catch ( final IOException e )
                {
                    // just closing if open
                }
            }

            lock.unlock();
        }
    }

    private CCapability getInternal( final String capabilityId )
        throws IOException
    {
        if ( StringUtils.isEmpty( capabilityId ) )
        {
            return null;
        }

        for ( final CCapability capability : load().getCapabilities() )
        {
            if ( capabilityId.equals( capability.getId() ) )
            {
                return capability;
            }
        }

        return null;
    }

    private CapabilityStorageItem asCapabilityStorageItem( final CCapability capability )
    {
        final Map<String, String> properties = new HashMap<String, String>();
        if ( capability.getProperties() != null )
        {
            for ( final CCapabilityProperty property : capability.getProperties() )
            {
                properties.put( property.getKey(), property.getValue() );
            }
        }

        return new CapabilityStorageItem(
            capability.getVersion(),
            capabilityIdentity( capability.getId() ),
            capabilityType( capability.getTypeId() ),
            capability.isEnabled(),
            capability.getNotes(),
            properties
        );
    }

    private CCapability asCCapability( final CapabilityStorageItem item )
    {
        final CCapability capability = new CCapability();
        capability.setVersion( item.version() );
        capability.setId( item.id().toString() );
        capability.setTypeId( item.type().toString() );
        capability.setEnabled( item.isEnabled() );
        capability.setNotes( item.notes() );
        if ( item.properties() != null )
        {
            for ( Map.Entry<String, String> entry : item.properties().entrySet() )
            {
                final CCapabilityProperty property = new CCapabilityProperty();
                property.setKey( entry.getKey() );
                property.setValue( entry.getValue() );
                capability.addProperty( property );
            }
        }
        return capability;
    }

}

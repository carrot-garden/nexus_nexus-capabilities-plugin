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
package org.sonatype.nexus.plugins.capabilities;

/**
 * Exception thrown when a capability was not found (e.g. updating an inexistent capability, removing an inexistent
 * capability,...).
 *
 * @since 2.2
 */
public class CapabilityNotFoundException
    extends RuntimeException
{

    public CapabilityNotFoundException( final CapabilityIdentity id )
    {
        super( String.format( "Cannot find a capability with specified id of %s", id ) );
    }

}

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

import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import com.google.inject.assistedinject.Assisted;

/**
 * {@link org.sonatype.nexus.plugins.capabilities.Validator} factory.
 *
 * @since 2.0
 */
public interface ValidatorFactory
{

    PrimaryKeyValidator uniquePer( CapabilityType type, String... propertyKeys );

    PrimaryKeyExcludingSelfValidator uniquePerExcluding( CapabilityIdentity excludeId, CapabilityType type,
                                                         String... propertyKeys );

    RepositoryTypeValidator repositoryOfType( CapabilityType type, String propertyKey, Class<?> facet );

    DescriptorConstraintsValidator constraintsOf( CapabilityType type );

    AlwaysValidValidator alwaysValid();

    RequiredFieldValidator required( CapabilityType type, String propertyKey );

    RegexpFieldValidator matches( CapabilityType type,
                                  @Assisted( "key" ) String propertyKey,
                                  @Assisted( "regexp" ) String regexp );
}

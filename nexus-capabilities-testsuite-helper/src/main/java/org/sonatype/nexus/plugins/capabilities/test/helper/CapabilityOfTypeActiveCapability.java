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
package org.sonatype.nexus.plugins.capabilities.test.helper;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.support.condition.Conditions;

@Named( CapabilityOfTypeActiveCapabilityDescriptor.TYPE_ID )
public class CapabilityOfTypeActiveCapability
    extends TestCapability
    implements Capability
{

    private final Conditions conditions;

    @Inject
    public CapabilityOfTypeActiveCapability( final Conditions conditions )
    {
        this.conditions = conditions;
    }

    @Override
    public Condition activationCondition()
    {
        return conditions.capabilities().capabilityOfTypeActive( MessageCapabilityDescriptor.TYPE );
    }

}

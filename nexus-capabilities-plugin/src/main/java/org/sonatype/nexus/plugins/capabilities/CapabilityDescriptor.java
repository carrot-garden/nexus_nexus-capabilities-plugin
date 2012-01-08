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
package org.sonatype.nexus.plugins.capabilities;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.formfields.FormField;

/**
 * Describes a capability (its type).
 */
public interface CapabilityDescriptor
{

    /**
     * Returns the capability type.
     *
     * @return unique identifier of capability type
     */
    CapabilityType type();

    /**
     * Returns a user friendly name of capability (to be presented in UI).
     *
     * @return capability type name.
     */
    String name();

    /**
     * Returns capability form fields (properties).
     *
     * @return capability form fields (properties)
     */
    List<FormField> formFields();

    /**
     * Whether or not capabilities of this type are user facing = user should be able create it via UI (select it from
     * capability type drop down). Usually not exposed capabilities are building blocks for some other capabilities and
     * should not be directly be created.
     *
     * @return true if is user facing
     */
    boolean isExposed();

    /**
     * Whether or not capabilities of this type should be hidden by default. Usually hidden capabilities are managed
     * (CRUD) by some other means like for example a custom made UI.
     * <p/>
     * User will be able to see them only when turning on hidden capabilities (in UI).
     *
     * @return true if is hidden
     */
    boolean isHidden();

    /**
     * Returns a detailed description of capability type (to be presented in UI).
     *
     * @return capability type description
     */
    String about();

    /**
     * Returns the version of descriptor. The version should change when the descriptor fields change, case when the
     * {@link #convert} method will be called upon loading of persisted capability configuration.
     *
     * @return version
     */
    int version();

    /**
     * Converts capability properties from one version to another. The method is called upon loading of capability, in
     * case that the persisted version differ from {@link #version}.
     *
     * @param properties  to be converted
     * @param fromVersion version of capability properties to be converted
     * @return converted
     */
    Map<String, String> convert( Map<String, String> properties, int fromVersion );

}
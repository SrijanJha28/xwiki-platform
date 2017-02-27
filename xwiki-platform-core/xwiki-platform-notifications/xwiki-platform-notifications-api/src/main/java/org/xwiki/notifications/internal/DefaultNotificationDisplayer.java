/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.notifications.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.eventstream.Event;
import org.xwiki.notifications.NotificationDisplayer;
import org.xwiki.notifications.NotificationException;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.template.TemplateManager;
import org.xwiki.velocity.VelocityManager;

/**
 * @version $Id$
 */
@Component
@Singleton
public class DefaultNotificationDisplayer implements NotificationDisplayer
{
    @Inject
    private TemplateManager templateManager;

    @Inject
    private VelocityManager velocityManager;

    @Override
    public XDOM renderNotification(Event eventNotification) throws NotificationException
    {
        try {
            velocityManager.getCurrentVelocityContext().put("event", eventNotification);
            return templateManager.execute("notification/default.vm");
        } catch (Exception e) {
            throw new NotificationException("Failed to render the notification.", e);
        } finally {
            velocityManager.getCurrentVelocityContext().remove("event");
        }
    }
}
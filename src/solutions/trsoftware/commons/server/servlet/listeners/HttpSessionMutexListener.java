/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.server.servlet.listeners;

// NOTE: this code borrowed from Spring: https://github.com/spring-projects/spring-framework/blob/9be327985b61588d5f8c7050a5558ef36a33b321/spring-web/src/main/java/org/springframework/web/util/HttpSessionMutexListener.java#L23-L66

import solutions.trsoftware.commons.server.servlet.ServletUtils;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.Serializable;

/**
 * Servlet session listener that automatically exposes the session mutex
 * when an {@link HttpSession} gets created. To be registered as a listener in
 * {@code web.xml}.
 *
 * <p>The session mutex is guaranteed to be the same object during
 * the entire lifetime of the session, available under the key defined
 * by the {@link ServletUtils#SESSION_MUTEX_ATTRIBUTE} constant. It serves as a
 * safe reference to synchronize on for locking on the current session.
 *
 * <p>In many cases, the {@link HttpSession} reference itself is a safe mutex
 * as well, since it will always be the same object reference for the
 * same active logical session. However, this is not guaranteed across
 * different servlet containers; the only 100% safe way is a session mutex.
 *
 * @author Juergen Hoeller (Spring Project)
 *
 * @see ServletUtils#getSessionMutex(HttpSession)
 */
public class HttpSessionMutexListener implements HttpSessionListener {


	@Override
	public void sessionCreated(HttpSessionEvent event) {
		event.getSession().setAttribute(ServletUtils.SESSION_MUTEX_ATTRIBUTE, new Mutex());
	}


	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		event.getSession().removeAttribute(ServletUtils.SESSION_MUTEX_ATTRIBUTE);
	}


	/**
	 * The mutex to be registered.
	 * Doesn't need to be anything but a plain Object to synchronize on.
	 * Should be serializable to allow for HttpSession persistence.
	 */
	@SuppressWarnings("serial")
	private static class Mutex implements Serializable {
	}

}
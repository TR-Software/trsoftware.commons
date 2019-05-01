/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.util.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating a proxy object that intercepts method invocations on a target object and persists its new state.
 *
 * @param <T> type of the objects being persisted
 *
 * @see Proxy
 * @author Alex, 4/23/2019
 */
public class PersistentObjectProxy<T> implements InvocationHandler {

  /**
   * This flyweight ensures a 1-1 mapping between the proxy object and the data source ({@link PersistentObjectDAO}),
   * which prevents a race condition of two different instances being persisted to the same file, for example.
   * The values in this map hold the {@link #target} object mapped to the data source (e.g. file) specified by the
   * corresponding key in this map.
   */
  private static Map<PersistentObjectDAO, PersistentObjectProxy> flyweight = new ConcurrentHashMap<>();

  /**
   * The object behind this proxy, which will be serialized to the {@link #dao} after every setter method invocation.
   * In other words, this is the object to be read/written to/from the data source.
   */
  private T target;

  private PersistentObjectDAO<T> dao;

  private T proxy;

  /**
   * Creates the {@link #target} object based on the contents of the given file or uses defaultTarget if the file doesn't exist.
   * This constructor is private to ensure that all instances are registered with the {@link #flyweight}.  Use one
   * of the static factory methods instead.
   * @param dao loads and stores the {@link #target} object to the underlying data store.
   * @param defaultTarget A default instance to use for {@link #target} if no persistent state exists for the object yet.
   */
  protected PersistentObjectProxy(PersistentObjectDAO<T> dao, T defaultTarget) {
    this.dao = dao;

    // if the persistent state already exists, use it instead of the passed defaultTarget param
    try {
      target = dao.load();
    }
    catch (java.io.IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    if (target == null) {
      assert defaultTarget != null;
      target = defaultTarget;
    }
  }

  /**
   * Creates the {@link #target} object based on the contents of the given file or uses defaultTarget if the file doesn't exist.
   * This constructor is private to ensure that all instances are registered with the {@link #flyweight}.  Use one
   * of the static factory methods instead.
   * @param dao loads and stores the {@link #target} object to the underlying data store.
   * @param defaultTarget A default instance to use for {@link #target} if no persistent state exists for the object yet.
   */
  protected PersistentObjectProxy(PersistentObjectDAO<T> dao, T defaultTarget, Class<T> interFace) {
    this(dao, defaultTarget);
    proxy = (T)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{interFace}, this);
  }

  /**
   * Factory method that creates the proxy defined by this instance.
   *
   * @return A proxy that will intercept invocations of methods defined in the given interfaces on the encapsulated
   * {@link #target} object.  The result can safely be cast to any of the given interfaces in order to be used
   * in lieu of the {@link #target} object in order for the interception to work.  The interception is implemented
   * by the {@link #invoke(Object, Method, Object[])} method.
   */
  private Proxy newProxyInstance(Class<?>... interfaces) {
    return (Proxy)Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, this);
  }


  public T getProxy() {
    return proxy;
  }

  /**
   * Implements the proxy functionality, i.e. the interception of methods invoked on the {@link #target} object whose
   * name starts with "set" in order to write the {@link #target} object to disk after each such invocation.
   */
  @Override
  public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object ret = method.invoke(target, args);
    if (method.getName().startsWith("set"))  // TODO: use solutions.trsoftware.commons.server.util.reflect.BeanUtils to determine if method is a setter
      synchronized (this) {
        dao.persist(target);
      }
    return ret;
  }

  public synchronized T getTarget() {
    return target;
  }

  public synchronized void setTarget(T target) {
    this.target = target;
  }

  /**
   * Creates the proxy defined by the given parameters, using the given default instance for {@link #target}
   * (object behind the proxy) iff the file doesn't already exist.
   *
   * @param targetDAO where the {@link #target} object is persisted
   * @param defaultTarget A default instance to use for {@link #target} if the file doesn't exist. Either way, this
   * parameter cannot be null, because it's needed to determine the class of the object to be read/written to/from disk.
   * To determine whether this default was used as the proxy target or whether a new instance was created can
   * {@link #getProxyTarget(PersistentObjectDAO)}  (e.g. <code>getProxyTarget(file) == defaultTarget</code>)
   * @param interfaces the returned proxy can be cast to any of the given interfaces (which must be implemented
   * by the {@link #target} object).
   *
   * @return A proxy that will intercept invocations of methods defined in the given interfaces on the encapsulated
   * {@link #target} object.  The result can safely be cast to any of the given interfaces in order to be used
   * in lieu of the {@link #target} object in order for the interception to work.  The interception is implemented
   * by the {@link #invoke(Object, Method, Object[])} method.
   */
  public static synchronized Proxy createProxy(PersistentObjectDAO targetDAO, Object defaultTarget, Class<?>... interfaces) {
    return flyweight.computeIfAbsent(targetDAO,
        dao -> new PersistentObjectProxy(dao, defaultTarget)).newProxyInstance(interfaces);
  }

  /**
   * Creates the proxy defined by the given parameters, using the given default instance for {@link #target}
   * (object behind the proxy) iff the file doesn't already exist.
   *
   * @param targetDAO where the {@link #target} object is persisted.
   * @param defaultTarget A default instance to use for {@link #target} if the file doesn't exist. Either way, this
   * parameter cannot be null, because it's needed to determine the class of the object to be read/written to/from disk.
   * To determine whether this default was used as the proxy target or whether a new instance was created can
   * {@link #getProxyTarget(PersistentObjectDAO)}  (e.g. <code>getProxyTarget(file) == defaultTarget</code>)
   * @param proxiedInterface the returned proxy will be cast to this interface (which must be implemented by the {@link #target} object).
   *
   * @return A proxy that will intercept invocations of methods defined in the given interfaces on the encapsulated
   * {@link #target} object.  The result can safely be cast to any of the given interfaces in order to be used
   * in lieu of the {@link #target} object in order for the interception to work.  The interception is implemented
   * by the {@link #invoke(Object, Method, Object[])} method.
   */
  public static synchronized <I, O extends I> I createProxy(PersistentObjectDAO<O> targetDAO, O defaultTarget, Class<I> proxiedInterface) {
    return (I)createProxy(targetDAO, defaultTarget, new Class[]{proxiedInterface});
  }


  /**
   * @return the object mapped to the given DAO, or {@code null} if no mapping has been created yet.
   * @param objectDAO the 1-1 mapping of the desired object to the underlying data source.
   */
  public static <T> T getProxyTarget(PersistentObjectDAO<T> objectDAO) {
    PersistentObjectProxy proxy = flyweight.get(objectDAO);
    return (T)proxy.target;
  }

}

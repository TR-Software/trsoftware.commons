package solutions.trsoftware.commons.server.util.reflect;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Utils for working with JavaBeans.
 *
 * <p style="font-style: italic;">
 *   NOTE: some of this code borrowed from {@code org.hibernate.cache.ehcache.management.impl.BeanUtils}
 * </p>
 */
public class BeanUtils {

  // TODO: cont here: document and unit test this class

  /**
   * Looks up the getter method for the given property on the bean.
   *
   * @param bean The bean
   * @param propertyName The property to get the getter for
   * @return the named getter method or null if not found
   */
  @Nullable
   public static Method getterMethod(Object bean, String propertyName) {
    return accessorMethod("get", bean, propertyName);
   }

  /**
   * Return the named setter method on the bean or null if not found.
   *
   * @param bean The bean
   * @param propertyName The property to get the setter for
   * @return the named setter method or null if not found
   */
  @Nullable
   public static Method setterMethod(Object bean, String propertyName) {
    return accessorMethod("set", bean, propertyName);
   }

  @Nullable
  public static Method accessorMethod(String methodNamePrefix, Object bean, String propertyName) {
    return accessorMethod(methodNamePrefix, bean.getClass(), propertyName);
  }

  @Nullable
  public static Method accessorMethod(String methodNamePrefix, Class<?> beanClass, String propertyName) {
    final String methodName = beanMethodName(methodNamePrefix, propertyName);
    for (Method m : beanClass.getMethods()) {
      if (methodName.equals(m.getName())) {
        switch (methodNamePrefix) {
          case "get":
          case "is":
            // getters should take no parameters
            if (m.getParameterCount() == 0)
              return m;
            break;
          case "set":
            // setters should take 1 parameter
            if (m.getParameterCount() == 1)
              return m;
            break;
          default:
            throw new IllegalArgumentException("methodNamePrefix should be get|is|set");
        }
      }
    }
    return null;
  }

  public static String beanMethodName(String methodNamePrefix, String propertyName) {
    final StringBuilder sb = new StringBuilder(methodNamePrefix).append(Character.toUpperCase(propertyName.charAt(0)));
    if (propertyName.length() > 1) {
      sb.append(propertyName.substring(1));
    }
    return sb.toString();
  }

}

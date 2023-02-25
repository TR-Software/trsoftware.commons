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

package solutions.trsoftware.commons.server.io;

import solutions.trsoftware.commons.server.SuperTestCase;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static solutions.trsoftware.commons.server.io.ResourceLocator.resolveResourceName;
import static solutions.trsoftware.commons.server.io.ServerIOUtils.readCharactersIntoString;
import static solutions.trsoftware.commons.server.io.ServerIOUtils.readFileIntoStringUTF8;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;

/**
 * @author Alex
 * @since 3/23/2018
 */
public class ResourceLocatorTest extends SuperTestCase {

  static final String RESOURCE_FILENAME = ResourceLocatorTest.class.getSimpleName() + ".txt";
  static final String RESOURCE_FQN
      = ResourceLocatorTest.class.getPackage().getName().replace('.', '/') + "/" + RESOURCE_FILENAME;
  static final String RESOURCE_TEXT = "This file is simply used to test solutions.trsoftware.commons.server.io.ResourceLocator";

  private ResourceLocator relativeResource;
  private ResourceLocator absoluteResource;
  private ResourceLocator absoluteResourceWithClassLoader;
  private ResourceLocator nonExistentResourceAbs;
  private ResourceLocator nonExistentResourceRel;

  public void setUp() throws Exception {
    super.setUp();
    relativeResource = new ResourceLocator(RESOURCE_FILENAME, getClass());
    absoluteResource = new ResourceLocator(RESOURCE_FQN);
    absoluteResourceWithClassLoader = new ResourceLocator(RESOURCE_FQN, getClass().getClassLoader());
    String randString = RandomUtils.randString(20);
    nonExistentResourceAbs = new ResourceLocator(randString);
    nonExistentResourceRel = new ResourceLocator(randString, getClass());
  }

  @Override
  public void tearDown() throws Exception {
    relativeResource = null;
    absoluteResource = null;
    absoluteResourceWithClassLoader = null;
    nonExistentResourceAbs = null;
    nonExistentResourceRel = null;
    super.tearDown();
  }

  public void testGetURL() throws Exception {
    URL expected = getClass().getResource(RESOURCE_FILENAME);
    assertNotNull(expected);
    assertEquals(expected, relativeResource.getURL());
    assertEquals(expected, absoluteResource.getURL());
    assertEquals(expected, absoluteResourceWithClassLoader.getURL());
    // should return null if the resource doesn't exist
    assertNull(nonExistentResourceAbs.getURL());
    assertNull(nonExistentResourceRel.getURL());
  }

  public void testGetURI() throws Exception {
    URI expected = getClass().getResource(RESOURCE_FILENAME).toURI();
    assertNotNull(expected);
    assertEquals(expected, relativeResource.getURI());
    assertEquals(expected, absoluteResource.getURI());
    assertEquals(expected, absoluteResourceWithClassLoader.getURI());
    // should return null if the resource doesn't exist
    assertNull(nonExistentResourceAbs.getURI());
    assertNull(nonExistentResourceRel.getURI());
  }

  public void testGetInputStream() throws Exception {
    String expectedText = RESOURCE_TEXT;
    assertEquals(expectedText, readCharactersIntoString(relativeResource.getInputStream()));
    assertEquals(expectedText, readCharactersIntoString(absoluteResource.getInputStream()));
    assertEquals(expectedText, readCharactersIntoString(absoluteResourceWithClassLoader.getInputStream()));
    // should return null if the resource doesn't exist
    assertNull(nonExistentResourceAbs.getInputStream());
    assertNull(nonExistentResourceRel.getInputStream());
  }

  public void testToFilepath() throws Exception {
    String expectedText = RESOURCE_TEXT;
    assertEquals(expectedText, readFileIntoStringUTF8(new File(relativeResource.toFilepath())));
    assertEquals(expectedText, readFileIntoStringUTF8(new File(absoluteResource.toFilepath())));
    assertEquals(expectedText, readFileIntoStringUTF8(new File(absoluteResourceWithClassLoader.toFilepath())));
    // should return null if the resource doesn't exist
    assertNull(nonExistentResourceAbs.toFilepath());
    assertNull(nonExistentResourceRel.toFilepath());
  }

  public void testToFile() throws Exception {
    assertEquals(new File(relativeResource.toFilepath()), relativeResource.toFile());
    assertEquals(new File(absoluteResource.toFilepath()), absoluteResource.toFile());
    assertEquals(new File(absoluteResourceWithClassLoader.toFilepath()), absoluteResourceWithClassLoader.toFile());
    // should return null if the resource doesn't exist
    assertNull(nonExistentResourceAbs.toFilepath());
    assertNull(nonExistentResourceRel.toFilepath());
  }

  public void testToPath() throws Exception {
    assertEquals(relativeResource.toFile().toPath(), relativeResource.toPath());
    assertEquals(absoluteResource.toFile().toPath(), absoluteResource.toPath());
    assertEquals(absoluteResourceWithClassLoader.toFile().toPath(), absoluteResourceWithClassLoader.toPath());
    // should return null if the resource doesn't exist
    assertNull(nonExistentResourceAbs.toPath());
    assertNull(nonExistentResourceRel.toPath());
  }

  public void testExists() throws Exception {
    assertTrue(relativeResource.exists());
    assertTrue(absoluteResource.exists());
    assertTrue(absoluteResourceWithClassLoader.exists());
    assertFalse(nonExistentResourceAbs.exists());
    assertFalse(nonExistentResourceRel.exists());
  }

  public void testGetCanonicalName() throws Exception {
    String expected = RESOURCE_FQN;
    assertEquals(expected, relativeResource.getCanonicalName());
    assertEquals(expected, absoluteResource.getCanonicalName());
    assertEquals(expected, absoluteResourceWithClassLoader.getCanonicalName());
  }

  public void testResolveResourceName() throws Exception {
    String fullName = RESOURCE_FQN;
    String simpleName = RESOURCE_FILENAME;
    assertEquals(fullName, resolveResourceName(simpleName, ResourceLocatorTest.class));
    // make sure the method can deal with array types
    assertEquals(fullName, resolveResourceName(simpleName, ResourceLocatorTest[].class));
    assertEquals(fullName, resolveResourceName(simpleName, ResourceLocatorTest[][].class));
    assertEquals(fullName, resolveResourceName(simpleName, ResourceLocatorTest[][][].class));
    // make sure it can handle null arguments without throwing an exception
    assertNull(resolveResourceName(null, null));
    assertNull(resolveResourceName(null, ResourceLocatorTest.class));
    // make sure it deals with a leading slash the same way as Class.resolveName (i.e. when the name is absolute, just needs to remove the leading slash)
    assertEquals(fullName, resolveResourceName("/" + fullName, ResourceLocatorTest.class));
    // if the given class is primitive, should return the name as-is
    assertEquals(simpleName, resolveResourceName(simpleName, int.class));
    assertEquals(simpleName, resolveResourceName(simpleName, void.class));
  }

  public void testEqualsAndHashCode() throws Exception {
    assertEqualsAndHashCode(relativeResource, absoluteResource);
    assertEqualsAndHashCode(absoluteResource, absoluteResourceWithClassLoader);

    assertNotEqualsAndHashCode(relativeResource, nonExistentResourceAbs);
    assertNotEqualsAndHashCode(relativeResource, nonExistentResourceRel);
    assertNotEqualsAndHashCode(nonExistentResourceAbs, nonExistentResourceRel);
  }

  public void testToString() throws Exception {
    String expected = RESOURCE_FQN;
    assertEquals(expected, relativeResource.toString());
    assertEquals(expected, absoluteResource.toString());
    assertEquals(expected, absoluteResourceWithClassLoader.toString());
  }

  public void testGetReader() throws Exception {
    assertEquals(RESOURCE_TEXT, readCharactersIntoString(relativeResource.getReader()));
    assertEquals(RESOURCE_TEXT, readCharactersIntoString(absoluteResource.getReader()));
    assertEquals(RESOURCE_TEXT, readCharactersIntoString(absoluteResourceWithClassLoader.getReader()));
    assertNull(nonExistentResourceAbs.getReader());
    assertNull(nonExistentResourceRel.getReader());
  }

  public void testGetReaderUTF8() throws Exception {
    assertEquals(RESOURCE_TEXT, readCharactersIntoString(relativeResource.getReaderUTF8()));
    assertEquals(RESOURCE_TEXT, readCharactersIntoString(absoluteResource.getReaderUTF8()));
    assertEquals(RESOURCE_TEXT, readCharactersIntoString(absoluteResourceWithClassLoader.getReaderUTF8()));

    assertNull(nonExistentResourceAbs.getReaderUTF8());
    assertNull(nonExistentResourceRel.getReaderUTF8());
  }

  public void testGetContentAsString() throws Exception {
    assertEquals(RESOURCE_TEXT, relativeResource.getContentAsString());
    assertEquals(RESOURCE_TEXT, absoluteResource.getContentAsString());
    assertEquals(RESOURCE_TEXT, absoluteResourceWithClassLoader.getContentAsString());
    assertThrows(IOException.class, (Function0_t<? extends Throwable>)nonExistentResourceAbs::getContentAsString);
    assertThrows(IOException.class, (Function0_t<? extends Throwable>)nonExistentResourceRel::getContentAsString);
  }
}
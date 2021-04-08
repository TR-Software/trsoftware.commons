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

package solutions.trsoftware.commons.server.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * An interface for reading data from some data source, such as a regular file or a classpath resource
 * ({@link ClassLoader#getResource(String)}).
 *
 * This is a simplified version of {@link javax.activation.DataSource}, providing a read-only interface.
 *
 *  @author Alex
 * @since 3/19/2018
 */
public interface DataResource {

  /**
   * @return an input stream that can be used to read the data
   */
  InputStream getInputStream() throws IOException;


  /**
   * A resource to be read directly from a {@link File}
   */
  class FileResource implements DataResource {
    private File file;

    public FileResource(File file) {
      this.file = Objects.requireNonNull(file);
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
      return new FileInputStream(file);
    }

    public File getFile() {
      return file;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      FileResource that = (FileResource)o;

      return file.equals(that.file);
    }

    @Override
    public int hashCode() {
      return file.hashCode();
    }

    @Override
    public String toString() {
      return file.toString();
    }
  }

  /**
   * A resource that can be read with {@link ClassLoader#getResourceAsStream} or {@link Class#getResourceAsStream}.
   * @see ResourceLocator
   */
  class JavaResource implements DataResource {
    private ResourceLocator resourceLocator;

    /**
     * @see ResourceLocator
     */
    public JavaResource(ResourceLocator resourceLocator) {
      this.resourceLocator = Objects.requireNonNull(resourceLocator);
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return resourceLocator.getInputStream();
    }

    public ResourceLocator getResourceLocator() {
      return resourceLocator;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      JavaResource that = (JavaResource)o;

      return resourceLocator.equals(that.resourceLocator);
    }

    @Override
    public int hashCode() {
      return resourceLocator.hashCode();
    }

    @Override
    public String toString() {
      return resourceLocator.toString();
    }
  }

  /**
   * A resource to be read directly from a file specified by a {@link Path}
   */
  class NioFileResource implements DataResource {
    private Path path;

    public NioFileResource(Path path) {
      this.path = Objects.requireNonNull(path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return Files.newInputStream(path, StandardOpenOption.READ);
    }

    public Path getPath() {
      return path;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      NioFileResource that = (NioFileResource)o;

      return path.equals(that.path);
    }

    @Override
    public int hashCode() {
      return path.hashCode();
    }

    @Override
    public String toString() {
      return path.toString();
    }
  }

}

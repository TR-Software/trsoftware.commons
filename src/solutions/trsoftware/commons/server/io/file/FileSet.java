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

package solutions.trsoftware.commons.server.io.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

/**
 * Represents a set of files in a directory tree, optionally filtering them by using a supplied {@link FileFilter} or
 * {@link FilenameFilter}.
 *
 * Similar to {@link File#listFiles()}, but descends into subdirectories.
 *
 * @author Alex, 10/28/2017
 */
public class FileSet extends LinkedHashSet<File> {

  /** Root of the directory tree to be searched */
  private File baseDir;

  /** Decides which files to include */
  private FileFilter fileFilter;

  /** Decides which filenames to include */
  private FilenameFilter filenameFilter;

  public FileSet(File baseDir) {
    this.baseDir = baseDir;
    addFiles(baseDir);
  }

  public FileSet(File baseDir, FileFilter fileFilter) {
    this.baseDir = baseDir;
    this.fileFilter = fileFilter;
    addFiles(baseDir);
  }

  public FileSet(File baseDir, FilenameFilter filenameFilter) {
    this.baseDir = baseDir;
    this.filenameFilter = filenameFilter;
    addFiles(baseDir);
  }

  /**
   * Factory method. Equivalent to
   * <pre>
   *   new FileSet(baseDir, File::isFile)
   * </pre>
   */
  public static FileSet allFiles(File baseDir) {
    return new FileSet(baseDir, File::isFile);
  }

  /**
   * Recursively searches the directory tree starting with the given directory, adding all contained files and directories
   * to this set.  If {@link #fileFilter} or {@link #filenameFilter} are specified, they will be used to filter the results.
   * @param dir the root of the directory tree to be searched
   */
  private void addFiles(File dir) {
    // 1) add all the files at this level
    File[] files;
    if (fileFilter != null)
      files = dir.listFiles(fileFilter);
    else
      files = dir.listFiles(filenameFilter);  // File.listFiles accepts a null filter (in which case it matches everything)
    if (files != null)
      addAll(Arrays.asList(files));
    // 2) descend into subdirectories
    File[] subDirs = listSubDirs(dir);
    if (subDirs != null) {
      for (File subDir : subDirs)
        addFiles(subDir);
    }
  }

  private File[] listSubDirs(File dir) {
    return dir.listFiles(File::isDirectory);
  }

  public File getBaseDir() {
    return baseDir;
  }

  public FileFilter getFileFilter() {
    return fileFilter;
  }

  public FilenameFilter getFilenameFilter() {
    return filenameFilter;
  }

  /** Accepts all files */
  public static class FileFilterAcceptsAll implements FileFilter {
    @Override
    public boolean accept(File pathname) {
      return true;
    }
  }

  /** Delegates to a {@link FilenameFilter} */
  public static class FileFilterFromFilenameFilter implements FileFilter, FilenameFilter {
    private FilenameFilter delegate;

    public FileFilterFromFilenameFilter(FilenameFilter delegate) {
      this.delegate = delegate;
    }

    @Override
    public boolean accept(File pathname) {
      return accept(pathname.getParentFile(), pathname.getName());
    }

    @Override
    public boolean accept(File dir, String name) {
      return delegate.accept(dir, name);
    }
  }

  /** Filters filenames based on a given regular expression. */
  public static class FilenameMatcher implements FilenameFilter {
    private Pattern pattern;

    public FilenameMatcher(String regex) {
      this(Pattern.compile(regex));
    }

    public FilenameMatcher(Pattern pattern) {
      this.pattern = pattern;
    }

    @Override
    public boolean accept(File dir, String name) {
      return pattern.matcher(name).matches();
    }
  }

  // TODO: cont here:
  // 1) finish implementing this class and unit test it
  // 3) finish implementing AllGwtTests using ReflectionUtils.getCompilerOutputDir()





}

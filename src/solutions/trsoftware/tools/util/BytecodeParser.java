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

package solutions.trsoftware.tools.util;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import java.io.File;
import java.io.IOException;

/**
 * A simple utility for extracting info from {@code .class} files,
 * using <a href="https://commons.apache.org/proper/commons-bcel/">BCEL</a>.
 *
 * @see ClassParser
 * @see <a href="https://commons.apache.org/proper/commons-bcel/">BCEL (Apache Commons Byte Code Engineering Library)</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4">JVM Spec Chapter 4 ("The class File Format")</a>
 * @author Alex
 * @since 4/18/2018
 */
public class BytecodeParser {

  /**
   * Extracts the fully-qualified name ("binary name") of the class represented by the given bytecode file.
   *
   * @param file a {@code .class} file.
   * @return the fully-qualified name ("binary name") of the class represented by the given bytecode file
   *
   * @see ClassParser#parse()
   * @throws IOException any exception encountered while reading the data
   * @throws ClassFormatException if the file is not a valid {@code .class} file
   */
  public static String extractClassName(File file) throws IOException {
    return parseClassFile(file.getPath()).getClassName();
  }

  /**
   * Uses BCEL to parse the given {@code .class} file.
   *
   * @param filePath fully-qualified filesystem path representing a {@code .class} file.
   * @return an object that represents the contained data, i.e., constants, methods, fields and commands.
   *
   * @see ClassParser#parse()
   * @throws IOException any exception encountered while reading the data
   * @throws ClassFormatException if the file is not a valid {@code .class} file
   */
  public static JavaClass parseClassFile(String filePath) throws IOException {
    return new ClassParser(filePath).parse();
  }

}

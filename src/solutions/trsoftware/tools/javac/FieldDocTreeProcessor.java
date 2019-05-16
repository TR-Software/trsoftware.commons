package solutions.trsoftware.tools.javac;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An annotation processor (that can be used with the Java Compiler API) to obtain the doc comments of fields in a
 * class.  Uses the <a href="https://docs.oracle.com/javase/8/docs/jdk/api/javac/tree/">Java Compiler Tree API</a>
 * to obtain the Javadoc comments.
 *
 * @see <a href="https://www.javacodegeeks.com/2015/09/java-compiler-api.html">Java Compiler API tutorial</a>
 * @see <a href="https://github.com/dnault/therapi-runtime-javadoc/blob/master/therapi-runtime-javadoc-scribe/src/main/java/com/github/therapi/runtimejavadoc/internal/JavadocAnnotationProcessor.java">
 *   "therapi-runtime-javadoc" project on GitHub</a>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class FieldDocTreeProcessor extends AbstractProcessor {
  private DocTrees trees;
  private Map<String, DocCommentTree> javadocByFieldName = new LinkedHashMap<>();
  private Class javaClass;

  /**
   * @param javaClass will collect doc comments for fields of this class.
   */
  public FieldDocTreeProcessor(Class javaClass) {
    this.javaClass = javaClass;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    trees = DocTrees.instance(processingEnv);
  }

  public Map<String, DocCommentTree> getJavadocByFieldName() {
    return javadocByFieldName;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
    if (!environment.processingOver()) {
      for (Element element : environment.getRootElements()) {
//          scanner.scan(trees.getDocCommentTree(trees.getPath(element)), trees);
        scan(element);
      }
    }
    return true;
  }

  private void scan(Element element) {
    if (element.getKind() == ElementKind.FIELD) {
      TreePath treePath = trees.getPath(element);
      DocCommentTree docCommentTree = trees.getDocCommentTree(treePath);
      if (docCommentTree != null) {  // the field might not have a doc comment, in which case this will be null
        Element parent = element.getEnclosingElement();
        if (parent.getKind() == ElementKind.CLASS && parent instanceof TypeElement) {
          TypeElement parentClass = (TypeElement)parent;
          Name qualifiedName = parentClass.getQualifiedName();
          if (javaClass.getCanonicalName().equals(qualifiedName.toString()))
            javadocByFieldName.put(element.getSimpleName().toString(), docCommentTree);
        }
      }
    }
    // recur on subtrees
    for (Element child : element.getEnclosedElements()) {
      scan(child);
    }
  }
}

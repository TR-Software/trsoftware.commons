package solutions.trsoftware.tools.codegen;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Scrapes an <a href="https://developer.mozilla.org/">MDN</a> page to generate Java code from definitions therein.
 *
 * @author Alex
 * @since 11/15/2022
 */
public class MdnCodeGen {


  public static void main(String[] args) throws Exception {
    genWebSocketCloseCodeEnum();
  }

  public static void genWebSocketCloseCodeEnum() throws IOException {
    Document document = Jsoup.connect("https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent/code").get();
    Elements tRows = document.select("table.no-markdown tbody tr");
    TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder("CloseCodes")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addField(
            FieldSpec.builder(int.class, "code", Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("The CloseEvent.code value\n").build())
        .addMethod(MethodSpec.constructorBuilder()
            .addParameter(int.class, "code")
            .addStatement("this.$N = $N", "code", "code")
            .build())
    ;
    for (int i = 0; i < tRows.size(); i++) {
      Element tr = tRows.get(i);
      Elements tdList = tr.children();
      assert tdList.size() == 3;
      String[] cellText = tdList.stream().map(Element::text).toArray(String[]::new);
      if (!cellText[1].isEmpty()) {
        int code = Integer.parseInt(cellText[0]);
        String meaning = cellText[1];
        String description = cellText[2];
        String constName = meaning.toUpperCase().replaceAll("\\s+", "_").replaceAll("[^\\w]+", "");
        System.out.printf("%d | %s (%s)| %s%n", code, meaning, constName, description);
        enumBuilder.addEnumConstant(constName,
            TypeSpec.anonymousClassBuilder("$L", code)
                .addJavadoc(String.format("%s%n", description))
                .build());

      }
    }
    TypeSpec statusCodesEnum = enumBuilder.build();
    System.out.println(statusCodesEnum.toString());
  }
}

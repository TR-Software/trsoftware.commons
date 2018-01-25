package solutions.trsoftware.commons.client.templates;

import com.google.gwt.core.shared.GWT;
import solutions.trsoftware.commons.shared.util.template.Template;
import solutions.trsoftware.commons.shared.util.template.TemplateBundle;

/**
 * @author Alex
 * @since 11/17/2017
 */
public interface CommonTemplates extends TemplateBundle {

  CommonTemplates INSTANCE = GWT.create(CommonTemplates.class);

  Template uncaught_exception_warning();
}

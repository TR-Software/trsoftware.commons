package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.user.cellview.client.TextColumn;
import solutions.trsoftware.commons.shared.util.TimeFormatter;

/**
 * @author Alex, 9/19/2017
 */
public abstract class DurationColumn<T> extends TextColumn<T> {

  private static final TimeFormatter DURATION_FORMATTER = new TimeFormatter(false);

  @Override
  public final String getValue(T object) {
    return DURATION_FORMATTER.format(getDuration(object));
  }

  /**
   * @return A value in milliseconds
   */
  public abstract int getDuration(T object);
}

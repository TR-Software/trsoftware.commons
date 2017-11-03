package solutions.trsoftware.commons.server.memquery;

/**
 * In relational algebra terms, this interface defines the rename (&rho;) operation.
 *
 * @author Alex, 1/6/14
 */
public interface HasName {
  /**
   * @return The "pretty name" to use for this column when printing out the table.
   */
  String getName();
}

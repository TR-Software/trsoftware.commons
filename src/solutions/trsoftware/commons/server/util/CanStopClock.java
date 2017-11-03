package solutions.trsoftware.commons.server.util;

/**
 * A marker interface indicating that code executed by this class
 * can stop the Clock.
 *
 * TODO: Perhaps a better name for this interface should be HasTestingPermissions,
 * because stopping the clock is only one of the things testing and simulation
 * code may want to do.
 *
 * @author Alex
 */
public interface CanStopClock {
}

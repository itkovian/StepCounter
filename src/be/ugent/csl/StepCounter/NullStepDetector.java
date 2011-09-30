/**
 * 
 */
package be.ugent.csl.StepCounter;

/**
 * @author Andy Georges
 *
 */
public class NullStepDetector implements StepDetection {

	/* (non-Javadoc)
	 * @see be.ugent.csl.StepCounter.StepDetection#addData(int, int, int, int)
	 */
	@Override
	public void addData(long timestamp, double xAccell, double yAccell, double zAccell) {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see be.ugent.csl.StepCounter.StepDetection#stepDetected()
	 */
	@Override
	public boolean stepDetected() {
		return false;
	}

}

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
	public void addData(int timestamp, int xAccell, int yAccell, int zAccell) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see be.ugent.csl.StepCounter.StepDetection#stepDetected()
	 */
	@Override
	public boolean stepDetected() {
		// TODO Auto-generated method stub
		return false;
	}

}

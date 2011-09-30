package be.ugent.csl.StepCounter;

/*
 * Interface for a step detector. 
 * 
 * @author Andy Georges
 */
public interface StepDetection {

	public void addData(int timestamp, int xAccell, int yAccell, int zAccell);
	public boolean stepDetected();
	
}

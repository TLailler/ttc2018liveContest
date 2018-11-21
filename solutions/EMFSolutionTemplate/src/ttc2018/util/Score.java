package ttc2018.util;

import java.util.Date;

/**
 * Class used as a Post or a Comment Score
 * 
 * @author Emeric G
 *
 */
public class Score {
	
	/**
	 * amount of points
	 */
	private int points;
	
	/**
	 * date of creation
	 */
	private Date timestamp;
	
	public Score(int p, Date time) {
		this.points = p;
		this.timestamp = time;
	}
	
	public int getPoints() {
		return this.points;
	}
	
	public void setPoints(int points) {
		this.points = points;
	}
	
	public Date getTimestamp() {
		return this.timestamp;
	}
	
	@Override
	public String toString() {
		return "Score : " + points + "pts (" + timestamp.toString() + ")";
	}
}

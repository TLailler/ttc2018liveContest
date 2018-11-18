package ttc2018.util;

import java.util.Date;

public class Score {
	
	private int points;
	private Date timestamp;
	
	public Score(int p, Date time) {
		this.points = p;
		this.timestamp = time;
	}
	
	public int getPoints() {
		return this.points;
	}
	
	public Date getTimestamp() {
		return this.timestamp;
	}
	
	@Override
	public String toString() {
		return "Score : " + points + "pts (" + timestamp.toString() + ")";
	}
}

package ttc2018.util;

import java.util.Comparator;

/**
 * Class used as a Comparator for Score instances
 * 
 * @author Emeric G
 *
 */
public class ScoreComparator implements Comparator<Score> {

	@Override
	/**
	 * Compare two Score instances.
	 * The Score which has the most of points is the bigger.
	 * If points are equal, the most recent Score (comparing the timeStamp attribute) is the bigger
	 */
	public int compare(Score s1, Score s2) {

		if (s1.getPoints() < s2.getPoints()) {
			return -1;
		}
		else if (s1.getPoints() > s2.getPoints()) {
			return 1;
		}
		else {
			return s1.getTimestamp().compareTo(s2.getTimestamp());
		}
	}

}

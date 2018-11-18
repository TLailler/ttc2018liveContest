package ttc2018.util;

import java.util.Comparator;

public class ScoreComparator implements Comparator<Score> {

	@Override
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

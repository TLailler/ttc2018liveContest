package ttc2018.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import SocialNetwork.Comment;
import SocialNetwork.Post;
import SocialNetwork.SocialNetworkRoot;
import SocialNetwork.User;

public class Task1Helper {
	
	private final static TreeMap<Score, String> PODIUM = new TreeMap<Score, String>(new ScoreComparator());
	private final static List<User> USERS_WHO_LIKED = new ArrayList<User>();
	
	private Task1Helper() {}
	
	public static String calculatePodium(SocialNetworkRoot socialNetwork) {
		
		for (Post post: socialNetwork.getPosts()) {
			calculatePostScore(post);
		}
		return computePodiumResult();
	}
	
	public static Integer calculatePostScore(Post post) {
		Integer score = 0;
		for (Comment commentChild : post.getComments()) {
			score += calculateCommentScore(commentChild);
		}
		score += USERS_WHO_LIKED.size();
		USERS_WHO_LIKED.clear();
		addToPodium(score, post.getTimestamp(), post.getId());
		return score;
	}
	
	public static Integer calculateCommentScore(Comment comm) {
		Integer score = 10;
		
		for (Comment commentChild : comm.getComments()) {
			score += calculateCommentScore(commentChild);
			USERS_WHO_LIKED.addAll(commentChild.getLikedBy());
		}
		return score;
	}
	
	public static void addToPodium(Integer score, Date time, String id) {
		
		if (score == null || time == null) 
			return;
		
		PODIUM.put(new Score(score, time),id);
		
		if (PODIUM.size() > 3) {
			// remove first score (which has the lowest)
			PODIUM.remove(PODIUM.firstKey());
		}
	}

	public static String computePodiumResult() {
		String result = "";
		int size = PODIUM.size();
		
		if (size == 0) return result;
		
		Iterator<Score> iter = PODIUM.keySet().iterator();
		result += PODIUM.get(iter.next());
		
		while (iter.hasNext()) {
			result = PODIUM.get(iter.next()) + "|" + result; 
		}
		System.out.println("#################### PODIUM Q1 : " + result);
		return result;
	}
}

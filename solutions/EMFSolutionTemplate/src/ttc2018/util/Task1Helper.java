package ttc2018.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import SocialNetwork.Comment;
import SocialNetwork.Post;
import SocialNetwork.SocialNetworkRoot;
import SocialNetwork.User;

public class Task1Helper {
	
	private final static Map<Integer, String> PODIUM = new HashMap<Integer, String>();
	private final static HashSet<User> USERS_WHO_LIKED = new HashSet<User>();
	private static Integer smallestScore = Integer.MAX_VALUE;
	
	private Task1Helper() {}
	
	public static String calculatePodium(SocialNetworkRoot socialNetwork) {
		Integer postScore = -1;
		
		for (Post post: socialNetwork.getPosts()) {
			postScore = calculatePostScore(post);
			System.out.println("Post id : " + post.getId() + " ; Score : " + postScore);
			postScore = -1;
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
		addToPodium(score, post.getId());
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
	
	public static void addToPodium(Integer score, String id) {
		
		if (PODIUM.size() < 3) {
			PODIUM.put(score,id);
			if (score < smallestScore) {
				smallestScore = score;
			}
			return;
		}
		
		if (score > smallestScore) {
			PODIUM.remove(smallestScore);
			PODIUM.put(score, id);
			calculateSmallestScore();
		}
		else {
			System.out.println("Post Score too small. Not added to the podium.");
		}
	}

	private static void calculateSmallestScore() {
		
		for (Integer score: PODIUM.keySet()) {
			if (score < smallestScore) {
				smallestScore = score;
			}
		}
	}
	
	public static String computePodiumResult() {
		String result = "";
		int size = PODIUM.size();
		
		if (size == 0) return result;
		
		Object[] arrayPodium = PODIUM.values().toArray();
		result += arrayPodium[0];
		
		for (int i = 1; i < size; i++) {
			result += "|" + arrayPodium[i]; 
		}
		return result;
	}
}

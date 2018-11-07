package ttc2018.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import SocialNetwork.Comment;
import SocialNetwork.Post;
import SocialNetwork.SocialNetworkRoot;
import SocialNetwork.User;

public class Task2Helper {
	
	/**
	 * Map containing the 3 most influent comments
	 */
	private final static Map<Integer, String> PODIUM = new HashMap<Integer, String>();
	
	/**
	 * List of groups of users who liked a comment
	 */
	private final static List<HashSet<User>> GROUPS_OF_USERS_WHO_LIKED = new ArrayList<>();
	
	/**
	 * Smallest score of the current podium
	 */
	private static Integer smallestScore = Integer.MAX_VALUE;
	
	private Task2Helper() {}
	
	public static String calculatePodium(SocialNetworkRoot socialNetwork) {
		
		for (Post post: socialNetwork.getPosts()) {
			for (Comment comm: post.getComments()) {
				calculateCommentScore(comm);
			}
		}
		return computePodiumResult();
	}
	
	public static Integer calculateCommentScore(Comment comm) {
		
		for (Comment commentChild : comm.getComments()) {
			calculateCommentScore(commentChild);
		}
		// calculate score for current comment
		// users who LIKED and are FRIENDS in the same GROUP
		GROUPS_OF_USERS_WHO_LIKED.clear();
		Integer score = 0;
		
		for (User user: comm.getLikedBy()) {
			addToGroups(user);
		}
		
		for (HashSet<User> group : GROUPS_OF_USERS_WHO_LIKED) {
			score += (group.size() * group.size()); 
		}
		addToPodium(score, comm.getId());
		return score;
	}
	
	public static void addToGroups(User user) {
		int currentNbOfGroups = GROUPS_OF_USERS_WHO_LIKED.size();
		List<Integer> groupIndexesWhereUserAdded = new ArrayList<Integer>();
		HashSet<User> group;

		for (int i = 0; i < currentNbOfGroups; i++) {
			group = GROUPS_OF_USERS_WHO_LIKED.get(i);
			
			for (User friend : user.getFriends()) {
				
				if (group.contains(friend)) {
					group.add(user);
					groupIndexesWhereUserAdded.add(i);
					break;
				}
			}
		}
		
		int nbGroupsWhereUserAdded = groupIndexesWhereUserAdded.size();
		
		if (nbGroupsWhereUserAdded == 0) {
			group = new HashSet<User>();
			group.add(user);
			GROUPS_OF_USERS_WHO_LIKED.add(group);
		}
		else if (nbGroupsWhereUserAdded > 1) {
			group = GROUPS_OF_USERS_WHO_LIKED.get(groupIndexesWhereUserAdded.get(0));
			int groupIndex;
			
			for (int ind = nbGroupsWhereUserAdded - 1; ind > 0; ind--) {
				groupIndex = groupIndexesWhereUserAdded.get(ind);
				group.addAll(GROUPS_OF_USERS_WHO_LIKED.get(groupIndex));
				GROUPS_OF_USERS_WHO_LIKED.remove(groupIndex);
			}
		}
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
			System.out.println("Comment Score too small. Not added to the podium.");
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

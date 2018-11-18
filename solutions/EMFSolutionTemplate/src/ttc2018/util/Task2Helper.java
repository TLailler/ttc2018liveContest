package ttc2018.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import SocialNetwork.Comment;
import SocialNetwork.Post;
import SocialNetwork.SocialNetworkRoot;
import SocialNetwork.User;

public class Task2Helper {
	
	/**
	 * Map containing the 3 most influent comments
	 */
	private final static TreeMap<Score, String> PODIUM = new TreeMap<Score, String>(new ScoreComparator());
	
	/**
	 * List of groups of users who liked a comment
	 */
	private final static List<HashSet<User>> GROUPS_OF_USERS_WHO_LIKED = new ArrayList<>();
	
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
		addToPodium(score, comm.getTimestamp(), comm.getId());
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
		System.out.println("#################### PODIUM Q2 : " + result);
		return result;
	}
}

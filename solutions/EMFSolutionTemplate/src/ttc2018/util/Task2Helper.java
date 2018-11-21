package ttc2018.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import Changes.AssociationCollectionInsertion;
import Changes.ChangeTransaction;
import Changes.CompositionListInsertion;
import Changes.ModelChange;
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
	
	/**
	 * For Update part (IN PROGRESS)
	 */
	private final static HashSet<Comment> MODIFIED_COMMENTS = new HashSet<Comment>();
	
	private Task2Helper() {}
	
	public static String calculatePodium(SocialNetworkRoot socialNetwork) {
		PODIUM.clear();
		
		for (Post post: socialNetwork.getPosts()) {
			for (Comment comm: post.getComments()) {
				calculateCommentScore(comm);
			}
		}
		return computePodiumResult();
	}
	
	public static String updatePodium(SocialNetworkRoot socialNetwork) {
		
		System.out.println("Nb MODIFIED COMMENTS : " + MODIFIED_COMMENTS.size());
		for (Comment comm: MODIFIED_COMMENTS) {
			System.out.println("MODIFIED COMMENT ID : " + comm.getId());
			updateCommentScore(comm);
		}
		MODIFIED_COMMENTS.clear();
		return computePodiumResult();
	}
	
	public static void findUpdatedComments(ModelChange change) {
		ChangeTransaction currentChangeTransaction = null;
		ModelChange sourceChange = null;
		EObject addedElement = null;
		EObject affectedElement = null;
		EReference feature = null;

		System.out.println();
		System.out.println();
		System.out.println("DEBUT FIND UPDATED COMMENTS");
		System.out.println();
		System.out.println("Change Class : " + change.getClass());
		
		if (change instanceof ChangeTransaction) {
			currentChangeTransaction = (ChangeTransaction)change;
			sourceChange = currentChangeTransaction.getSourceChange();
			System.out.println("Source change Class : " + sourceChange.getClass());
		}
		else {
			sourceChange = change;
		}
		
		
		if (sourceChange instanceof CompositionListInsertion) {
			addedElement = ((CompositionListInsertion)sourceChange).getAddedElement();
			int index = ((CompositionListInsertion)sourceChange).getIndex();
			if (addedElement != null)
				System.out.println("addedElement : " + addedElement.getClass());
			else {
				System.out.println("addedElement : null");
				System.out.println("index : " + index);
				addedElement = ((CompositionListInsertion)sourceChange).getAffectedElement();
			}
				
			System.out.println("affectedElement : " + ((CompositionListInsertion)sourceChange).getAffectedElement().getClass());
			System.out.println("feature : " + ((EReference)((CompositionListInsertion)sourceChange).getFeature()).getName());
			
			if (addedElement instanceof Comment) {
				System.out.println("Comment added id : " + ((Comment)addedElement).getId());
				MODIFIED_COMMENTS.add((Comment)addedElement);
			}
			else if (addedElement instanceof Post) {
				System.out.println("Post added id : " + ((Post)addedElement).getId());
				System.out.println("Comment added test id : " + ((Post)addedElement).getComments().get(index).getId());
				MODIFIED_COMMENTS.add(((Post)addedElement).getComments().get(index));
			}
		}
		else if (sourceChange instanceof AssociationCollectionInsertion) {
			addedElement = ((AssociationCollectionInsertion)sourceChange).getAddedElement();
			affectedElement = ((AssociationCollectionInsertion)sourceChange).getAffectedElement();
			feature = (EReference)((AssociationCollectionInsertion)sourceChange).getFeature();
			
			if (addedElement != null)
				System.out.println("addedElement : " + addedElement.getClass());
			else 
				System.out.println("addedElement : null");
			
			if (affectedElement != null)
				System.out.println("affectedElement : " + affectedElement.getClass());
			else 
				System.out.println("affectedElement : null");
			
			if (feature != null)
				System.out.println("feature : " + feature.getName());
			else 
				System.out.println("feature : null");
			
			if (addedElement instanceof Comment) {
				System.out.println("Comment liked id : " + ((Comment)addedElement).getId());
				MODIFIED_COMMENTS.add((Comment)addedElement);
			}
			else if (addedElement instanceof User && affectedElement instanceof User && "friends".contentEquals(feature.getName())) {
				List<Comment> commentsLikedByAdded = ((User)addedElement).getLikes();
				List<Comment> commentsLikedByAffected = ((User)affectedElement).getLikes();
				
				for (Comment comm : commentsLikedByAdded) {
					
					if (commentsLikedByAffected.contains(comm)) {
						System.out.println("Comment liked by two new friends : " + comm.getId());
						MODIFIED_COMMENTS.add(comm);
					}
				}
			}
		}

		System.out.println();
		System.out.println("FIN FIND UPDATED COMMENTS");
		System.out.println();
		System.out.println();
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
	
	public static Integer updateCommentScore(Comment comm) {
		Score keyToRemove = null;
		
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
		
		for (Entry<Score, String> podiumMember : PODIUM.entrySet()) {
			if (podiumMember.getValue().equals(comm.getId())) {
				keyToRemove = podiumMember.getKey();
			}
		}
		
		if (keyToRemove != null) {
			PODIUM.remove(keyToRemove);			
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

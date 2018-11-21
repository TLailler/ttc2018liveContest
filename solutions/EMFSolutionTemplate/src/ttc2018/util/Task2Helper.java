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
	 * List of groups of users who liked a comment (strongly connected components)
	 */
	private final static List<HashSet<User>> GROUPS_OF_USERS_WHO_LIKED = new ArrayList<>();
	
	/**
	 * For Update Task :
	 * Set containing comments to update after that changes have been applied
	 */
	private final static HashSet<Comment> MODIFIED_COMMENTS = new HashSet<Comment>();
	
	private Task2Helper() {}
	
	/**
	 * Method to call to compute the podium of the most influent comments
	 * @param socialNetwork - Social network to analyse
	 * @return String containing the podium (3 ids separated by the char '|')
	 */
	public static String calculatePodium(SocialNetworkRoot socialNetwork) {
		PODIUM.clear();
		
		for (Post post: socialNetwork.getPosts()) {
			for (Comment comm: post.getComments()) {
				calculateCommentScore(comm);
			}
		}
		return computePodiumResult();
	}
	
	/**
	 * For Update Task :
	 * Method to call to update the podium of the most influent comments
	 * @return String containing the podium (3 ids separated by the char '|')
	 */
	public static String updatePodium() {
		
		// Update the score of all the comments affected by changes applied  while updating the social network
		for (Comment comm: MODIFIED_COMMENTS) {
			updateCommentScore(comm);
		}
		MODIFIED_COMMENTS.clear();
		return computePodiumResult();
	}
	
	/**
	 * For Update Task :
	 * Find out if a change affects a (or several) Comment(s).
	 * If so, the comment(s) is(are) added the MODIFIED_COMMENTS set. 
	 * @param change
	 */
	public static void findUpdatedComments(ModelChange change) {
		ChangeTransaction currentChangeTransaction = null;
		ModelChange sourceChange = null;
		CompositionListInsertion compositionListInsertion = null;
		AssociationCollectionInsertion associationCollectionInsertion = null;
		EObject addedElement = null;
		EObject affectedElement = null;
		EReference feature = null;
		Integer index = null;

		if (change instanceof ChangeTransaction) {
			currentChangeTransaction = (ChangeTransaction)change;
			sourceChange = currentChangeTransaction.getSourceChange();
		}
		else {
			sourceChange = change;
		}
		
		// Case : a new Comment is added so its score has to be updated
		if (sourceChange instanceof CompositionListInsertion) {
			compositionListInsertion = (CompositionListInsertion)sourceChange;
			addedElement = compositionListInsertion.getAddedElement();
			index = compositionListInsertion.getIndex();
			
			if (addedElement == null)
				affectedElement = compositionListInsertion.getAffectedElement();
				
			if (addedElement instanceof Comment) {
				MODIFIED_COMMENTS.add((Comment)addedElement);
			}
			else if (affectedElement instanceof Post && index != null) {
				MODIFIED_COMMENTS.add(((Post)affectedElement).getComments().get(index));
			}
		}
		else if (sourceChange instanceof AssociationCollectionInsertion) {
			associationCollectionInsertion = (AssociationCollectionInsertion)sourceChange;
			addedElement = associationCollectionInsertion.getAddedElement();
			affectedElement = associationCollectionInsertion.getAffectedElement();
			feature = (EReference)(associationCollectionInsertion).getFeature();
			
			// Case : a Comment is liked by a User so its score has to be updated
			if (addedElement instanceof Comment) {
				MODIFIED_COMMENTS.add((Comment)addedElement);
			}
			// Case : two users become friends -> Scores of Comments liked by both of them
			//        have to be updated
			else if (addedElement instanceof User && affectedElement instanceof User && "friends".contentEquals(feature.getName())) {
				List<Comment> commentsLikedByAdded = ((User)addedElement).getLikes();
				List<Comment> commentsLikedByAffected = ((User)affectedElement).getLikes();
				
				for (Comment comm : commentsLikedByAdded) {
					
					if (commentsLikedByAffected.contains(comm)) {
						MODIFIED_COMMENTS.add(comm);
					}
				}
			}
		}
	}
	
	/**
	 * Method which calculates the Score of a given Comment
	 * @param comment
	 * @return points of the Comment's Score
	 */
	public static Integer calculateCommentScore(Comment comm) {
		
		for (Comment commentChild : comm.getComments()) {
			calculateCommentScore(commentChild);
		}
		GROUPS_OF_USERS_WHO_LIKED.clear();
		Integer score = 0;
		
		// Classify users who liked the Comment into strongly connected components
		for (User user: comm.getLikedBy()) {
			addToGroups(user);
		}
		
		// Comment's points are the sum of its squared strongly connected components sizes
		for (HashSet<User> group : GROUPS_OF_USERS_WHO_LIKED) {
			score += (group.size() * group.size()); 
		}
		addToPodium(score, comm.getTimestamp(), comm.getId());
		return score;
	}
	
	/**
	 * For Update Task :
	 * Method which updates the Score of a given Comment
	 * @param comment
	 * @return points of the Comment's Score
	 */
	public static Integer updateCommentScore(Comment comm) {
		Score keyToRemove = null;
		
		for (Comment commentChild : comm.getComments()) {
			calculateCommentScore(commentChild);
		}
		GROUPS_OF_USERS_WHO_LIKED.clear();
		Integer score = 0;
		
		// Classify users who liked the Comment into strongly connected components
		for (User user: comm.getLikedBy()) {
			addToGroups(user);
		}
		
		// Comment's points are the sum of its squared strongly connected components sizes
		for (HashSet<User> group : GROUPS_OF_USERS_WHO_LIKED) {
			score += (group.size() * group.size()); 
		}
		
		// If the updated Comment was already in the PODIUM with its previous Score, we have to remove it
		for (Entry<Score, String> podiumMember : PODIUM.entrySet()) {
			if (podiumMember.getValue().equals(comm.getId())) {
				keyToRemove = podiumMember.getKey();
			}
		}
		
		if (keyToRemove != null) {
			PODIUM.remove(keyToRemove);			
		}
		// Try to add the new Score to the PODIUM
		addToPodium(score, comm.getTimestamp(), comm.getId());
		return score;
	}
	
	/**
	 * Add a user to a group of users which contains at least one friend of the user
	 * Create a new group of users who liked the Comment if the user is not added to
	 * one of the existing groups.
	 * @param user
	 */
	public static void addToGroups(User user) {
		int currentNbOfGroups = GROUPS_OF_USERS_WHO_LIKED.size();
		List<Integer> groupIndexesWhereUserAdded = new ArrayList<Integer>();
		HashSet<User> group;

		// Add the user to groups to which there are strongly connected 
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
		
		// if the user wasn't added to a group,
		// create a new strongly connected component with this user
		if (nbGroupsWhereUserAdded == 0) {
			group = new HashSet<User>();
			group.add(user);
			GROUPS_OF_USERS_WHO_LIKED.add(group);
		}
		// if a user was added to several strongly connected components,
		// these groups are joined to create only one big strongly connected component 
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

	/**
	 * Add a Comment to the Podium if it has a big enough Score
	 * @param points - first part of the Score
	 * @param time - second part of the Score
	 * @param id - id of the Comment
	 */
	public static void addToPodium(Integer score, Date time, String id) {
		
		if (score == null || time == null) 
			return;
		
		// As a TreeMap, the PODIUM is automatically sorted by ScoreComparator
		PODIUM.put(new Score(score, time),id);
		
		// PODIUM's size must be lower or equal to 3
		if (PODIUM.size() > 3) {
			// remove first Comment (which has the lowest Score)
			PODIUM.remove(PODIUM.firstKey());
		}
	}
	
	/**
	 * Generate the string representation of the current PODIUM
	 * @return String containing the 3 Comment'ids separated by the char '|'
	 */
	public static String computePodiumResult() {
		String result = "";
		int size = PODIUM.size();
		
		if (size == 0) return result;
		
		Iterator<Score> iter = PODIUM.keySet().iterator();
		result += PODIUM.get(iter.next());
		
		while (iter.hasNext()) {
			result = PODIUM.get(iter.next()) + "|" + result; 
		}
		return result;
	}
}

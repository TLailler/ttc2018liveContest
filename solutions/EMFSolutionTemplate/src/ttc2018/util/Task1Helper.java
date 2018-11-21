package ttc2018.util;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.emf.ecore.EObject;

import Changes.AssociationCollectionInsertion;
import Changes.ChangeTransaction;
import Changes.CompositionListInsertion;
import Changes.ModelChange;
import SocialNetwork.Comment;
import SocialNetwork.Post;
import SocialNetwork.SocialNetworkRoot;

public class Task1Helper {
	
	/**
	 * Map containing the 3 most controversial posts
	 */
	private final static TreeMap<Score, String> PODIUM = new TreeMap<Score, String>(new ScoreComparator());
	
	/**
	 * For Update Task :
	 * Set containing posts to update after that changes have been applied
	 */
	private final static HashSet<Post> MODIFIED_POSTS = new HashSet<Post>();
	
	private Task1Helper() {}
	
	/**
	 * Method to call to compute the podium of the most controversial posts
	 * @param socialNetwork - Social network to analyse
	 * @return String containing the podium (3 ids separated by the char '|')
	 */
	public static String calculatePodium(SocialNetworkRoot socialNetwork) {
		PODIUM.clear();
		
		for (Post post: socialNetwork.getPosts()) {
			calculatePostScore(post);
		}
		return computePodiumResult();
	}
	
	/**
	 * For Update Task :
	 * Method to call to update the podium of the most controversial posts
	 * @return String containing the podium (3 ids separated by the char '|')
	 */
	public static String updatePodium() {
		
		// Update the score of all the posts affected by changes applied  while updating the social network
		for (Post post: MODIFIED_POSTS) {
			updatePostScore(post);
		}
		MODIFIED_POSTS.clear();
		return computePodiumResult();
	}
	
	/**
	 * For Update Task :
	 * Find out if a change affects a Post.
	 * If so, the post is added the MODIFIED_POSTS set. 
	 * @param change
	 */
	public static void findUpdatedPost(ModelChange change) {
		ChangeTransaction currentChangeTransaction = null;
		ModelChange sourceChange = null;
		CompositionListInsertion compositionListInsertion = null;
		EObject addedElement = null;

		if (change instanceof ChangeTransaction) {
			currentChangeTransaction = (ChangeTransaction)change;
			sourceChange = currentChangeTransaction.getSourceChange();
		}
		else {
			sourceChange = change;
		}
		
		// Case : a new Comment is added so its parent Post's score has to be updated
		if (sourceChange instanceof CompositionListInsertion) {
			compositionListInsertion = (CompositionListInsertion)sourceChange;
			addedElement = compositionListInsertion.getAddedElement();
			
			if (addedElement == null)
				addedElement = compositionListInsertion.getAffectedElement();
				
			if (addedElement instanceof Comment) {
				MODIFIED_POSTS.add(((Comment)addedElement).getPost());
			}
			else if (addedElement instanceof Post) {
				MODIFIED_POSTS.add((Post)addedElement);
			}
		}
		// Case : a Comment is liked by a User so its parent Post's score has to be updated
		else if (sourceChange instanceof AssociationCollectionInsertion) {
			addedElement = ((AssociationCollectionInsertion)sourceChange).getAddedElement();
			
			if (addedElement instanceof Comment) {
				MODIFIED_POSTS.add(((Comment)addedElement).getPost());
			}
		}
	}
	
	/**
	 * Method which calculates the Score of a given Post
	 * @param post
	 * @return points of the Post's Score
	 */
	public static Integer calculatePostScore(Post post) {
		Integer score = 0;
		
		// Post's points are the sum of its comments' points
		for (Comment commentChild : post.getComments()) {
			score += calculateCommentScore(commentChild);
		}
		addToPodium(score, post.getTimestamp(), post.getId());
		return score;
	}
	
	/**
	 * For Update Task :
	 * Method which updates the Score of a given Post
	 * @param post
	 * @return points of the Post's Score
	 */
	public static Integer updatePostScore(Post post) {
		Integer score = 0;
		Score keyToRemove = null;
		
		// Post's points are the sum of its comments' points
		for (Comment commentChild : post.getComments()) {
			score += calculateCommentScore(commentChild);
		}
		
		// If the updated Post was already in the PODIUM with its previous Score, we have to remove it
		for (Entry<Score, String> podiumMember : PODIUM.entrySet()) {
			if (podiumMember.getValue().equals(post.getId())) {
				keyToRemove = podiumMember.getKey();
			}
		}
		
		if (keyToRemove != null) {
			PODIUM.remove(keyToRemove);			
		}
		// Try to add the new Score to the PODIUM
		addToPodium(score, post.getTimestamp(), post.getId());	
		return score;
	}
	
	/**
	 * Method which calculates the score of a Comment
	 * @param comm
	 * @return points of the Comment's Score
	 */
	public static Integer calculateCommentScore(Comment comm) {
		Integer score = 10 + comm.getLikedBy().size();
		
		for (Comment commentChild : comm.getComments()) {
			score += calculateCommentScore(commentChild);
		}
		return score;
	}
	
	/**
	 * Add a Post to the Podium if it has a big enough Score
	 * @param points - first part of the Score
	 * @param time - second part of the Score
	 * @param id - id of the Post
	 */
	public static void addToPodium(Integer points, Date time, String id) {
		
		if (points == null || time == null) 
			return;
		
		// As a TreeMap, the PODIUM is automatically sorted by ScoreComparator
		PODIUM.put(new Score(points, time),id);
		
		// PODIUM's size must be lower or equal to 3
		if (PODIUM.size() > 3) {
			// remove first Post (which has the lowest Score)
			PODIUM.remove(PODIUM.firstKey());
		}
	}

	/**
	 * Generate the string representation of the current PODIUM
	 * @return String containing the 3 Post'ids separated by the char '|'
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

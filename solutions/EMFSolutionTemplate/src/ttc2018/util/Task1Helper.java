package ttc2018.util;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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

public class Task1Helper {
	
	private final static TreeMap<Score, String> PODIUM = new TreeMap<Score, String>(new ScoreComparator());
	private final static HashSet<Post> MODIFIED_POST = new HashSet<Post>();
	
	private Task1Helper() {}
	
	public static String calculatePodium(SocialNetworkRoot socialNetwork) {
		PODIUM.clear();
		
		for (Post post: socialNetwork.getPosts()) {
			calculatePostScore(post);
		}
		return computePodiumResult();
	}
	
	public static String updatePodium(SocialNetworkRoot socialNetwork) {
		
		System.out.println("Nb MODIFIED POST : " + MODIFIED_POST.size());
		for (Post post: MODIFIED_POST) {
			System.out.println("MODIFIED POST ID : " + post.getId());
			updatePostScore(post);
		}
		MODIFIED_POST.clear();
		return computePodiumResult();
	}
	
	public static void findUpdatedPost(ModelChange change) {
		ChangeTransaction currentChangeTransaction = null;
		ModelChange sourceChange = null;
		EObject addedElement = null;

		System.out.println();
		System.out.println();
		System.out.println("DEBUT FIND UPDATED POST");
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
			
			if (addedElement != null)
				System.out.println("addedElement : " + addedElement.getClass());
			else {
				System.out.println("addedElement : null");
				addedElement = ((CompositionListInsertion)sourceChange).getAffectedElement();
			}
				
			System.out.println("affectedElement : " + ((CompositionListInsertion)sourceChange).getAffectedElement().getClass());
			System.out.println("feature : " + ((EReference)((CompositionListInsertion)sourceChange).getFeature()).getName());
			
			if (addedElement instanceof Comment) {
				System.out.println("Comment id : " + ((Comment)addedElement).getId() + " ; post id : " + ((Comment)addedElement).getPost().getId());
				MODIFIED_POST.add(((Comment)addedElement).getPost());
			}
			else if (addedElement instanceof Post) {
				System.out.println("Post id : " + ((Post)addedElement).getId());
				MODIFIED_POST.add((Post)addedElement);
			}
		}
		else if (sourceChange instanceof AssociationCollectionInsertion) {
			addedElement = ((AssociationCollectionInsertion)sourceChange).getAddedElement();
			if (addedElement != null)
				System.out.println("addedElement : " + addedElement.getClass());
			else 
				System.out.println("addedElement : null");
			System.out.println("affectedElement : " + ((AssociationCollectionInsertion)sourceChange).getAffectedElement().getClass());
			System.out.println("feature : " + ((EReference)((AssociationCollectionInsertion)sourceChange).getFeature()).getName());
			
			if (addedElement instanceof Comment) {
				System.out.println("Comment id : " + ((Comment)addedElement).getId() + " ; post id : " + ((Comment)addedElement).getPost().getId());
				MODIFIED_POST.add(((Comment)addedElement).getPost());
			}
		}

		System.out.println();
		System.out.println("FIN FIND UPDATED POST");
		System.out.println();
		System.out.println();
	}
	
	public static Integer calculatePostScore(Post post) {
		Integer score = 0;
		for (Comment commentChild : post.getComments()) {
			score += calculateCommentScore(commentChild);
		}
		addToPodium(score, post.getTimestamp(), post.getId());
		return score;
	}
	
	public static Integer updatePostScore(Post post) {
		Integer score = 0;
		Score keyToRemove = null;
		
		for (Comment commentChild : post.getComments()) {
			score += calculateCommentScore(commentChild);
		}
		
		for (Entry<Score, String> podiumMember : PODIUM.entrySet()) {
			if (podiumMember.getValue().equals(post.getId())) {
				keyToRemove = podiumMember.getKey();
			}
		}
		
		if (keyToRemove != null) {
			PODIUM.remove(keyToRemove);			
		}
		addToPodium(score, post.getTimestamp(), post.getId());	
		return score;
	}
	
	public static Integer calculateCommentScore(Comment comm) {
		Integer score = 10 + comm.getLikedBy().size();
		
		for (Comment commentChild : comm.getComments()) {
			score += calculateCommentScore(commentChild);
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

package ttc2018;

import org.eclipse.emf.common.util.EList;

import Changes.ModelChange;
import Changes.ModelChangeSet;
import ttc2018.util.Task1Helper;

public class SolutionQ1 extends Solution {

	@Override
	public String Initial() {
		return Task1Helper.calculatePodium(this.getSocialNetwork());
	}

	@Override
	public String Update(ModelChangeSet changes) {
		EList<ModelChange> coll = changes.getChanges();
		
		for (ModelChange change : coll) {
			change.apply();
			Task1Helper.findUpdatedPost(change);
		}
		return Task1Helper.updatePodium(this.getSocialNetwork());
		
		// If we want to use Initial method for update
//		return Task1Helper.calculatePodium(this.getSocialNetwork());
	}

}

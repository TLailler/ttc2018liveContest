package ttc2018;

import org.eclipse.emf.common.util.EList;

import Changes.ModelChange;
import Changes.ModelChangeSet;
import ttc2018.util.Task2Helper;

public class SolutionQ2 extends Solution {

	@Override
	public String Initial() {
		return Task2Helper.calculatePodium(this.getSocialNetwork());
	}

	@Override
	public String Update(ModelChangeSet changes) {
		EList<ModelChange> coll = changes.getChanges();
		for (ModelChange change : coll) {
			change.apply();
		}
		return Task2Helper.calculatePodium(this.getSocialNetwork());
	}

}

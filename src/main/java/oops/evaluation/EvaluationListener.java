package oops.evaluation;

import oops.model.EvaluationResult;

public interface EvaluationListener {
	public void onEvaluationStarted();
	
	public void onEvaluationDone(EvaluationResult result);
	
	public void OnEvaluationException(Throwable exception);
}

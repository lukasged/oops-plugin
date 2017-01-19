package oops.evaluation;

import oops.model.EvaluationResult;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * A custom interface for OOPS! evaluation events listeners.
 */
public interface EvaluationListener {
	public void onEvaluationStarted();
	
	public void onEvaluationDone(EvaluationResult result);
	
	public void OnEvaluationException(Throwable exception);
}

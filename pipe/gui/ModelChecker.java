package pipe.gui;

import java.io.File;

public interface ModelChecker<TModel, TQuery> {
	boolean setup();
		
	String getVersion();
	boolean isCorrectVersion();
	
	String getPath(); // TODO: MJ -- Delete me when refactoring is done

	VerificationResult verify(TModel model, TQuery query, VerificationOptions options);
	// TODO: MJ -- get rid of xmlFile and queryFile.. Legacy stuff to support older reductions
	VerificationResult verify(TModel model, TQuery query, VerificationOptions options, File xmlFile, File queryFile);
}
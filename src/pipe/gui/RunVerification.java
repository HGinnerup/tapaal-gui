/**
 * 
 */
package pipe.gui;

import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNTrace;
import pipe.gui.GuiFrame.GUIMode;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunVerification extends RunVerificationBase {

	public RunVerification(ModelChecker modelChecker, Messenger messenger) {
		super(modelChecker, messenger);
	}

	@Override
	protected void showResult(VerificationResult result, long verificationTime) {	
		if(result != null && !result.error()){
			String satisfaction = result.isQuerySatisfied() ? "satisfied" : "not satisfied";
			JOptionPane.showMessageDialog(CreateGui.getApp(), 
					String.format("Property is %1$s.\nEstimated verification time: %2$.2fs", satisfaction, verificationTime/1000.0),
					"Verification Result", JOptionPane.INFORMATION_MESSAGE);
			
			if(result.getTrace() != null){
				DataLayer model = CreateGui.getModel();
				TraceTransformer interpreter =  model.isUsingColors() ? new ColoredTraceTransformer(model) : new TraceTransformer(model);
				TAPNTrace trace = interpreter.interpretTrace(result.getTrace());
				CreateGui.getApp().setGUIMode(GUIMode.animation);
				CreateGui.getAnimator().SetTrace(trace);
			}else if(result.getTrace() == null && shouldFindTrace(result)){
				messenger.displayErrorMessage("A trace could not be generated by the chosen verification engine.");
			}
		}else{
			
			//Check if the is something like 
			//verifyta: relocation_error:
			///usr/lib32/libnss_msdn4_minimal.so.2 symbol strlen, 
			//version GLIB_2.0 not defined in file libc.so.6 with
			//link time reference
			//is the error as this (often) means the possibility for a uppaal licence key error
			
			String extraInformation = "";
			
			if (result != null && (result.errorMessage().contains("relocation") || result.errorMessage().toLowerCase().contains("internet connection is required for activation"))){
				
				extraInformation = "We have detected an error that often arises when UPPAAL is missing a valid Licence File.\n" +
						"Please open the UPPAAL GUI while connected to the internet, to correct this problem.";
				
			}
			
			String message = "An error occured during the verification." +
			System.getProperty("line.separator") + 	
			System.getProperty("line.separator");
			
			if (extraInformation != ""){
				message += extraInformation +			
				System.getProperty("line.separator") + 	
				System.getProperty("line.separator");
			}
			
			message += "UPPAAL output:\n" + result.errorMessage();
			
			messenger.displayWrappedErrorMessage(message,"Error during verification");
		}
	}

	private boolean shouldFindTrace(VerificationResult result) {
		boolean askedForTrace = !options.getOption("trace").equalsIgnoreCase("NONE");
		boolean queryAllowsTrace = (query.getProperty() instanceof TCTLEFNode && result.isQuerySatisfied()) 
			|| (query.getProperty() instanceof TCTLEGNode && result.isQuerySatisfied())
			|| (query.getProperty() instanceof TCTLAGNode && !result.isQuerySatisfied())
			|| (query.getProperty() instanceof TCTLAFNode && !result.isQuerySatisfied());
		
		return askedForTrace && queryAllowsTrace;
	}
}
package net.tapaal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.prefs.BackingStoreException;

import org.jdesktop.swingx.MultiSplitLayout.Split;

public class Preferences {

	private static Preferences instance = null;
	private static java.util.prefs.Preferences pref;

	protected Preferences() {
		// Exists only to defeat instantiation.
		pref = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
	}

	public void clear(){
		try {
			pref.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public static Preferences getInstance() {
		if(instance == null) {
			instance = new Preferences();
		}
		return instance;
	}

	public String getVerifytaLocation() {

		return pref.get("verifyta.location", "");
	}

	public void setVerifytaLocation(String location) {
		final String key = "verifyta.location";

		if (location == null || location.equals("")){
			pref.remove(key);
		} else {
			pref.put("verifyta.location", location);
		}


	}

	public String getVerifytapnLocation() {
		return pref.get("verifytapn.location", "");
	}

	public void setVerifytapnLocation(String location) {
		final String key = "verifytapn.location";

		if (location == null || location.equals("")){
			pref.remove(key);
		}else {
			pref.put(key, location);   
		}
	}

	public String getLatestVersion() {
		return pref.get("tapaal.latestVersion", "");
	}

	public void setLatestVersion(String version) {
		final String key = "tapaal.latestVersion";

		if (version == null || version.equals("")){
			pref.remove(key);
		}else {
			pref.put(key, version);   
		}
	}

	/* Workspace */
	//General
	public void setShowToolTips(boolean show){
		pref.putBoolean("showToolTips", show);
	}

	public boolean getShowToolTips(){
		return pref.getBoolean("showToolTips", true);
	}

	//Queries
	public void setAdvancedQueryView(boolean advanced){
		pref.putBoolean("queryAdvanced", advanced);
	}

	public boolean getAdvancedQueryView(){
		return pref.getBoolean("queryAdvanced", false);
	}

	//Editor
	public void setShowComponents(boolean show){
		pref.putBoolean("componentsPanel", show);
	}

	public boolean getShowComponents(){
		return pref.getBoolean("componentsPanel", true);
	}

	public void setShowQueries(boolean show){
		pref.putBoolean("QueriesPanel", show);
	}

	public boolean getShowQueries(){
		return pref.getBoolean("QueriesPanel", true);
	}

	public void setShowConstants(boolean show){
		pref.putBoolean("constantPanel", show);
	}

	public boolean getShowConstants(){
		return pref.getBoolean("constantPanel", true);
	}

	public void setEditorModelRoot(Split modelRoot){
		if(modelRoot == null){
			return;
		}
		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(out);

			//Serialize model
			oos.writeObject(modelRoot);
			oos.close();

			pref.putByteArray("editorModelRoot", out.toByteArray());
		} catch (IOException e){
			System.err.println("Something went wrong couldn't save workspace");
		}
	}

	public Split getEditorModelRoot(){
		byte[] model = pref.getByteArray("editorModelRoot", null);
		if(model == null){
			return null;
		}
		Split editorModelroot = null;

		try{ 
			ByteArrayInputStream in = new ByteArrayInputStream(model);
			ObjectInputStream ois = new ObjectInputStream(in);

			//Read in the model
			editorModelroot = (Split)ois.readObject();
			ois.close();
		} catch (Exception e){
			System.err.println("Something went wrong didn't load saved workspace");
		}
		return editorModelroot;
	}

	//Simulator
	public void setShowEnabledTrasitions(boolean show){
		pref.putBoolean("enabledTransitionsPanel", show);
	}

	public boolean getShowEnabledTransitions(){
		return pref.getBoolean("enabledTransitionsPanel", true);
	}

	//Drawing surface
	public void setShowZeroInfIntervals(boolean show){
		pref.putBoolean("showZeroInfIntervals", show);
	}

	public boolean getShowZeroInfIntervals(){
		return pref.getBoolean("showZeroInfIntervals", true);
	}
}

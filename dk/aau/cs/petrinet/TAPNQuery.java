package dk.aau.cs.petrinet;

public class TAPNQuery {
	private String pathQuantifier; // E or A
	private String nodeQuantifier; // F or G
	
	private String remQuery; // TODO: make this more object oriented (modify query dialog
	private int totalTokens = 0;

	public TAPNQuery(String inputQuery, int totalTokens){
		String query = inputQuery.trim();
		parseQuery(query);
		this.totalTokens = totalTokens;
	}

	private void parseQuery(String query) {
		pathQuantifier = query.substring(0,1);
		nodeQuantifier = query.substring(1,3);
		
		remQuery = query.substring(3, query.length());
	}
	
	@Override
	public String toString(){
		return pathQuantifier + nodeQuantifier + remQuery;
	}

	public int getTotalTokens() {
		return totalTokens;
	}
	
}
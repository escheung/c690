
public class Trip {

	private String _Subject;
	private String _Object;
	private String _Predicate;
	
	public Trip() {
		_Subject = "";
		_Object = "";
		_Predicate = "";
		
	}
	
	public Trip(String s, String p, String o) {
		
		_Subject = s;
		_Predicate = p;
		_Object = o;
		
	}
	
	public String getSubject() {
		return _Subject;
	}
	public String getObject() {
		return _Object;
	}
	public String getPredicate() {
		return _Predicate;
	}
	public String toString() {
		return String.format("%s %s %s", this._Subject, this._Predicate, this._Object);
	}
}

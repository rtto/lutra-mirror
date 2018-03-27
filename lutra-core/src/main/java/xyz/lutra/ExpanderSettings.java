package xyz.lutra;

public class ExpanderSettings {
	
	
	private boolean includeHead = false;
	private boolean includeCall = false;
	private boolean includeBody = true;
	private int depth = -1; // infinite depth

	private ExpanderSettings () {}
	
	private ExpanderSettings (boolean includeHead, boolean includeCall, boolean includeBody, int depth) {
		this.includeHead = includeHead;
		this.includeCall = includeCall;
		this.includeBody = includeBody;
		this.depth = depth;
	}
	
	public static final ExpanderSettings ALL   = new ExpanderSettings(true, true, true, -1);
	public static final ExpanderSettings BODY  = new ExpanderSettings(false, false, true, -1);
	public static final ExpanderSettings HEAD  = new ExpanderSettings(true, true, false, -1);

	public boolean isIncludeHead() {
		return includeHead;
	}

	public void setIncludeHead (boolean includeHead) {
		this.includeHead = includeHead;
	}
	
	public boolean isIncludeBody() {
		return includeBody;
	}

	public void setIncludeBody (boolean includeBody) {
		this.includeBody = includeBody;
	}

	public boolean isIncludeCall() {
		return includeCall;
	}

	public void setIncludeCall (boolean includeCall) {
		this.includeCall = includeCall;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	@Override
    public int hashCode() {
        int hash = 13;
        hash *= (includeHead ? 17 : 19);
        hash *= (includeCall ? 23 : 29);
        hash *= 31 * depth;
        hash *= (includeCall ? 37 : 41);
        return hash;
    }
}

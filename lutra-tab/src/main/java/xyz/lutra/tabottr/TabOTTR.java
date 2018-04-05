package xyz.lutra.tabottr;

public abstract class TabOTTR {
	
	public static final String TOKEN = "#OTTR";
	
	public static final String INSTRUCTION_TEMPLATE = "template";
	public static final String INSTRUCTION_PREFIX = "prefix";
	public static final String INSTRUCTION_END = "end";
	
	public static final String TYPE_IRI = "iri";
	public static final String TYPE_BLANK = "blank";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_AUTO = "auto";
	public static final String TYPE_LIST_POSTFIX = "+";
	
	public static final String VALUE_LIST_SEPARATOR = "|";
	public static final String VALUE_FRESH_BLANK = "*";
	public static final String VALUE_BLANK_NODE_PREFIX = "_:";
	public static final String VALUE_LANGUAGE_TAG_PREFIX = "@@";
}

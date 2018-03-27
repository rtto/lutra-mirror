package xyz.lutra;

public class Settings {

	public static final String vocabularyRoot = "http://ns.ottr.xyz/";
	
	// web
	public static final String servletRoot = "http://osl.ottr.xyz";
	public static final String servletParamTemplate = "tpl";
	public static final String servletParamFormat = "fmt";

	public static final String
	servletExpansionServiceAll            = "/expansion/all",
	servletExpansionServiceBody           = "/expansion/body",
	servletExpansionServiceHead           = "/expansion/head",

	servletSpecificationServiceAll        = "/specification/all",
	servletSpecificationServiceBody       = "/specification/body",
	servletSpecificationServiceHead       = "/specification/head",
	servletSpecificationServiceExpansion  = "/specification/expansion",

	servletFormatServiceHeadXML           = "/format/head/xml",

	servletSampleServiceHeadXML           = "/sample/head/xml",

	servletLiftingServiceSelect           = "/lifting/select",
	servletLiftingServiceConstruct        = "/lifting/construct",
	servletLiftingServiceUpdate           = "/lifting/update",
	servletLoweringServiceSelect          = "/lowering/select",
	servletLoweringServiceConstruct       = "/lowering/construct",
	servletLoweringServiceUpdate          = "/lowering/update"
	;
	
	
	// Parser checks
	public static boolean enableSemanticTemplateVocabularyExpansionCheck = true;
	public static boolean enableSemanticExpansionCheck = true;
	public static boolean enableSyntaxTemplateVocabularyCheck = true;
	public static boolean enableSemanticTemplateVocabularyCheck = true;
	public static boolean enableXMLSampleValidation = true;
	public static boolean enableXSDStingValidation = true;

	// Cache
	public static  boolean enableTempalteParserCache = true;
	public static  boolean enableExpanderCache = true;
	public static  boolean enableTemplateQueriesCache = true;
	public static  boolean enableTempalteXSDCache = true;
	public static  boolean enableTempalteXMLSampleCache = true;

	
	
	
	public static void enableValidation (boolean b) {
		enableSyntaxTemplateVocabularyCheck = b;
		enableSemanticTemplateVocabularyCheck = b;
		enableXMLSampleValidation = b;
		enableXSDStingValidation = b;
		enableSemanticExpansionCheck = b;
	}
	
	public static void enableCache (boolean b) {
		enableTempalteParserCache = b;
		enableExpanderCache = b;
		enableTemplateQueriesCache = b;
		enableTempalteXSDCache = b;
		enableTempalteXMLSampleCache = b;
	}

}

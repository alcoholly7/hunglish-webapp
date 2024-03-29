package hu.mokk.hunglish.web;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.domain.Genre;
import hu.mokk.hunglish.lucene.SearchRequest;
import hu.mokk.hunglish.lucene.SearchResult;
import hu.mokk.hunglish.lucene.Searcher;
import hu.mokk.hunglish.util.Pair;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RooWebScaffold(path = "search", automaticallyMaintainView = true, formBackingObject = Bisen.class)
@RequestMapping("/search/**")
@Controller
public class SearchController {
	private static Log logger = LogFactory.getLog(SearchController.class);
	
	public static final int PAGE_SIZE = 20;
	public static final int MAX_PAGE_LINKS = 20;
	public static final String BACK_ARROW = "<<";
	public static final String FORWARD_ARROW = ">>";
	public static final int PAGINATION_LINK_WINDOW_SIZE = 10;
			
	@Autowired
	private Searcher searcher;

	//TODO eliminate this method by assembling request in View layer
	private SearchRequest transformRequest(Bisen bisen, int sizeNo, int pageNo){
		SearchRequest request = new SearchRequest();
		
		request.setEnQuery(bisen.getEnSentence());
		request.setHuQuery(bisen.getHuSentence());
		if (bisen.getDoc() != null && bisen.getDoc().getGenre() != null
				&& bisen.getDoc().getGenre().getId() != null) {
			request.setGenreId(bisen.getDoc().getGenre().getId().toString());
		}
		request.setMaxResults(sizeNo);
		request.setStartOffset((pageNo-1)*sizeNo);
		request.setHunglishSyntax(true);
		return request;
	}

	private List<Pair<String, String>> getPaginationLinks(SearchRequest request, Bisen bisen, 
			int sizeNo, int maxPages, int pageNo) throws UnsupportedEncodingException {
        String baseUrl = getBaseUrl(bisen.getHuSentence(), bisen.getEnSentence()) +"&size="+sizeNo;
        List<Pair<String, String>> linx = new ArrayList<Pair<String, String>>();
        String url;
        if (pageNo > 1) {
        	url =baseUrl+"&page="+new Integer(pageNo-1).toString();
        	if (request.getGenreId() != null){
        		url += "&doc.genre="+request.getGenreId();
        	}
        	linx.add(new Pair<String, String>(url, BACK_ARROW));        	
        }
        int start = Math.max(1, pageNo - PAGINATION_LINK_WINDOW_SIZE);
        int end = Math.min(maxPages, pageNo + PAGINATION_LINK_WINDOW_SIZE);
        for (int i = start; i <= end; i++){
        	url =baseUrl+"&page="+i;
        	if (request.getGenreId() != null){
        		url += "&doc.genre="+request.getGenreId();
        	}
        	linx.add(new Pair<String, String>(url, new Integer(i).toString()));
        }
    	
        if (pageNo < maxPages) {
        	url =baseUrl+"&page="+new Integer(pageNo+1).toString();
        	if (request.getGenreId() != null){
        		url += "&doc.genre="+request.getGenreId();
        	}
        	linx.add(new Pair<String, String>(url, FORWARD_ARROW));        	
        }
    	
        return linx;
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String search(
    		@RequestParam(value = "page", required = false) Integer page, 
    		@RequestParam(value = "size", required = false) Integer size, 
			Bisen bisen, //this is a hack, this holds the search terms! TODO clear it up 
			ModelMap modelMap) throws UnsupportedEncodingException {


        int sizeNo = size == null ? PAGE_SIZE : size.intValue();
        int pageNo = page == null ? 1 : page.intValue();
		
        SearchRequest request = transformRequest(bisen, sizeNo, pageNo);
		
        modelMap.addAttribute("request", request);
		modelMap.addAttribute("genres", Genre.findAllGenres());

		SearchResult result = searcher.search(request);
		
        float nrOfPages = (float) result.getTotalCount() / sizeNo;
        int maxPages = (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
        
        float nrOfPagesLimited = (float) searcher.getMaxDocuments() / sizeNo;
        int maxPagesLimited = (int) nrOfPagesLimited; 
                
        modelMap.addAttribute("maxPages", maxPages);
        modelMap.addAttribute("page", pageNo);
        
        result.setPaginationLinks(getPaginationLinks(request, bisen, sizeNo, (int)Math.min(maxPagesLimited, maxPages), pageNo));
        
        if (result.getEnSuggestionString() != null || result.getHuSuggestionString() != null){
        	String suggestionURL = getBaseUrl(result.getHuSuggestionString(), result.getEnSuggestionString()) +"&size="+sizeNo;
        	if (request.getGenreId() != null){
        		suggestionURL += "&doc.genre="+request.getGenreId();
        	}
        	modelMap.addAttribute("suggestionURL", suggestionURL);
        }
        modelMap.addAttribute("result", result);
		
		return "search/list";
	}
	
	private String getBaseUrl(String huSentence, String enSentence) throws UnsupportedEncodingException{
		 String baseUrl = "?huSentence=";
		 if (huSentence != null){
			 baseUrl += URLEncoder.encode(huSentence, "UTF-8");
		 }
		 baseUrl += "&enSentence=";
		 if (enSentence != null){
			 baseUrl += URLEncoder.encode(enSentence, "UTF-8");
		 }
		 return baseUrl;
	}
}

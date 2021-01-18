package de.uniwue.web.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uniwue.web.communication.SegmentationRequest;
import de.uniwue.web.communication.BatchSegmentationRequest;
import de.uniwue.web.config.LarexConfiguration;
import de.uniwue.web.facade.segmentation.LarexFacade;
import de.uniwue.web.facade.segmentation.SegmentationSettings;
import de.uniwue.web.io.FileDatabase;
import de.uniwue.web.io.FilePathManager;
import de.uniwue.web.model.Page;
import de.uniwue.web.model.PageAnnotations;

/**
 * Communication Controller to handle requests for the main viewer/editor.
 * Handles requests about displaying book scans and segmentations.
 * 
 */
@Controller
@Scope("session")
public class SegmentationController {
	@Autowired
	private ServletContext servletContext;
	@Autowired
	private FilePathManager fileManager;
	@Autowired
	private LarexConfiguration config;
	/**
	 * Progress of the batchSegmentation process
	 */
	private int segProgress = -1;

	/**
	 * Initialize the controller by loading the fileManager and settings if not
	 * loaded already.
	 **/
	@PostConstruct
	private void init() {
		if (!fileManager.isInit()) {
			fileManager.init(servletContext);
		}
		if (!config.isInitiated()) {
			config.read(new File(fileManager.getConfigurationFile()));
			String bookFolder = config.getSetting("bookpath");
			if (!bookFolder.equals("")) {
				fileManager.setLocalBooksPath(bookFolder);
			}
		}
		this.segProgress = 0;
	}

	@RequestMapping(value = "segmentation/segment", method = RequestMethod.POST, headers = "Accept=*/*",
									produces = "application/json", consumes = "application/json")
	public @ResponseBody PageAnnotations segment(@RequestBody SegmentationRequest segmentationRequest) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"), fileManager.checkFlat());
		return LarexFacade.segmentPage(segmentationRequest.getSettings(), segmentationRequest.getPage(), fileManager, database);
	}

	@RequestMapping(value = "segmentation/batchSegment", method = RequestMethod.POST, headers = "Accept=*/*",
			produces = "application/json", consumes = "application/json")
	public @ResponseBody List<PageAnnotations> batchSegment(@RequestBody BatchSegmentationRequest batchSegmentationRequest) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"), fileManager.checkFlat());
		List<PageAnnotations> results = new ArrayList<>();
		this.segProgress = 0;
		for(int page: batchSegmentationRequest.getPages()){
			PageAnnotations result = LarexFacade.segmentPage(batchSegmentationRequest.getSettings(), page, fileManager, database);
			results.add(result);
			this.segProgress++;
		}
		return results;
	}

	@RequestMapping(value = "segmentation/settings", method = RequestMethod.POST)
	public @ResponseBody SegmentationSettings getBook(@RequestParam("bookid") int bookID) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"), fileManager.checkFlat());
		if(fileManager.checkFlat()) {
			return new SegmentationSettings(database.getBook(bookID));
		} else {
			return new SegmentationSettings(database.getBook(fileManager.getNonFlatBookName(),fileManager.getNonFlatBookId(),fileManager.getLocalImageMap()));
		}

	}
	@RequestMapping(value = "segmentation/empty", method = RequestMethod.POST)
	public @ResponseBody PageAnnotations emptysegment(@RequestParam("bookid") int bookID,
			@RequestParam("pageid") int pageID) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"), fileManager.checkFlat());
		Page page;
		if(fileManager.checkFlat()) {
			page = database.getBook(bookID).getPage(pageID);
		} else {
			page = database.getBook(fileManager.getNonFlatBookName(),fileManager.getNonFlatBookId(),fileManager.getLocalImageMap()).getPage(pageID);
		}

		return new PageAnnotations(page.getName(), page.getWidth(), page.getHeight(), page.getId());
	}

	/**
	 * Response to the request to return the progress status of the adjust files service
	 *
	 * @param session Session of the user
	 * @return Current progress (range: 0 - 100)
	 */
	@RequestMapping(value = "segmentation/batchSegmentProgress" , method = RequestMethod.GET)
	public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {return this.segProgress; }
}

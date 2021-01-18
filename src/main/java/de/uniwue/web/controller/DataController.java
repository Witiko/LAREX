package de.uniwue.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import de.uniwue.web.communication.BatchLoadRequest;
import de.uniwue.web.communication.BatchSegmentationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import de.uniwue.web.config.LarexConfiguration;
import de.uniwue.web.io.FileDatabase;
import de.uniwue.web.io.FilePathManager;
import de.uniwue.web.io.PageXMLReader;
import de.uniwue.web.model.Book;
import de.uniwue.web.model.Page;
import de.uniwue.web.model.PageAnnotations;

/**
 * Communication Controller to handle project data requests.
 * Provide information about books, pages and more.
 */
@Controller
@Scope("request")
public class DataController {
	@Autowired
	private ServletContext servletContext;
	@Autowired
	private FilePathManager fileManager;
	@Autowired
	private LarexConfiguration config;

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
	}
	
	/**
	 * Return informations about a book
	 * 
	 * @param bookID
	 * @return
	 */
	@RequestMapping(value = "data/book", method = RequestMethod.POST)
	public @ResponseBody Book getBook(@RequestParam("bookid") int bookID) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"), fileManager.checkFlat());
		if(fileManager.checkFlat()) {
			return database.getBook(bookID);
		} else {
			return database.getBook(fileManager.getNonFlatBookName(),fileManager.getNonFlatBookId(),fileManager.getLocalImageMap());
		}

	}

	/**
	 * Return the annotations of a page if exists or empty annotations 
	 *  
	 * @param bookID
	 * @param pageID
	 * @return
	 */
	@RequestMapping(value = "data/page/annotations", method = RequestMethod.POST)
	public @ResponseBody PageAnnotations getAnnotations(@RequestParam("bookid") int bookID, @RequestParam("pageid") int pageID) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"), fileManager.checkFlat());

		Book book;
		Page page;
		File annotationsPath;

		if(fileManager.checkFlat()) {
			book = database.getBook(bookID);
			page = book.getPage(pageID);
			annotationsPath = fileManager.getAnnotationPath(book.getName(), page.getName());
		} else {
			book = database.getBook(fileManager.getNonFlatBookName(),fileManager.getNonFlatBookId(),fileManager.getLocalImageMap());
			page = book.getPage(pageID);
			annotationsPath = new File(fileManager.getLocalXmlMap().get(page.getName() + ".xml"));
		}

		if (annotationsPath.exists()) {
			return PageXMLReader.loadPageAnnotationsFromDisc(annotationsPath);
		} else {
			return new PageAnnotations(page.getName(), page.getWidth(), page.getHeight(),
					page.getId());
		}
	}

	/**
	 * Return the annotations of one or multiple pages if exists or empty annotations
	 *x
	 * @param batchLoadRequest
	 * @return
	 */
	@RequestMapping(value = "data/page/batchAnnotations", method = RequestMethod.POST, headers = "Accept=*/*",
			produces = "application/json", consumes = "application/json")
	public @ResponseBody List<PageAnnotations> getBatchAnnotations(@RequestBody BatchLoadRequest batchLoadRequest) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"), fileManager.checkFlat());

		Book book;
		if(fileManager.checkFlat()) {
			book = database.getBook(batchLoadRequest.getBookid());
		} else {
			book = database.getBook(fileManager.getNonFlatBookName(),fileManager.getNonFlatBookId(),fileManager.getLocalImageMap());
		}
		List<PageAnnotations> pageAnnotations = new ArrayList<>();
		for( int pageID : batchLoadRequest.getPages()) {
			Page page = book.getPage(pageID);
			File annotationsPath;
			if(fileManager.checkFlat()) {
				annotationsPath = fileManager.getAnnotationPath(book.getName(), page.getName());
			} else {
				annotationsPath = new File(fileManager.getLocalXmlMap().get(page.getName() + ".xml"));
			}
			if (annotationsPath.exists()) {
				pageAnnotations.add(PageXMLReader.loadPageAnnotationsFromDisc(annotationsPath));
			} else {
				pageAnnotations.add( new PageAnnotations(page.getName(), page.getWidth(), page.getHeight(),
						page.getId()));
			}
		}

		return pageAnnotations;
	}

	/**
	 * Return if page annotations for the pages of a book exist
	 * 
	 * @param bookID
	 * @return Map of PageNr -> Boolean : True if annotations file exist on the server
	 */
	@RequestMapping(value = "data/status/all/annotations", method = RequestMethod.POST)
	public @ResponseBody Collection<Integer> getAnnotationAllStatus(@RequestParam("bookid") int bookID) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"), fileManager.checkFlat());
		if(fileManager.checkFlat()) {
			return database.getPagesWithAnnotations(bookID);
		} else {
			return database.getPagesWithAnnotations(fileManager.getNonFlatBookName(),fileManager.getNonFlatBookId(), fileManager.getLocalImageMap(), fileManager.getLocalXmlMap());
		}
	}

	/**
	 * Retrieve the default virtual keyboard.
	 *
	 * @return default virtual keyboard
	 */
	@RequestMapping(value = "data/virtualkeyboard", method = RequestMethod.POST)
	public @ResponseBody List<String[]> virtualKeyboard() {
		File virtualKeyboard = new File(fileManager.getVirtualKeyboardFile());

		List<String[]> keyboard = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(virtualKeyboard))) {
			String st; 
			while ((st = br.readLine()) != null) 
				if(st.replace("\\s+", "").length() > 0) 
					keyboard.add(st.split("\\s+"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return keyboard;
	}

	/**
	 * Retrieve a preset virtual keyboard.
	 *
	 * @param language
	 * @return vk
	 */
	@RequestMapping(value = "data/virtualkeyboardPreset", method = RequestMethod.POST)
	public @ResponseBody List<String[]> virtualKeyboardPreset(String language) {
		File virtualKeyboard = new File(fileManager.getVirtualKeyboardFile(language));

		List<String[]> keyboard = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(virtualKeyboard))) {
			String st;
			while ((st = br.readLine()) != null)
				if(st.replace("\\s+", "").length() > 0)
					keyboard.add(st.split("\\s+"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return keyboard;
	}

	/**
	 * Returns whether LAREX is configured to be used in conjunction with OCR4all or not.
	 */
	@RequestMapping(value = "config/ocr4all", method = RequestMethod.POST, headers = "Accept=*/*",
			produces = "application/json")
	public @ResponseBody Boolean isOCR4allMode() {
		String ocr4allMode = config.getSetting("ocr4all");
		return ocr4allMode.equals("enable");
	}
}
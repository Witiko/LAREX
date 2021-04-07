package de.uniwue.web.controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import de.uniwue.web.io.MetsReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.json.JSONObject;

import de.uniwue.web.io.MetsReader;
import de.uniwue.web.config.LarexConfiguration;
import de.uniwue.web.io.FileDatabase;
import de.uniwue.web.io.FilePathManager;
import de.uniwue.web.model.Library;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Communication Controller to handle simple requests about the book library.
 * 
 */
@Controller
@Scope("request")
public class LibraryController {
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
	 * Display a list of all books present in the book path. Clicking on a book will
	 * open it in the larex view.
	 * 
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/")
	public String home(Model model) throws IOException {
		// Reset config
		String bookFolder = config.getSetting("bookpath");
		if (!bookFolder.equals("")) {
			fileManager.setLocalBooksPath(bookFolder);
		}
		File bookPath = new File(fileManager.getLocalBooksPath());
		bookPath.isDirectory();
		FileDatabase database = new FileDatabase(bookPath, config.getListSetting("imagefilter"), false);
		Library lib = new Library(database);

		model.addAttribute("library", lib);
		return "lib";
	}

	/**
	 * Display a list of all books present in the book path. Clicking on a book will
	 * open it in the larex view.
	 *
	 * @param bookid
	 * @param bookpath
	 * @param type
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "library/getPageLocations", method = RequestMethod.POST, headers = "Accept=*/*")
	public @ResponseBody Map<String, String> getPageLocations(@RequestParam(value = "bookid") int bookid, @RequestParam(value = "bookpath") String bookpath, @RequestParam(value = "booktype") String type) throws IOException {
		fileManager.init(servletContext);
		String booktype = "";
		try {
			booktype = java.net.URLDecoder.decode(type, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		File baseFolder = new File(bookpath);
		if(!baseFolder.isDirectory()) {
			throw new IOException("Path is no directory, but should be in this instance");
		}
		try {
			switch (booktype) {

				case "legacy":
					Map<String, String> map = getFileMap(baseFolder.getAbsolutePath(), ".png");
					return map;
				default:
					System.out.println("Attempting to open empty directory");
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * Opens Mets file and returns all known filegroups and each imageLocation
	 *
	 * @param metsPath path to mets.xml
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "library/getMetsData", method = RequestMethod.POST, headers = "Accept=*/*")
	public @ResponseBody Map<String, List<String>> getMetsData(@RequestParam("metspath") String metsPath) throws IOException {
		if (!fileManager.isInit()) {
			fileManager.init(servletContext);
		}
		String mets = "";
		try {
			mets = java.net.URLDecoder.decode(metsPath, StandardCharsets.UTF_8.name()) + File.separator + "mets.xml";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		File metsFile = new File(mets);
		if(!metsFile.exists()) { throw new IOException("Mets file doesn't exist anymore"); }
		return MetsReader.getFileGroups(mets, false);
	}

	/**
	 * Opens Mets file and returns all known filegroups and each imageLocation
	 *
	 * @param baseFolder path to legacy baseFolder
	 * @param ext file extension to map
	 * @return map containing imageName and path
	 */
	public Map<String, String> getFileMap(String baseFolder, String ext) {
		Map<String, String> fileMap = new LinkedHashMap<String, String>();
		File directFolder = new File(baseFolder);
		List<File> files = Arrays.stream(directFolder.listFiles()).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
		for (File file : files) {
			if(file.getName().endsWith(ext)) {
				String path = file.getAbsolutePath();
				fileMap.put(file.getName(), path);
			}
		}
		return fileMap;
	}
}

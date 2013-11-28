package edu.yale.sml.logic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.Report;
import edu.yale.sml.model.SearchResult;
import edu.yale.sml.model.ShelvingError;
import edu.yale.sml.persistence.BarcodeSearchDAO;
import edu.yale.sml.view.NullFileException;

/**
 * Types of methods: decorate, filter, process, calculate diff
 * 
 * @author od26
 * 
 */
public class BasicShelfScanEngine implements java.io.Serializable,
	ShelfScanEngine {
    private static final long serialVersionUID = -1871752891918863039L;

    // Do not remove/rename fields:

    final static Logger logger = LoggerFactory
	    .getLogger(BasicShelfScanEngine.class);
    public static final String ITEM_FLAG_STRING = "*";
    public static final String ENUM_FLAG_STRING = "@";
    public static final String LC_STRING = "( LC )";
    public static final int MAX_QUERY_COUNT = 1500;
    public static final String NOT_CHARGED_STRING = "Not Charged";
    public static final String NULL_BARCODE_STRING = "00000000";
    public static final String RESET_BARCODE = "";
    public static final int MIN_ERROR_DISPLAY = 2;

    private List<OrbisRecord> badBarcodes;
    private List<Report> culpritList;
    private List<Report> enumWarnings = new ArrayList<Report>();
    private int nullBarcodes = 0;
    private List<Report> reportListCopy = new ArrayList<Report>();
    private DataLists reportLists = new DataLists(); // main data structure
    private ShelvingError shelvingError;

    /**
     * Main Function
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DataLists process(List<String> toFind, String finalLocationName,
	    Date scanDate, String oversize) throws IllegalAccessException,
	    InvocationTargetException, IOException, HibernateException,
	    NullFileException {
	logger.debug("Processing data");
	// (bad barcodes != null barcodes ?)
	nullBarcodes = Collections.frequency(toFind, NULL_BARCODE_STRING);
	reportLists = new DataLists();
	shelvingError = new ShelvingError();
	BarcodeSearchDAO itemdao = new BarcodeSearchDAO();
	reportLists.setCatalogAsList(new ArrayList<OrbisRecord>());
	ShelvingErrorPopulator shelvingErrorPopulator = new ShelvingErrorPopulator();
	// enum_culprit_list
	FullComparator fullComparator = new FullComparator();

	try {
	    // call Voyager, put lists (orbis and null) in reportLists
	    List<SearchResult> list = itemdao.findAllById(toFind);
	    reportLists = initCatalogList(immutableList(list));

	    // Filter Items -- e.g. Z9A394, separate Z9 A394
	    reportLists.setCatalogAsList(filterCallnumber(reportLists
		    .getCatalogAsList()));

	    // Create purgedList and catalogSortedPurged
	    List<OrbisRecord> purgedList = new ArrayList<OrbisRecord>(
		    reportLists.getCatalogAsList());

	    // remove null items & add to suppress errors
	    processPurgedList(purgedList);
	    List<OrbisRecord> catalogSortedPurged = new ArrayList<OrbisRecord>(
		    purgedList);
	    Collections.copy(catalogSortedPurged, purgedList);

	    // sorted for mis-shelf?
	    Collections.sort(catalogSortedPurged, fullComparator);
	    fullComparator.getCulprits(); // TODO

	    // set priors, and mis-shelf -- another method also runs for this
	    List<Report> legacyMisshelfs = legacyCalculateMisshelf(purgedList,
		    catalogSortedPurged);
	    
	    
	    // 2 pass approach (ignored for now)
	    /*List<Report> legacyMisshelfs = legacyAddAll(purgedList,
	       catalogSortedPurged);  // populate List<Report> since AccuracyErrorsProcessors adds only m/s to List<Report>
*/	    
	    
	    reportLists.setReportCatalogAsList(legacyMisshelfs); // remove?

	    // set oversize flag
	    setOversizeFlag(reportLists.getReportCatalogAsList(), oversize);

	    // Filter out objects that do NOT have ANY errors
	    List<Report> errorsOnlyList = ReportListFilter.filterReportList(
		    Collections.unmodifiableList(reportLists
			    .getReportCatalogAsList()), finalLocationName,
		    scanDate, oversize);
	    reportLists.setReportCatalogAsList(errorsOnlyList);

	    // UI: raw results:
	    reportLists.setCatalogSortedRaw(new ArrayList(reportLists
		    .getCatalogAsList()));
	    sortCatalogSortedRaw();
	    removeNulls();

	    // UI: Used for printing dialog:
	    reportListCopy = new ArrayList(reportLists.getReportCatalogAsList());

	    // marked list copy for UI -- new Logic
	    reportLists.setMarkedCatalogAsList(new ArrayList(reportLists
		    .getCatalogAsList()));
	    // again, strip out all null barcodes:
	    reportLists.setMarkedCatalogAsList(removeNulls(reportLists
		    .getMarkedCatalogAsList()));

	    // Add * for call nums. that are out of place
	    decorateMarkList(reportLists.getMarkedCatalogAsList());

	    // (Bit problematic: populates culprit list : enum w/ shelving
	    // warnings are left out)
	    // assumes all errors have been added prior
	    // NEW routine.. conflicts with legacy one
	    culpritList = generateMisshelfList(reportLists);

	    // New Add enums, since processMisshelf() doesn't add enum warnings
	    addRemainingToMisshelfCulpritList(culpritList,
		    reportLists.getReportCatalogAsList(),
		    reportLists.getCatalogAsList(), finalLocationName,
		    scanDate, oversize);

	    // Add to culprit list other errors:
	    for (Report item : fullComparator.getCulpritList()) {
		enumWarnings.add(item);
		culpritList.add(item); // add shelving warnings to culpritList
	    }

	    // re-arrange by File Order:
	    culpritList = fixSortOrder(reportLists.getCatalogAsList(),
		    culpritList);
	    reportLists.setCulpritList(culpritList); // ?

	    // Calculate shelving error count
	    shelvingError = shelvingErrorPopulator.populateShelvingError(
		    reportLists.getReportCatalogAsList(), finalLocationName,
		    scanDate, oversize, nullBarcodes);
	    reportLists.setShelvingError(shelvingError); // TODO clean up

	    reportLists.setEnumWarnings(enumWarnings); // ?
	    setEnumWarningsSize(shelvingError, fullComparator.getCulpritList()
		    .size()); // ?
	} catch (HibernateException e1) {
	    LogicHelper.printErrors("Hibernate exception", e1);
	    throw new HibernateException(e1); // delegated to ErrorBean
	} catch (Throwable t) {
	    LogicHelper.printErrors("Generic error", t);
	}
	return reportLists;
    }

    /**
     * Calculate number of null barcodes. Null barcodes are added in another
     * operation though, to keep the file sort order intact!
     * 
     * @param list
     * @param
     * @return
     */
    private int computeNullBarcodes(final List<SearchResult> list) {
	for (SearchResult searchResult : list) {
	    // e.g. for a barcode of legit length, but no result in Orbis
	    if (searchResult.getResult().size() == 0) {
		if (searchResult.getId().contains(NULL_BARCODE_STRING)) {
		    nullBarcodes++;
		}
	    }
	}
	return nullBarcodes;
    }

    /**
     * TODO use new reportLists Populates catalog
     * 
     * @param list
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public DataLists initCatalogList(final List<SearchResult> list)
	    throws InvocationTargetException, IllegalAccessException {
	nullBarcodes = computeNullBarcodes(Collections.unmodifiableList(list));
	if (reportLists.getCatalogAsList() != null) {
	    logger.debug("Pre CatalogInit processing, list catalog size : "
		    + reportLists.getCatalogAsList().size());
	}
	reportLists = CatalogInit.processCatalogList(Collections
		.unmodifiableList(list));
	logger.debug("Post CatalogInit processing, list catalog size : "
		+ reportLists.getCatalogAsList().size());
	return reportLists;
    }

    /**
     * Revert sort order
     * 
     * @param catalogList
     * @param culpritList
     * @return
     */
    private List<Report> fixSortOrder(List<OrbisRecord> catalogList,
	    List<Report> culpritList) {
	logger.debug("Fixing sort order");
	List<Report> naturalOrderList = new ArrayList<Report>();
	for (OrbisRecord orbisItem : catalogList) {
	    Report item = LogicHelper
		    .findFirstItemIndex(culpritList, orbisItem);
	    if (item != null) {
		naturalOrderList.add(item);
	    }
	}
	return naturalOrderList;
    }

    /**
     * process Purged + Suppressed //TODO change signature if not used by JSF
     */
    public void processPurgedList(List<OrbisRecord> purgedList) {
	for (OrbisRecord o : reportLists.getCatalogAsList()) {
	    if (o.getITEM_BARCODE().contains("0000000")) {
		purgedList.remove(o);
	    }

	    if (o.getSUPPRESS_IN_OPAC().trim().equals("Y")) {
		shelvingError.setSuppress_errors(shelvingError
			.getSuppress_errors() + 1);
		reportLists.getSuppressedList().add(o);
		continue;
	    }
	}
    }

    /**
     * ?
     */
    public void sortCatalogSortedRaw() {
	logger.debug("Sorting raw catalog list");
	Collections.copy(reportLists.getCatalogSortedRaw(),
		reportLists.getCatalogAsList());
	Collections.sort(reportLists.getCatalogSortedRaw(),
		new FullComparator());
    }

    /**
     * ?
     */
    public void removeNulls() {
	logger.debug("Cleaning up list. Removing null barcodes");
	for (OrbisRecord o : reportLists.getCatalogAsList()) {
	    if (o.getITEM_BARCODE() == null
		    || o.getITEM_BARCODE().equals(NULL_BARCODE_STRING)) {
		reportLists.getCatalogSortedRaw().remove(o);
	    }
	}
    }

    /**
     * ?
     */
    public List<OrbisRecord> removeNulls(List<OrbisRecord> markedList) {

	logger.debug("Cleaning up list. Removing null barcodes");
	List<OrbisRecord> refList = new ArrayList<OrbisRecord>(markedList);
	Collections.copy(refList, markedList);
	for (OrbisRecord o : refList) {
	    if (o.getITEM_BARCODE() == null
		    || o.getITEM_BARCODE().equals(NULL_BARCODE_STRING)) {
		markedList.remove(o);
	    }
	}
	return markedList;
    }

    /**
     * Adds all catalogSorted objects. Items with accuracy, location errors are
     * filtered out later by ShelfScanEngine.
     * 
     * @param catalogList
     * @param sortedList
     * @param reportCatalogAsList
     * @return
     */
    public List<Report> legacyAddAll(List<OrbisRecord> catalogList,
	    List<OrbisRecord> sortedList) {
	List<Report> reportCatalogAsList = new ArrayList<Report>();
	logger.debug("(LegacyAddAll) Adding all to reportCatalogAsList");

	for (int i = 0; i < catalogList.size(); i++) {
	    String priorDisplay = "N/A";

	    if (catalogList.get(i).getNORMALIZED_CALL_NO() == null) {
		logger.debug("Skipping because normalized call number null for : "
			+ catalogList.get(i).getITEM_BARCODE());
		continue;
	    }

	    // for 1st there's not prior
	    if (i == 0) {
		priorDisplay = "N/A";
		logger.debug("(Legacy) Skipping 1st in sorted list");
		Report item = Report.populateReport(catalogList.get(i), 0,
			"N/A", null, null, null); // hold
		reportCatalogAsList.add(item);
		continue;
	    }

	    if (catalogList.get(i - 1).getDISPLAY_CALL_NO() != null) {
		priorDisplay = catalogList.get(i - 1).getDISPLAY_CALL_NO();
	    }

	    Report item = Report.populateReport(sortedList.get(i), 0, "N/A",
		    priorDisplay, null, null); // hold
	    reportCatalogAsList.add(item);
	}
	return reportCatalogAsList;
    }

    /**
     * Adds all catalogSorted objects. Items with accuracy, location errors are
     * filtered out later by ShelfScanEngine.
     * 
     * @param catalogList
     * @param sortedList
     * @param reportCatalogAsList
     * @return
     */
    public List<Report> legacyCalculateMisshelf(List<OrbisRecord> catalogList,
	    List<OrbisRecord> sortedList) {
	List<Report> reportCatalogAsList = new ArrayList<Report>();
	logger.debug("Report Catalog As List size :"
		+ reportCatalogAsList.size());

	logger.debug("-------------------------------");
	logger.debug("(Legacy) Process by sort order");
	logger.debug("--------------------------------");

	int diff = 0;
	for (int i = 0; i < sortedList.size(); i++) {
	    diff = 0;
	    if (i == 0) {
		logger.debug("(Legacy) Skipping 1st in sorted list");
		continue; // skip 1st
	    }

	    if (anyNull(sortedList.get(i).getNORMALIZED_CALL_NO(), sortedList
		    .get(i - 1).getNORMALIZED_CALL_NO())) {
		logger.debug("Null norm. call num. case for barcode : "
			+ sortedList.get(i).getITEM_BARCODE());
		continue; // bug
	    }

	    /*
	     * String sortItem1 = catalogSorted.get(i).getNORMALIZED_CALL_NO();
	     * String sortItem2 = catalogSorted.get(i -
	     * 1).getNORMALIZED_CALL_NO(); sortItem1 =
	     * sortItem1.replace("( LC )", " "); // TODO replace w/ sortItem2 =
	     * sortItem2.replace("( LC )", " ");
	     */

	    if (catalogList.indexOf(sortedList.get(i - 1)) < catalogList
		    .indexOf(sortedList.get(i))) {
		Report item = Report.populateReport(
			sortedList.get(i),
			0,
			"N/A",
			catalogList.get(
				catalogList.indexOf(sortedList.get(i - 1)))
				.getDISPLAY_CALL_NO(),
			catalogList.get(catalogList.indexOf(sortedList
				.get(i - 1))), sortedList.get(i - 1)); // hold
		reportCatalogAsList.add(item);
		// logger.debug("(Legacy) Added item:" + item.getITEM_BARCODE()
		// + " with diff: " + diff);

	    } else {
		diff = Math.abs(catalogList.indexOf(sortedList.get(i - 1))
			- catalogList.indexOf(sortedList.get(i)));
		Report item = Report.populateReport(
			sortedList.get(i),
			diff,
			"N/A",
			catalogList.get(
				catalogList.indexOf(sortedList.get(i - 1)))
				.getDISPLAY_CALL_NO(),
			catalogList.get(catalogList.indexOf(sortedList
				.get(i - 1))), sortedList.get(i - 1)); // hold
		reportCatalogAsList.add(item);
		logger.debug("(Legacy) Added item:" + item.getITEM_BARCODE()
			+ " with diff: " + diff);
	    }
	}
	return reportCatalogAsList;
    }

    /*
     * Adds * Mark list is used in the main results page tab as well. It
     * compares on Normalized Call Number. Comparing on Display Call Number
     * results in much more errors. // e.g. :
     */

    // TODO replace w/ ListDecorator
    private List<OrbisRecord> decorateMarkList(List<OrbisRecord> catalogList) {
	logger.debug("Decorating list");
	for (int i = 1; i < catalogList.size(); i++) {
	    if (catalogList.get(i).getNORMALIZED_CALL_NO() == null
		    || catalogList.get(i - 1).getNORMALIZED_CALL_NO() == null
		    || catalogList.get(i).getDISPLAY_CALL_NO() == null
		    || catalogList.get(i - 1).getDISPLAY_CALL_NO() == null) {
		continue;
	    }
	    String item1 = catalogList.get(i).getNORMALIZED_CALL_NO();
	    String item2 = catalogList.get(i - 1).getNORMALIZED_CALL_NO();
	    item1 = item1.replace("( LC )", " ");
	    item1 = item1.replace("(LC)", " ");
	    item2 = item2.replace("( LC )", " ");
	    item2 = item2.replace("(LC)", " ");
	    if (item1.trim().compareTo(item2.trim()) < 0) {
		catalogList.get(i).setDISPLAY_CALL_NO(
			ITEM_FLAG_STRING
				+ catalogList.get(i).getDISPLAY_CALL_NO());
	    }
	    // else //skip adding flag
	}
	logger.debug("Done decorating list");
	return null; // TODO fix. use ListDecorator
    }

    public List<OrbisRecord> filterCallnumber(
	    List<OrbisRecord> reportCatalogAsList) {
	// filter obj -- e.g. PQ6613 Z9A394, separate Z9 A394

	for (OrbisRecord o : reportCatalogAsList) {
	    if (anyNull(o.getDISPLAY_CALL_NO(), o.getNORMALIZED_CALL_NO())) {
		continue;
	    }

	    // TODO doesn't count Z9 instances
	    if (o.getDISPLAY_CALL_NO().contains("Z9")) {
		String[] str = o.getDISPLAY_CALL_NO().split("Z9");
		if (str[1].matches("^[^\\d].*")) {
		    o.setDISPLAY_CALL_NO(o.getDISPLAY_CALL_NO().replace("Z9",
			    "Z9 "));
		    o.setNORMALIZED_CALL_NO(o.getNORMALIZED_CALL_NO().replace(
			    "Z9", "Z9 "));
		}
	    }
	}
	return reportCatalogAsList;
    }

    /**
     * Sets oversize
     */
    private void setOversizeFlag(final List<Report> itemList,
	    final String oversize) {
	List<Report> filteredList = new ArrayList<Report>(itemList);
	for (Report item : itemList) {
	    try {
		if (item.getNORMALIZED_CALL_NO() == null
			|| item.getDISPLAY_CALL_NO() == null
			|| item.getLOCATION_NAME() == null
			|| item.getITEM_STATUS_DESC() == null
			|| item.getSUPPRESS_IN_OPAC() == null) {
		    logger.debug("at least one field null for: "
			    + item.getITEM_BARCODE());
		}

		if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode")) {
		    continue;
		}

		boolean oversizeCallNumber = (item.getDISPLAY_CALL_NO()
			.contains("+") || item.getDISPLAY_CALL_NO().contains(
			"Oversize")) ? true : false;
		if (oversize.equalsIgnoreCase("N")) {
		    if (oversizeCallNumber) {
			item.setOVERSIZE("Y"); // used?
		    }
		} else if (oversize.equalsIgnoreCase("Y")) {
		    if (oversizeCallNumber) {
			item.setOVERSIZE("Y"); // NOT AN ERROR
		    } else {
			item.setOVERSIZE("N");
		    }
		}
	    } catch (Exception e) {
		logger.debug("Exception setting oversize flag for item : "
			+ item.getITEM_BARCODE());
		e.printStackTrace();
		continue; // ?
	    }
	}
	// logger.debug("Done filtering barcodes");
    }

    /**
     * TODO clean up so it uses Rules.isItemError. And BSSE uses above method
     * add item_size
     * 
     * Filter list -- if no errors are found, the item is not displayed in the
     * final report
     * 
     * TODO scan date? TODO check/report nulls
     * 
     * @param itemList
     *            ArrayList<Report> of report entries that are displayed on the
     *            final report
     * @param finalLocationName
     *            location entered by end user when running the report
     * @param scanDate
     *            scan date entered by end user
     * @param oversize
     *            user specification of the material (options: y, intermixed, n)
     * @return List<Report> filtered list
     */
    private List<Report> filterReportList(List<Report> itemList,
	    String finalLocationName, Date scanDate, String oversize) {
	logger.debug("Filtering out barcodes that do not have any errors");
	List<Report> filteredList = new ArrayList<Report>(itemList);
	boolean foundError = false;

	for (Report item : itemList) {
	    foundError = false;
	    try {
		if (item.getNORMALIZED_CALL_NO() == null
			|| item.getDISPLAY_CALL_NO() == null
			|| item.getLOCATION_NAME() == null
			|| item.getITEM_STATUS_DESC() == null
			|| item.getSUPPRESS_IN_OPAC() == null) {
		    logger.debug("at least one field null for: "
			    + item.getITEM_BARCODE());
		}

		if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode")) {
		    continue;
		}

		boolean oversizeCallNumber = (item.getDISPLAY_CALL_NO()
			.contains("+") || item.getDISPLAY_CALL_NO().contains(
			"Oversize")) ? true : false;

		if (oversize.equalsIgnoreCase("N")) {
		    if (oversizeCallNumber) {
			item.setOVERSIZE("Y"); // used?
			foundError = true;
		    }
		} else if (oversize.equalsIgnoreCase("Y")) {
		    if (oversizeCallNumber) {
			item.setOVERSIZE("Y"); // NOT AN ERROR
		    } else {
			item.setOVERSIZE("N");
			foundError = true;
		    }
		}

		if (item.getText() != 0) {
		    foundError = true;
		}

		if (!item.getLOCATION_NAME().equals(finalLocationName)) {
		    foundError = true;
		}

		if (item.getITEM_STATUS_DESC().equals("Not Charged")
			|| item.getITEM_STATUS_DESC().equals("Discharged")) {
		    if (item.getITEM_STATUS_DATE() != null
			    && scanDate.before(item.getITEM_STATUS_DATE())) {
			foundError = true;
		    }
		} else {
		    // System.out.print("Suspicious:" + r.getITEM_BARCODE());
		    foundError = true;
		}

		if (item.getSUPPRESS_IN_OPAC().equalsIgnoreCase("Y")) {
		    foundError = true;
		}

		if (foundError == false) {
		    filteredList.remove(item); // remove if no error was found!
		}
	    } catch (Exception e) {
		logger.debug("Exception filtering barcodes");
		e.printStackTrace();
		continue; // ?
	    }
	}
	// logger.debug("Done filtering barcodes");
	return filteredList;
    }

    /**
     * Call AccuracyErrorsProcessor: mishelf Uses Lauren's logic to compute item
     * shelving errors
     */
    public List<Report> generateMisshelfList(final DataLists dataList) {
	return AccuracyErrorsProcessor.processMisshelfs(dataList);
    }

    /**
     * TODO Separate class. Adds non accuracy errors if there are other errors.
     * 
     * @param culpritList
     * @param reportCatalogAsList
     */
    public void addRemainingToMisshelfCulpritList(List<Report> culpritList,
	    final List<Report> reportCatalogAsList,
	    List<OrbisRecord> orbisList, String finalLocationName,
	    Date scanDate, String oversize) {
	logger.debug("*Adding Non Acc items to culprit list");
	logger.debug("Current culprit list : " + culpritList.toString());
	for (Report item : reportCatalogAsList) {
	    // logger.debug("Considering:" + item.getITEM_BARCODE() +
	    // " Text flag : " + item.getText());
	    // ? why only one added? Is this to avoid adding an item twice?
	    /*
	     * if (item.getText() == 0) {
	     * item.setDISPLAY_CALL_NO(item.getDISPLAY_CALL_NO());
	     * culpritList.add(item); }
	     */
	    if (culpritList.contains(item)) {
		logger.debug("Skipping : " + item.getITEM_BARCODE()
			+ " (Already contains).");
	    } else if (Rules.isVoyagerError(item, finalLocationName, scanDate,
		    oversize)) // necessary because of legacyMisshelf()
	    {
		OrbisRecord prior = LogicHelper.priorPhysical(orbisList,
			item.getITEM_BARCODE());
		if (prior != null) {
		    if (prior.getDISPLAY_CALL_NO() != null)
			item.setPriorPhysical(prior.getDISPLAY_CALL_NO());
		    if (prior.getITEM_ENUM() != null)
			item.setPriorPhysicalEnum(prior.getITEM_ENUM());
		    if (prior.getCHRON() != null)
			item.setPriorEnum(prior.getCHRON());
		} else {
		    // prior null. e.g. for the 1st item on report
		    item.setPriorPhysical("N/A");
		    item.setPriorPhysicalEnum("N/A");
		    item.setPriorPhysicalChron("N/A");
		}
		culpritList.add(item);
	    } else {
		logger.debug("Skipping : " + item.getITEM_BARCODE());
	    }
	}
    }

    public List<OrbisRecord> getBadBarcodes() {
	return badBarcodes;
    }

    public List<Report> getCulpritList() {
	return culpritList;
    }

    public int getNullBarcodes() {
	return nullBarcodes;
    }

    public List<Report> getReportListCopy() {
	return reportListCopy;
    }

    public DataLists getReportLists() {
	return reportLists;
    }

    public ShelvingError getShelvingError() {
	return shelvingError;
    }

    public void setBadBarcodes(List<OrbisRecord> badBarcodes) {
	this.badBarcodes = badBarcodes;
    }

    public void setCulpritList(List<Report> culpritList) {
	this.culpritList = culpritList;
    }

    public void setNullBarcodes(int nullBarcodes) {
	this.nullBarcodes = nullBarcodes;
    }

    public void setReportListCopy(List<Report> reportListCopy) {
	this.reportListCopy = reportListCopy;
    }

    public void setReportLists(DataLists reportLists) {
	this.reportLists = reportLists;
    }

    public void setShelvingError(ShelvingError shelvingError) {
	this.shelvingError = shelvingError;
    }

    /**
     * Used by JSF?
     * 
     * @return
     */
    public List<Report> getEnumWarnings() {
	return enumWarnings;
    }

    /**
     * Used by JSF?
     * 
     * @param enumWarnings
     */
    public void setEnumWarnings(List<Report> enumWarnings) {
	this.enumWarnings = enumWarnings;
    }

    private void setEnumWarningsSize(ShelvingError shelvingError, int size) {
	shelvingError.setEnum_warnings(size);
    }

    public <T> List<T> immutableList(List<? extends T> list) {
	return Collections.unmodifiableList(list);
    }

    public static boolean anyNull(String str, String str2) {
	return (str == null || str2 == null) ? true : false;
    }

    /**
     * TODO Separate class
     * 
     * (Potentially) buggy --in the case 2 items causing a single misshelf, only
     * one is reported. and hence any other errors for the item are missed.
     * 
     * @param culpritList
     * @param reportCatalogAsList
     */
    @Deprecated
    public void addNonAccErrorsToCulpritList(List<Report> culpritList,
	    final List<Report> reportCatalogAsList) {
	logger.debug("Adding Non Acc items to culprit list");
	logger.debug("Current culprit list : " + culpritList.toString());
	for (Report item : reportCatalogAsList) {
	    logger.debug("Considering:" + item.getITEM_BARCODE()
		    + " Text flag : " + item.getText());
	    // ? why only one added? Is this to avoid adding an item twice?
	    if (item.getText() == 0) {
		item.setDISPLAY_CALL_NO(item.getDISPLAY_CALL_NO());
		culpritList.add(item);
	    }
	}
    }

    /**
     * find Date TODO -- used by JSF???
     */

    @Deprecated
    public Date findMaxItemStatusDate(List<OrbisRecord> itemList, String barcode) {
	for (OrbisRecord item : itemList) {
	    // assuming there's only one;

	    if (item.getITEM_BARCODE().equals(barcode)) {
		if (item.getITEM_STATUS_DATE() != null
			&& item.getITEM_STATUS_DATE().toString().length() > 1) {
		    return item.getITEM_STATUS_DATE();
		}
	    }
	}
	return null;
    }

    public BasicShelfScanEngine() {
	super();
    }

}
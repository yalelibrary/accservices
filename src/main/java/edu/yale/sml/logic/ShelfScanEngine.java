package edu.yale.sml.logic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;

import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.Report;
import edu.yale.sml.model.ShelvingError;
import edu.yale.sml.view.NullFileException;


public interface ShelfScanEngine {

	List<OrbisRecord> getBadBarcodes();

	int getNullBarcodes();

	List<Report> getReportListCopy();

	DataLists getReportLists();

	ShelvingError getShelvingError();

	DataLists process(List<String> toFind,
			String finalLocationName, Date scanDate, String oversize)
			throws IllegalAccessException, InvocationTargetException,
			IOException, HibernateException, NullFileException;

	void setBadBarcodes(List<OrbisRecord> badBarcodes);

	void setNullBarcodes(int nullBarcodes);

	void setReportListCopy(List<Report> reportListCopy);

	void setReportLists(DataLists reportLists);

	void setShelvingError(ShelvingError shelvingError);

}
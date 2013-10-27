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

	public abstract List<OrbisRecord> getBadBarcodes();

	public abstract int getNullBarcodes();

	public abstract List<Report> getReportListCopy();

	public abstract DataLists getReportLists();

	public abstract ShelvingError getShelvingError();

	public abstract DataLists process(List<String> toFind,
			String finalLocationName, Date scanDate, String oversize)
			throws IllegalAccessException, InvocationTargetException,
			IOException, HibernateException, NullFileException;

	public abstract void setBadBarcodes(List<OrbisRecord> badBarcodes);

	public abstract void setNullBarcodes(int nullBarcodes);

	public abstract void setReportListCopy(List<Report> reportListCopy);

	public abstract void setReportLists(DataLists reportLists);

	public abstract void setShelvingError(ShelvingError shelvingError);

}
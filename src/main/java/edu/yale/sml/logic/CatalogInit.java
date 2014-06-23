package edu.yale.sml.logic;

import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.*;


public class CatalogInit {

    final static Logger logger = LoggerFactory.getLogger(CatalogInit.class);

    private static final String NULL_BARCODE_STRING = "00000000";

    /**
     * Used by "BasicShelfScanEngine"
     *
     * @param list representing orbis record search results.
     * @return report Container list; older status items and null barcodes are ignored.
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static DataLists processCatalogList(final List<SearchResult> list)
            throws InvocationTargetException, IllegalAccessException {
        logger.debug("Populating DataLists from Orbis search results.");

        final List<String> barocodesAdded = new ArrayList<String>();
        final List<OrbisRecord> badBarcodes = new ArrayList<OrbisRecord>();
        final DataLists dataLists = new DataLists();
        final Multimap<String, String> barcodeStatuses = ArrayListMultimap.create();

        for (SearchResult searchResult : list) {

            // e.g. for a barcode of legit length, but no result in Orbis
            if (searchResult.getResult().isEmpty()) {
                OrbisRecord catalogObj = getBadBarcodeOrbisRecord(searchResult.getId());
                badBarcodes.add(catalogObj);
                continue; // skip full object populating
            }

            for (Map<String, Object> m : searchResult.getResult()) {
                OrbisRecord o = new OrbisRecord();
                java.sql.Date date = null;
                Converter dc = new DateConverter(date);
                ConvertUtils.register(dc, java.sql.Date.class);
                BeanUtils.populate(o, m);

                if (o.getITEM_STATUS_DESC() != null) {
                    barcodeStatuses.put(searchResult.getId(), o.ITEM_STATUS_DESC());
                }

                if (o.getCallNoType() == null) {
                    logger.debug("CN TYPE null for :" + o.getITEM_BARCODE());
                }

                if (o.getITEM_STATUS_DESC() == null && o.getITEM_STATUS_DATE() == null
                        && (o.getNORMALIZED_CALL_NO() == null)
                        || o.getDISPLAY_CALL_NO() == null) {
                    logger.debug("Ignoring completely null record" + o.BARCODE());
                    continue;
                }

                if (o.getITEM_STATUS_DESC() == null) {
                    logger.trace("ITEM_STATUS_DESC null for:" + o.getITEM_BARCODE());
                    continue;
                }

                // check if valid item status. This may cause duplicate entries:
                if (Rules.isValidItemStatus(o.getITEM_STATUS_DESC())) {

                    // not sure how useful it is because valid items seem to
                    // fetch only one row from Orbis (unlike invalid)
                    if (barocodesAdded.contains(o.getITEM_BARCODE())
                            && !o.getITEM_BARCODE().contains(NULL_BARCODE_STRING)) {
                        logger.trace("List already contains valid status item for this item: "+ o.getItemBarcode());
                        // check if repeat takes care of prior
                        o.setDISPLAY_CALL_NO(o.getDISPLAY_CALL_NO() + " REPEAT ");
                        dataLists.getCatalogAsList().add(o);
                    } else {
                        logger.trace("List does NOT contain valid item status:" + o.getItemBarcode());
                        dataLists.getCatalogAsList().add(o);
                        barocodesAdded.add(o.getITEM_BARCODE());
                    }
                }
                // e.g. for barcode with Status 'Hold Status'
                else {                 // if not valid item status
                    logger.trace("Considering invalid status item :" + o.getITEM_BARCODE() + " ? ");
                    printStatuses(o);

                    if (barocodesAdded.contains(o.getITEM_BARCODE()) == false) {
                        logger.trace("Adding item (w/ invalid status) . The list does NOT contains barcode : "
                                + o.getITEM_BARCODE());
                        dataLists.getCatalogAsList().add(o);
                        barocodesAdded.add(o.getITEM_BARCODE());
                    } else if (barocodesAdded.contains(o.getITEM_BARCODE())) {
                        logger.trace("List already contains this item : " + o.getITEM_BARCODE());
                        Date existingItemStatusDate = null;
                        OrbisRecord outdatedObject = findOlderItemStatusDateObject(dataLists.getCatalogAsList(), o.getITEM_BARCODE());
                        if (outdatedObject != null) {
                            existingItemStatusDate = outdatedObject.getITEM_STATUS_DATE();
                        } else {
                            logger.trace("Outdated object null!");
                        }

                        if (o.getITEM_STATUS_DATE() != null
                                && outdatedObject != null
                                && o.getITEM_STATUS_DATE().compareTo(existingItemStatusDate) > 0) {
                            logger.trace("Item (w/ invalid status) has more recent date:" + o.getItemBarcode() + ", so it's replacing the older entity");
                            dataLists.getCatalogAsList().remove(outdatedObject);
                            dataLists.getCatalogAsList().add(o);
                        } else {
                            logger.trace("Item (w/ invalid status) doesn't have more recent status." + o.getITEM_BARCODE());
                            logger.trace("Checking if the other item (in the list is valid though?");

                            if (outdatedObject != null && outdatedObject.getITEM_STATUS_DESC() != null && Rules.isValidItemStatus(outdatedObject.getITEM_STATUS_DESC())) {
                                logger.trace("Confirmed that current list already contains a valid item");
                                logger.trace("Discarding valid with invalid");

                                dataLists.getCatalogAsList().remove(outdatedObject);
                                dataLists.getCatalogAsList().add(o);
                            } else {
                                logger.trace("Nope. Either existing item also invalid status OR it's status desc is null");
                            }
                        }

                        // e.g. Missing 5-5-55 vs 'Not Charged' with status date
                        // wont' get here if item_status_desc for existing item
                        // is not null:

                        //IF outdated object IS NULL

                        if (o.getITEM_STATUS_DATE() != null && outdatedObject == null) {
                            OrbisRecord priorWithNullDate = findOlderItemStatusDesc(dataLists.getCatalogAsList(), o.getITEM_BARCODE());

                            if (priorWithNullDate != null) {
                                logger.trace("Adding more recent invalid, and discarding older valid or invalid w/ null status date!");
                                dataLists.getCatalogAsList().remove(priorWithNullDate);
                                dataLists.getCatalogAsList().add(o);
                            } else {
                                logger.trace("Not sure what to do with item : " + o.getITEM_BARCODE());
                            }
                        } else {
                            logger.trace("Item " + o.getITEM_BARCODE() + " status date null or outdated object NOT null");

                        }
                    }
                }
            }
        }
        dataLists.setNullResultBarcodes(badBarcodes);
        dataLists.setBarcodesAsMap(barcodeStatuses);
        return dataLists;
    }

    private static OrbisRecord findOlderItemStatusDesc(List<OrbisRecord> catalogAsList, String item_BARCODE) {
        for (OrbisRecord o : catalogAsList) {         // assuming there's only one;
            if (o.getITEM_BARCODE().equals(item_BARCODE)) {
                if (o.getITEM_STATUS_DESC() != null
                        && o.getITEM_STATUS_DESC().toString().length() > 1) {
                    return o;
                }
            }
        }
        return null;
    }

    private static OrbisRecord findOlderItemStatusDateObject(List<OrbisRecord> catalogAsList, String item_barcode) {
        for (OrbisRecord o : catalogAsList) { // assuming there's only one;
            if (o.getITEM_BARCODE().equals(item_barcode)) {
                if (o.getITEM_STATUS_DATE() != null
                        && o.getITEM_STATUS_DATE().toString().length() > 1) {
                    return o;
                }
            }
        }
        return null;
    }

    /**
     * Prints status desc and date
     *
     * @param item
     */
    private static void printStatuses(final OrbisRecord item) {
        StringBuffer sb = new StringBuffer();
        sb.append(item.getITEM_BARCODE());

        if (item.getITEM_STATUS_DESC() == null) {
        } else {
            sb.append(" status_desc: " + item.getITEM_STATUS_DESC());
        }

        if (item.getITEM_STATUS_DATE() == null) {
            sb.append(" , Null status date");
        } else {
            sb.append(" ,status_date : " + item.getITEM_STATUS_DATE());
        }
        logger.debug(sb.toString());
    }

    private static OrbisRecord getBadBarcodeOrbisRecord(final String id) {
        final OrbisRecord o = new OrbisRecord();
        o.setITEM_BARCODE(id);
        o.setDISPLAY_CALL_NO("Bad Barcode");
        o.setNORMALIZED_CALL_NO("Bad Barcode");
        o.setSUPPRESS_IN_OPAC("N/A");
        return o;
    }

}

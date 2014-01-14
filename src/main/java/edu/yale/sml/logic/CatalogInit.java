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
     * @param list representing orbis record serach results.
     * @return report Container list; older status items and null barcodes are ignored.
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static DataLists processCatalogList(List<SearchResult> list) throws InvocationTargetException,
            IllegalAccessException {
        logger.debug("Populating DataLists from Orbis search results.");
        List<String> barocodesAdded = new ArrayList<String>();
        List<OrbisRecord> badBarcodes = new ArrayList<OrbisRecord>();
        DataLists dataLists = new DataLists();

        Multimap<String, String> barcodeStatuses = ArrayListMultimap.create();


        for (SearchResult searchResult : list) {
            // e.g. for a barcode of legit length, but no result in Orbis


            if (searchResult.getResult().size() == 0) {
                OrbisRecord catalogObj = new OrbisRecord();
                catalogObj.setITEM_BARCODE(searchResult.getId());
                catalogObj.setDISPLAY_CALL_NO("Bad Barcode");
                catalogObj.setNORMALIZED_CALL_NO("Bad Barcode");
                catalogObj.setSUPPRESS_IN_OPAC("N/A");
                badBarcodes.add(catalogObj);

                continue; // skip full object populating
            }

            for (Map<String, Object> m : searchResult.getResult()) {
                OrbisRecord catalogObj = new OrbisRecord();
                java.sql.Date date = null;
                Converter dc = new DateConverter(date);
                ConvertUtils.register(dc, java.sql.Date.class);
                BeanUtils.populate(catalogObj, m);

                // logger.debug("Added:" + catalogObj.getITEM_BARCODE());

                //used for testing:
                if (catalogObj.getITEM_STATUS_DESC() != null)
                    barcodeStatuses.put(searchResult.getId(), catalogObj.ITEM_STATUS_DESC());


                // Not sure what to do if CN Type null
                if (catalogObj.getCALL_NO_TYPE() == null) {
                    logger.debug("CN TYPE null for :" + catalogObj.getITEM_BARCODE());
                }

                if (catalogObj.getITEM_STATUS_DESC() == null
                        && catalogObj.getITEM_STATUS_DATE() == null
                        && (catalogObj.getNORMALIZED_CALL_NO() == null)
                        || catalogObj.getDISPLAY_CALL_NO() == null) {
                    logger.debug("Ignoring completely null record for Lauen"
                            + catalogObj.BARCODE());
                    continue;
                }

                if (catalogObj.getITEM_STATUS_DESC() == null) {
                    logger.debug("ITEM_STATUS_DESC null for:" + catalogObj.getITEM_BARCODE());
                    continue;
                }

                // check if valid item status. This may cause duplicate entries:
                if (Rules.isValidItemStatus(catalogObj.getITEM_STATUS_DESC())) {

                    // not sure how useful it is because valid items seem to
                    // fetch only one row from Orbis (unlike invalid)

                    if (barocodesAdded.contains(catalogObj.getITEM_BARCODE())
                            && !catalogObj.getITEM_BARCODE().contains(NULL_BARCODE_STRING)) {
                        logger.debug("List already contains valid status item for this item: "
                                + catalogObj.getITEM_BARCODE());
                        // check if repeat takes care of prior
                        catalogObj.setDISPLAY_CALL_NO(catalogObj.getDISPLAY_CALL_NO() + " REPEAT ");
                        dataLists.getCatalogAsList().add(catalogObj);

                    } else {
                        logger.debug("List does NOT contain valid item status:" + catalogObj.getITEM_BARCODE());
                        dataLists.getCatalogAsList().add(catalogObj);
                        barocodesAdded.add(catalogObj.getITEM_BARCODE());
                    }
                }
                // e.g. for barcode with Status 'Hold Status'
                else
                // if not valid item status
                {
                    logger.debug("Considering invalid status item :" + catalogObj.getITEM_BARCODE() + " ? ");

                    printStatuses(catalogObj);

                    if (barocodesAdded.contains(catalogObj.getITEM_BARCODE()) == false) {
                        logger.debug("Adding item (w/ invalid status) . The list does NOT contains barcode : "
                                + catalogObj.getITEM_BARCODE());
                        dataLists.getCatalogAsList().add(catalogObj);
                        barocodesAdded.add(catalogObj.getITEM_BARCODE());
                    } else if (barocodesAdded.contains(catalogObj.getITEM_BARCODE())) {
                        logger.debug("List already contains this item : " + catalogObj.getITEM_BARCODE());
                        Date existingItemStatusDate = null;
                        OrbisRecord outdatedObject = findOlderItemStatusDateObject(
                                dataLists.getCatalogAsList(), catalogObj.getITEM_BARCODE());
                        if (outdatedObject != null) {
                            existingItemStatusDate = outdatedObject.getITEM_STATUS_DATE();
                        } else {
                            logger.debug("Outdated object null!");
                        }

                        if (catalogObj.getITEM_STATUS_DATE() != null
                                && outdatedObject != null
                                && catalogObj.getITEM_STATUS_DATE().compareTo(
                                existingItemStatusDate) > 0) {
                            logger.debug("Item (w/ invalid status) has more recent date:"
                                    + catalogObj.getITEM_BARCODE()
                                    + ", so it's replacing the older enttity");
                            dataLists.getCatalogAsList().remove(outdatedObject);
                            dataLists.getCatalogAsList().add(catalogObj);
                        } else {
                            logger.debug("Item (w/ invalid status) doesn't have more recent status." + catalogObj.getITEM_BARCODE());
                            logger.debug("Checking if the other item (in the list is valid though?");
                            if (outdatedObject != null && outdatedObject.getITEM_STATUS_DESC() != null && Rules.isValidItemStatus(outdatedObject.getITEM_STATUS_DESC())) {
                                logger.debug("Confirmed that current list already contains a valid item");
                                logger.debug("Discarding valid with invalid");
                                dataLists.getCatalogAsList().remove(outdatedObject);
                                dataLists.getCatalogAsList().add(catalogObj);
                            } else {
                                logger.debug("Nope. Either existing item also invalid status OR it's status desc is null");
                            }


                        }

                        // e.g. Missing 5-5-55 vs 'Not Charged' with status date
                        // wont' get here if item_status_desc for existing item
                        // is not null:

                        //IF outdated object IS NULL

                        if (catalogObj.getITEM_STATUS_DATE() != null && outdatedObject == null) {
                            OrbisRecord priorWithNullDate = findOlderItemStatusDesc(
                                    dataLists.getCatalogAsList(), catalogObj.getITEM_BARCODE());

                            if (priorWithNullDate != null) // &&
                            // Rules.isValidItemStatus(priorWithNullDate.getITEM_STATUS_DESC()))
                            {
                                logger.debug("Adding more recent invalid, and discarding older valid or invalid w/ null status date!");
                                dataLists.getCatalogAsList().remove(priorWithNullDate);
                                dataLists.getCatalogAsList().add(catalogObj);
                            } else {
                                logger.debug("Not sure what to do with item : "
                                        + catalogObj.getITEM_BARCODE());
                            }
                        } else {
                            logger.debug("Item " + catalogObj.getITEM_BARCODE() + " status date null or outdated object NOT null");

                        }
                    }
                }
            }
        }
        dataLists.setNullResultBarcodes(badBarcodes);
        dataLists.setBarcodesAsMap(barcodeStatuses);
        return dataLists; //done!
    }

    /**
     * helper method
     *
     * @param catalogAsList
     * @param item_BARCODE
     * @return
     */
    private static OrbisRecord findOlderItemStatusDesc(List<OrbisRecord> catalogAsList, String item_BARCODE) {
        // assuming there's only one;
        for (OrbisRecord o : catalogAsList) {
            if (o.getITEM_BARCODE().equals(item_BARCODE)) {
                if (o.getITEM_STATUS_DESC() != null
                        && o.getITEM_STATUS_DESC().toString().length() > 1) {
                    return o;
                }
            }
        }
        return null;
    }

    /**
     * helper method
     *
     * @param catalogAsList
     * @param item_BARCODE
     * @return
     */
    private static OrbisRecord findOlderItemStatusDateObject(List<OrbisRecord> catalogAsList,
                                                             String item_BARCODE) {
        // assuming there's only one;
        for (OrbisRecord o : catalogAsList) {
            if (o.getITEM_BARCODE().equals(item_BARCODE)) {
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


}

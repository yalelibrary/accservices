package edu.yale.sml.logic;

import edu.yale.sml.model.OrbisRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HOLD
 *
 */
public class ListDecorator {

    final static Logger logger = LoggerFactory.getLogger(ListDecorator.class);
    private final static String LC_STRING = "( LC )";
    private final static String LC_2_STRING = "(LC)"; // get list from lauren to replace w/ regex

    /*
     * Adds *.  Mark list is used in the main results page tab as well. It
     * compares on Normalized Call Number. Comparing on Display Call Number
     * results in much more errors. // e.g. :
     */

    /**
     * Adds * if sort order is messed up
     *
     * @param catalogList
     * @return
     */

    // TODO replace LC logic w/ filter
    public static List<OrbisRecord> decorateList(final List<OrbisRecord> catalogList) {
        logger.debug("Decorating list");
        List<OrbisRecord> itemList = new ArrayList<OrbisRecord>();
        Collections.copy(itemList, catalogList);

        //note loop starts with 1 (no prior for item 0)
        for (int i = 1; i < itemList.size(); i++) {
            OrbisRecord item = itemList.get(i);
            OrbisRecord previousItem = itemList.get(i - 1);
            // add to itemList
            if (anyNull(item.getNORMALIZED_CALL_NO(), item.getDISPLAY_CALL_NO(),
                    previousItem.getNORMALIZED_CALL_NO(), previousItem.getDISPLAY_CALL_NO())) {
                logger.debug("Skipping decorating item (" + item.getITEM_BARCODE() + "). Normalized or " +
                        "display call num of this or previous item null");
                continue;
            }

            String itemCallNum = itemList.get(i).getNORMALIZED_CALL_NO();
            String previousItemCallNum = itemList.get(i - 1).getNORMALIZED_CALL_NO();
            itemCallNum = itemCallNum.replace(LC_STRING, " ");
            itemCallNum = itemCallNum.replace(LC_2_STRING, " ");
            previousItemCallNum = previousItemCallNum.replace(LC_STRING, " ");
            previousItemCallNum = previousItemCallNum.replace(LC_2_STRING, " ");

            if (itemCallNum.trim().compareTo(previousItemCallNum.trim()) < 0) {
                //catalogList.get(i).setDISPLAY_CALL_NO(
                //        BasicShelfScanEngine.ITEM_FLAG_STRING + catalogList.get(i).getDISPLAY_CALL_NO());
                itemList.get(i).setDISPLAY_CALL_NO(
                        BasicShelfScanEngine.ITEM_FLAG_STRING + itemList.get(i).getDISPLAY_CALL_NO());
            }
        }
        return itemList;
    }

    public static boolean anyNull(String str, String str2, String str3, String str4) {
        return (str == null || str2 == null || str3 == null || str4 == null) ? true : false;
    }
}

package edu.yale.sml.logic;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.yale.sml.logic.CatalogInit;
import org.junit.Test;
import org.slf4j.*;

import java.lang.reflect.InvocationTargetException;

public class CatalogInitTest {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void shouldProcess() {

        try {
            CatalogInit.processCatalogList(Collections.emptyList());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}

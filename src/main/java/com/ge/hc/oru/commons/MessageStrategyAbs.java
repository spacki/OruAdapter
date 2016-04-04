package com.ge.hc.oru.commons;

import java.util.HashMap;

/**
 * Created by 100026806 on 2/26/14.
 */
public abstract class MessageStrategyAbs {

    /**
     * Abstract method for obtaining the content
     * of incoming message as String. Concreted classes
     * provide the implementation of how to extract the data
     * @return
     */
    public abstract String getDocContent();


    /**
     * Abstract method for obtaining the content
     * of incoming metadata as HashMap. Concreted classes
     * provide the implementation of how to extract the data
     * @return
     */

    public abstract HashMap getMetaInput();
}

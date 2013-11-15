package edu.yale.sml.util.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

public class HTMLayout extends LayoutBase<ILoggingEvent>
{
    public String doLayout(ILoggingEvent event)
    {
        StringBuffer sbuf = new StringBuffer(128);
        sbuf.append("<div>");
        sbuf.append(event.getTimeStamp() -  event.getLoggerContextVO().getBirthTime());
        sbuf.append(" ");
        sbuf.append(event.getLevel());
        sbuf.append(event.getLoggerName());
        sbuf.append(" - ");
        sbuf.append(event.getFormattedMessage());
        sbuf.append("<br>");
        sbuf.append("</div>");
        return sbuf.toString();
    }
}
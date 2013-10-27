package edu.yale.sml.util.logging;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.sift.DefaultDiscriminator;
import ch.qos.logback.core.sift.Discriminator;

@Deprecated
public class JSFAppender<E> extends AppenderBase<ILoggingEvent>
{
    static int DEFAULT_LIMIT = 16;

    public static void clearData()
    {
        uniqueData.remove();
    }

    public static StringBuffer getMyLogs()
    {
        return uniqueData.get();
    }

    public static ThreadLocal<StringBuffer> getUniquedata()
    {
        return uniqueData;
    }

    int counter = 0; // ?

    int limit = DEFAULT_LIMIT;
    Layout layout;

    PatternLayoutEncoder encoder; // ?

    Discriminator<E> discriminator = new DefaultDiscriminator<E>();

    EventEvaluator<E> eventEvaluator;

    private static final ThreadLocal<StringBuffer> uniqueData = new ThreadLocal<StringBuffer>()
    {
        @Override
        protected StringBuffer initialValue()
        {
            return new StringBuffer();
        }
    };

    public JSFAppender()
    {
    }

    @Override
    protected void append(ILoggingEvent event)
    {
        if (counter >= limit)
        {
            return;
        }
        try
        {
            if (!eventEvaluator.evaluate((E) event))
            {
                return;
            }
        }
        catch (NullPointerException e)
        {
            return;
        }
        catch (EvaluationException e)
        {
            return;
        }

        uniqueData.set(new StringBuffer(uniqueData.get() + this.layout.doLayout(event)));
        counter++;
    }

    public EventEvaluator<E> getEventEvaluator()
    {
        return eventEvaluator;
    }

    public int getLimit()
    {
        return limit;
    }

    public void setEvaluator(EventEvaluator<E> eventEvaluator)
    {
        this.eventEvaluator = eventEvaluator;
    }

    public void setEventEvaluator(EventEvaluator<E> eventEvaluator)
    {
        this.eventEvaluator = eventEvaluator;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    @Override
    public void start()
    {
        layout = new HTMLayout(); // TODO Where is this set? In .xml or here?

        if (this.layout == null)
        {
            addError("No layout set for the appender named :  [" + name + "].");
            return;
        }
        super.start();
    }

}
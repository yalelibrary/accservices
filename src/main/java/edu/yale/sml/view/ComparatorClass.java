package edu.yale.sml.view;

import edu.yale.sml.model.OrbisRecord;

@Deprecated
public class ComparatorClass<O> implements Comparable<OrbisRecord>
{

    public ComparatorClass()
    {
        super();
    }

    public int compare(OrbisRecord o1, OrbisRecord o2)
    {
        int diff = 0, enum_diff = 0, year_diff = 0;
        diff = o1.getNORMALIZED_CALL_NO().compareTo(o2.getNORMALIZED_CALL_NO());

        if (diff == 0)
        {
            if (o1.getITEM_ENUM() != null && o2.getITEM_ENUM() != null)
            {
                if (o1.getITEM_ENUM().length() == o2.getITEM_ENUM().length())
                {
                    enum_diff += o1.getITEM_ENUM().compareTo(o2.getITEM_ENUM());
                }
                else
                {
                    enum_diff += o2.getITEM_ENUM().compareTo(o1.getITEM_ENUM());
                }
                if (enum_diff == 0)
                {
                    year_diff = o1.getYEAR().compareTo(o2.getYEAR());
                }
            }
        }
        return diff + enum_diff + year_diff;
    }

    public int compareTo(OrbisRecord arg0)
    {
        return 0;
    }
}
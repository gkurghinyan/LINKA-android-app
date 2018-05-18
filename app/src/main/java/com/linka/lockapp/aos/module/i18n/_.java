package com.linka.lockapp.aos.module.i18n;

import com.linka.lockapp.aos.AppDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cpuser on 4/2/15.
 */
public class _ {
    public static String i(String... message)
    {
        String msg = "";
        for (int i = 0; i < message.length; i++)
        {
            if (i > 0)
            {
                msg += " ";
            }
            msg += message[i];
        }
        return msg;
    }

    public static String i(int... message)
    {
        String msg = "";
        for (int i = 0; i < message.length; i++)
        {
            if (i > 0)
            {
                msg += " ";
            }

            msg += AppDelegate.getInstance().getString(message[i]);
        }
        return msg;
    }

    public static String ic(String... message)
    {
        String msg = "";
        int j = 0;
        for (int i = 0; i < message.length; i++)
        {
            if (message[i] == null || message[i].equals("")) continue;

            if (j > 0)
            {
                msg += ", ";
            }
            msg += message[i];

            j += 1;
        }
        return msg;
    }

    public static List<String> is(String... message)
    {
        List<String> msgs = new ArrayList<>();
        for (int i = 0; i < message.length; i++)
        {
            msgs.add(message[i]);
        }
        return msgs;
    }

    public static List<String> isa(String[] messages)
    {
        List<String> msgs = new ArrayList<>();
        for (int i = 0; i < messages.length; i++)
        {
            msgs.add(messages[i]);
        }
        return msgs;
    }


}

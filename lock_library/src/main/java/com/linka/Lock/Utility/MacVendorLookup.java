package com.linka.Lock.Utility;

import android.content.Context;
import android.util.Log;

import com.linka.Lock.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by loren on 1/21/2015.
 */
public class MacVendorLookup {
    Context m_context;
    private final static String TAG = MacVendorLookup.class.getSimpleName();

    public MacVendorLookup(Context context)
    {
        m_context = context;
    }

    public String lookupVendorFromMac(String mac)
    {
        int index = 0;
        // Search the MAC-vendor file for the specified MAC string, and return the vendor if found
        InputStream is = m_context.getResources().openRawResource(R.raw.mac_vendor_lookup);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = reader.readLine()) != null)
            {
                index++;
                if (!line.startsWith("#"))  // Ignore comment lines
                {
                    // Search for our prefix
                    if (line.startsWith(mac.substring(0,7)))    // Compare the first 8 chars, e.g. 00:07:80
                    {
                        // Each record is of the form
                        // <MAC>    <Short Name>    # <Long name>
                        // e.g.
                        // 00:00:12	Informat               # INFORMATION TECHNOLOGY LIMITED
                        //
                        // Return the long name
//                        return line.substring(line.lastIndexOf("#")+1) + String.format(" [%d]", index);
                        return line.substring(line.lastIndexOf("#")+1);
                    }
                }


            }
            reader.close();
        }catch (IOException e){
            Log.d(TAG, e.toString());
        };



        return ("[Vendor not found]");

    }


}

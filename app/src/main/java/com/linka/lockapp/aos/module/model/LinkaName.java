package com.linka.lockapp.aos.module.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

/**
 * Created by Vanson on 17/9/2016.
 */

@Table(name = "LinkaNames", id = "_id")
public class LinkaName extends Model {
    @Column(name = "linka_mac_address")
    public String linka_mac_address = "";
    @Column(name = "name")
    public String name = "";

    public static LinkaName getLinkaNameForMACAddress(String linka_mac_address) {
        LinkaName linkaName = new Select().from(LinkaName.class).where("linka_mac_address = ?", linka_mac_address).executeSingle();
        return linkaName;
    }

    public static void saveLinkaNameForMacAddress(String linka_mac_address, String name) {
        LinkaName linkaName = LinkaName.getLinkaNameForMACAddress(linka_mac_address);
        if (linkaName == null) {
            linkaName = new LinkaName();
            linkaName.linka_mac_address = linka_mac_address;
            linkaName.name = name;
            linkaName.save();
        } else {
            linkaName.name = name;
            linkaName.save();
        }
    }
}

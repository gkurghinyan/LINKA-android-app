package com.linka.lockapp.aos.module.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

/**
 * Created by Vanson on 20/4/2016.
 */
@Table(name = "LinkaAddresses", id = "_id")
public class LinkaAddress extends Model {
    @Column(name = "latitude")
    public String latitude = "";
    @Column(name = "longitude")
    public String longitude = "";
    @Column(name = "address")
    public String address = "";

    public static LinkaAddress getAddressForLatLng(String latitude, String longitude) {
        LinkaAddress address = new Select().from(LinkaAddress.class).where("latitude = ? and longitude = ?", latitude, longitude).executeSingle();
        return address;
    }

    public static void saveAddressForLatLng(String latitude, String longitude, String address) {
        if (LinkaAddress.getAddressForLatLng(latitude, longitude) == null) {
            LinkaAddress addr = new LinkaAddress();
            addr.latitude = latitude;
            addr.longitude = longitude;
            addr.address = address;
            addr.save();
        }
    }
}

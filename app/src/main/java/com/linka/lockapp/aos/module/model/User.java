package com.linka.lockapp.aos.module.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

/**
 * Created by kyle on 5/9/18.
 */

@Table(name = "Users", id = "_id")
public class User extends Model {
    @Column(name = "email")
    public String email = "";
    @Column(name = "first_name")
    public String first_name = "";
    @Column(name = "last_name")
    public String last_name = "";
    @Column(name = "name")
    public String name = "";
    @Column(name = "isOwner")
    public boolean isOwner = false;
    @Column(name = "isPendingApproval")
    public boolean isPendingApproval = false;

    public static User getUserForEmail(String email) {
        User user = new Select().from(User.class).where("email = ?", email).executeSingle();
        return user;
    }

    public static User saveUserForEmail(String email, String first_name, String last_name, String name, boolean isOwner, boolean isPendingApproval) {
        User user = User.getUserForEmail(email);
        if (user == null) {
            User newUser = new User();
            newUser.first_name = first_name;
            newUser.email = email;
            newUser.isOwner = isOwner;
            newUser.last_name = last_name;
            newUser.name = name;
            newUser.isPendingApproval = isPendingApproval;
            newUser.save();
            return newUser;
        } else {
            user.name = name;
            user.first_name = first_name;
            user.last_name = last_name;
            user.isPendingApproval = isPendingApproval;
            user.isOwner = isOwner;
            user.save();
            return user;
        }
    }
}

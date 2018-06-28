package com.linka.lockapp.aos.module.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.io.Serializable;

/**
 * Created by kyle on 5/9/18.
 */

@Table(name = "Users", id = "_id")
public class User extends Model implements Serializable{
    @Column(name = "email")
    public String email = "";
    @Column(name = "first_name")
    public String first_name = "";
    @Column(name = "last_name")
    public String last_name = "";
    @Column(name = "name")
    public String name = "";
    @Column(name = "userId")
    public String userId = "";
    @Column(name = "isOwner")
    public boolean isOwner = false;
    @Column(name = "isPendingApproval")
    public boolean isPendingApproval = false;
    @Column(name = "lastUsed")
    public String lastUsed = "";

    public static User getUserForEmail(String email) {
        User user = new Select().from(User.class).where("email = ?", email).executeSingle();
        return user;
    }

    public static User saveUserForEmail(String email, String first_name, String last_name, String name,String userId, boolean isOwner, boolean isPendingApproval,String lastUsed) {
        User user = User.getUserForEmail(email);
        if (user == null) {
            User newUser = new User();
            newUser.first_name = first_name;
            newUser.email = email;
            newUser.isOwner = isOwner;
            newUser.last_name = last_name;
            newUser.name = name;
            newUser.userId = userId;
            newUser.isPendingApproval = isPendingApproval;
            newUser.lastUsed = lastUsed;
            newUser.save();
            return newUser;
        } else {
            user.name = name;
            user.first_name = first_name;
            user.last_name = last_name;
            user.userId = userId;
            user.isPendingApproval = isPendingApproval;
            user.isOwner = isOwner;
            user.lastUsed = lastUsed;
            user.save();
            return user;
        }
    }
}

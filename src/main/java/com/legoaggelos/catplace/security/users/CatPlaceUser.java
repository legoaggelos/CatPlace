package com.legoaggelos.catplace.security.users;

import com.fasterxml.jackson.annotation.JsonCreator;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.legoaggelos.catplace.security.deserializers.SerialBlobDeserializer;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.sql.rowset.serial.SerialBlob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CatPlaceUser implements Persistable<String> {
    private final @Id String username;
    private String displayName;
    private SerialBlob profilePicture;
    private String bio;
    private String email;
    private boolean isAdmin;
    @Transient
    private boolean isNew = true;

    public CatPlaceUser(String displayName, String username, SerialBlob profilePicture, String bio, String email, boolean isAdmin, boolean isNew) {
        this.username = username;
        this.displayName = displayName;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.email = email;
        this.isAdmin = isAdmin;
        this.isNew=isNew;
    }
    @JsonCreator
    public CatPlaceUser(String displayName, String username, @JsonDeserialize(using = SerialBlobDeserializer.class) SerialBlob profilePicture, String bio, String email, boolean isAdmin) {
        this.username = username;
        this.displayName = displayName;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public CatPlaceUser(String displayName, String username, SerialBlob profilePicture, String bio, String email) {
        this.username = username;
        this.displayName = displayName;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.email = email;
        isAdmin=false;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    @PersistenceCreator
    public CatPlaceUser(String username) {
        this.displayName = username;
        this.username = username;
        profilePicture=null;
        bio="";
        email="";
        isAdmin=false;
    }

    public SerialBlob getProfilePicture() {
        return profilePicture;
    }

    public String getBio() {
        return bio;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public String getId() {
        return username;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}

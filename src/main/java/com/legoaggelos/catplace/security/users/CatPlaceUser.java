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
    private List<String> roles;
    @Transient
    private boolean isNew = true;

    public CatPlaceUser(String displayName, String username, SerialBlob profilePicture, String bio, String email, boolean isAdmin, boolean isNew) {
        this.username = username;
        this.displayName = displayName;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.email = email;
        this.roles=new ArrayList<>();
        roles.add("USER");
        if (isAdmin) {
            roles.add("ADMIN");
        }
        this.isNew=isNew;
    }
    @JsonCreator
    public CatPlaceUser(String displayName, String username, @JsonDeserialize(using = SerialBlobDeserializer.class) SerialBlob profilePicture, String bio, String email, boolean isAdmin) {
        this.username = username;
        this.displayName = displayName;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.email = email;
        this.roles=new ArrayList<>();
        roles.add("USER");
        if (isAdmin) {
            roles.add("ADMIN");
        }
    }

    public CatPlaceUser(String displayName, String username, SerialBlob profilePicture, String bio, String email) {
        this.username = username;
        this.displayName = displayName;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.email = email;
        this.roles=new ArrayList<>();
        roles.add("USER");
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
        this.roles=new ArrayList<>();
        roles.add("USER");
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
        return roles.contains("ADMIN");
    }

    @Override
    public String getId() {
        return username;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public List<String> getRoles() {
        return roles;
    }
}

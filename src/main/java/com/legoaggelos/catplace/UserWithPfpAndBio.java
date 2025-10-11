package com.legoaggelos.catplace;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.sql.rowset.serial.SerialBlob;
import java.util.Collection;

public class UserWithPfpAndBio extends User {
    private SerialBlob pfp;
    private String bio;

    public UserWithPfpAndBio(UserDetails user) {
        super(user.getUsername(), user.getPassword(), user.isEnabled(), user.isAccountNonExpired(), user.isCredentialsNonExpired(), user.isAccountNonLocked(), user.getAuthorities());
        pfp=null;
        bio="";
    }
    public UserWithPfpAndBio(UserDetails user, SerialBlob pfp, String bio) {
        super(user.getUsername(), user.getPassword(), user.isEnabled(), user.isAccountNonExpired(), user.isCredentialsNonExpired(), user.isAccountNonLocked(), user.getAuthorities());
        this.pfp=pfp;
        this.bio=bio;
    }

    public UserWithPfpAndBio(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        pfp = null;
        bio = "";
    }

    public UserWithPfpAndBio(String username, String password, Collection<? extends GrantedAuthority> authorities, SerialBlob pfp, String bio) {
        super(username, password, authorities);
        this.pfp = pfp;
        this.bio = bio;
    }

    public UserWithPfpAndBio(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, SerialBlob pfp, String bio) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.pfp = pfp;
        this.bio = bio;
    }

    public UserWithPfpAndBio(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    public SerialBlob getPfp() {
        return pfp;
    }

    public String getBio() {
        return bio;
    }
}

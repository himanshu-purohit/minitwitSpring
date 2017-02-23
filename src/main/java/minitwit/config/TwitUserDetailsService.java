package minitwit.config;

import minitwit.service.impl.MiniTwitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Himanshu on 02/21/2017.
 */
@Service
public class TwitUserDetailsService implements UserDetailsService {

    @Autowired
    private MiniTwitService service;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        minitwit.model.User modelUser = service.getUserbyUsername(s);
        GrantedAuthority ga = new GrantedAuthority(){
            public String getAuthority(){
                return "ROLE_ALL";
            }};
        Set<GrantedAuthority> gaSet = new HashSet<GrantedAuthority>();
        gaSet.add(ga);

        return new org.springframework.security.core.userdetails.User(modelUser.getUsername(),modelUser.getPassword(),gaSet);

        };
    }




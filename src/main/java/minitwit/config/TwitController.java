package minitwit.config;

import minitwit.model.Message;
import minitwit.model.User;
import minitwit.service.impl.MiniTwitService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TwitController {

    @Autowired
    private MiniTwitService service;

    /*
		 * Shows a users timeline or if no user is logged in,
		 *  it will redirect to the public timeline.
		 *  This timeline shows the user's messages as well
		 *  as all the messages of followed users.
		 */
    @GetMapping("/")
    public String loadTimeline(HttpServletRequest req, Map model , HttpServletResponse res){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Auth is:" + auth);
        if(auth.getName().equals("anonymousUser"))
            return "redirect:/public";
        User user = service.getUserbyUsername(auth.getName());
        model.put("pageTitle", "Timeline");
        model.put("user",user );
        List<Message> messages = service.getUserFullTimelineMessages(user);
        model.put("messages", messages);
        return "timeline";
    }

    /*
             * Displays the latest messages of all users.
             */
    @GetMapping("/public")
    public String loadPublicTimeline(Map model){

        model.put("pageTitle", "Public Timeline");
        model.put("user", service.getUserbyUsername(SecurityContextHolder.getContext().getAuthentication().getName()));
        List<Message> messages = service.getPublicTimelineMessages();
        model.put("messages", messages);
        return "timeline";
    }

    @GetMapping("/t/{username}")
    public String loadUserTweets(HttpServletRequest req, @PathVariable String username,Map<String,Object> model){
        User profileUser = service.getUserbyUsername(username);
        User authUser = service.getUserbyUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        boolean followed = false;
        if(authUser != null) {
            followed = service.isUserFollower(authUser, profileUser);
        }
        List<Message> messages = service.getUserTimelineMessages(profileUser);
        model.put("pageTitle", username + "'s Timeline");
        model.put("user", authUser);
        model.put("profileUser", profileUser);
        model.put("followed", followed);
        model.put("messages", messages);
        return "timeline";
    }

    @GetMapping("/login")
    public String presentLogin(HttpServletRequest request){
        Map<String, Object> map = new HashMap<>();
        return "login";
    }

    /* @PostMapping("/login")
    public String submitLogin(HttpServletRequest request){
        System.out.println("Received login request");
        Map<String, Object> map = new HashMap<>();
        User user = new User();
        try {
            BeanUtils.populate(user, request.getParameterMap());
        } catch (Exception e) {

            return null;
        }
        LoginResult result = service.checkUser(user);
        User authUser = result.getUser();
        if(authUser != null) {
            SecurityContextHolder.getContext().
                    setAuthentication(new UsernamePasswordAuthenticationToken(authUser.getUsername(),authUser.getPassword()));
            map.put("user",user);
            System.out.println("User found !!!!" + user);
            return "redirect:/";

        } else {
            map.put("error", result.getError());
        }
        map.put("username", user.getUsername());
        return "login";
    } */

    @GetMapping("/register")
    public String presentRegistration() {
        Map<String, Object> map = new HashMap<>();
        return "register";
    }

    @PostMapping("/register")
    public String submitRegistration(HttpServletRequest request,HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        User user = new User();
        try {
            BeanUtils.populate(user, request.getParameterMap());
        } catch (Exception e) {

            return null;
        }
        String error = user.validate();
        if(StringUtils.isEmpty(error)) {
            User existingUser = service.getUserbyUsername(user.getUsername());
            if(existingUser == null) {
                service.registerUser(user);
                return "redirect:/login";
            } else {
                error = "The username is already taken";
            }
        }
        map.put("error", error);
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        return "register";
    }

    @PostMapping("/message")
    public String postMessage(HttpServletRequest request){
        System.out.println("Received post request");
        User user = service.getUserbyUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Message m = new Message();
        m.setUserId(user.getId());
        m.setPubDate(new Date());
        try {
            BeanUtils.populate(m, request.getParameterMap());
        }
        catch(Exception e){
            System.out.println("Unable to post message");
            return "/";
        }
        service.addMessage(m);
        return "redirect:/";

    }

    @GetMapping("/t/{username}/follow")
    public String followUser(HttpServletRequest request, @PathVariable String username){
        User profileUser = service.getUserbyUsername(username);
        User authUser = service.getUserbyUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        service.followUser(authUser, profileUser);
        return "redirect:"+"/t/" + username;

    }

    @GetMapping("/t/{username}/unfollow")
    public String unfollowUser(HttpServletRequest request, @PathVariable String username){
        User profileUser = service.getUserbyUsername(username);
        User authUser = service.getUserbyUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        service.unfollowUser(authUser, profileUser);
        return "redirect:"+"/t/" + username;

    }
}
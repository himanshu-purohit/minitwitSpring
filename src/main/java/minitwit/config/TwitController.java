package minitwit.config;

import minitwit.model.Message;
import minitwit.model.User;
import minitwit.service.impl.MiniTwitService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String loadTimeline(@AuthenticationPrincipal Authentication auth, Map model) {

        if (auth instanceof AnonymousAuthenticationToken || auth == null) return "redirect:/public";
        minitwit.model.User appUser = service.getUserbyUsername(auth.getName());
        model.put("pageTitle", "Timeline");
        model.put("user", appUser);
        List<Message> messages = service.getUserFullTimelineMessages(appUser);
        model.put("messages", messages);
        return "timeline";
    }

    @GetMapping("/public")
    public String loadPublicTimeline(Map model, @AuthenticationPrincipal Authentication auth) {
        model.put("pageTitle", "Public Timeline");
        if (!(auth instanceof AnonymousAuthenticationToken) && auth != null)
            model.put("user", service.getUserbyUsername(auth.getName()));
        List<Message> messages = service.getPublicTimelineMessages();
        model.put("messages", messages);
        return "timeline";
    }

    @GetMapping("/t/{username}")
    public String loadUserTweets(@PathVariable String username, Map model, @AuthenticationPrincipal Authentication auth) {
        User profileUser = service.getUserbyUsername(username);
        User authUser = service.getUserbyUsername(auth.getName());
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
    public String presentLogin() {
        return "login";
    }

    @GetMapping("/register")
    public String presentRegistration() {
        return "register";
    }

    @PostMapping("/register")
    public String submitRegistration(@RequestParam Map<String, String> reqParamMap) {
        Map<String, Object> map = new HashMap<>();
        User user = new User();
        try {
            BeanUtils.populate(user, reqParamMap);
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
    public String postMessage(@AuthenticationPrincipal Authentication auth, @RequestParam Map<String, String> reqParamMap) {
        minitwit.model.User appUser = service.getUserbyUsername(auth.getName());
        Message m = new Message();
        m.setUserId(appUser.getId());
        m.setPubDate(new Date());
        try {
            BeanUtils.populate(m, reqParamMap);
        }
        catch(Exception e){
            System.out.println("Unable to post message");
            return "/";
        }
        service.addMessage(m);
        return "redirect:/";

    }

    @GetMapping("/t/{username}/follow")
    public String followUser(@AuthenticationPrincipal Authentication auth, @PathVariable String username) {
        User profileUser = service.getUserbyUsername(username);
        User authUser = service.getUserbyUsername(auth.getName());

        service.followUser(authUser, profileUser);
        return "redirect:"+"/t/" + username;

    }

    @GetMapping("/t/{username}/unfollow")
    public String unfollowUser(@AuthenticationPrincipal Authentication auth, @PathVariable String username) {
        User profileUser = service.getUserbyUsername(username);
        User authUser = service.getUserbyUsername(auth.getName());

        service.unfollowUser(authUser, profileUser);
        return "redirect:"+"/t/" + username;

    }
}
package minitwit.config;

import minitwit.model.LoginResult;
import minitwit.model.Message;
import minitwit.model.User;
import minitwit.service.impl.MiniTwitService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebConfig{

    @Autowired
    private MiniTwitService service;
    private static final String USER_SESSION_ID = "user";

    /*
		 * Shows a users timeline or if no user is logged in,
		 *  it will redirect to the public timeline.
		 *  This timeline shows the user's messages as well
		 *  as all the messages of followed users.
		 */
    @GetMapping("/")
    public String loadTimeline(Principal principal, HttpServletRequest req, Map model , HttpServletResponse res){


        if(principal==null)
            return "redirect:/public";
        User user = service.getUserbyUsername(principal.getName());
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
    public String loadPublicTimeline(HttpServletRequest req, Map model , HttpServletResponse res){

        model.put("pageTitle", "Public Timeline");
        model.put("user", null);
        List<Message> messages = service.getPublicTimelineMessages();
        model.put("messages", messages);
        return "timeline";
    }

    @GetMapping("/t/{username}")
    public String loadUserTweets(HttpServletRequest req, @PathVariable String username){
        User profileUser = service.getUserbyUsername(username);
        User authUser = service.getUserbyUsername(req.getUserPrincipal().getName());;
        boolean followed = false;
        if(authUser != null) {
            followed = service.isUserFollower(authUser, profileUser);
        }
        List<Message> messages = service.getUserTimelineMessages(profileUser);

        Map<String, Object> map = new HashMap<>();
        map.put("pageTitle", username + "'s Timeline");
        map.put("user", authUser);
        map.put("profileUser", profileUser);
        map.put("followed", followed);
        map.put("messages", messages);
        return "timeline";
    }

    @GetMapping("/login")
    public String presentLogin(HttpServletRequest req){
        return "login.ftl";
    }

    @PostMapping("/login")
    public String submitLogin(HttpServletRequest request){
        Map<String, Object> map = new HashMap<>();
        User user = new User();
        try {
            BeanUtils.populate(user, request.getParameterMap());
        } catch (Exception e) {
            halt(501);
            return null;
        }
        LoginResult result = service.checkUser(user);
        if(result.getUser() != null) {
            SecurityContextHolder.getContext().setAuthentication();
            res.redirect("/");
            halt();
        } else {
            map.put("error", result.getError());
        }
        map.put("username", user.getUsername());
        return "login.ftl";
    }
}
package minitwit.config;

import minitwit.model.Message;
import minitwit.model.User;
import minitwit.service.impl.MiniTwitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public String loadTimeline(HttpServletRequest req, Map model , HttpServletResponse res){
        User user = getAuthenticatedUser(req);
        if(user == null)
            return "redirect:/public";
            /*try {res.sendRedirect("/public");}
            catch (IOException e)
            {e.printStackTrace();}*/

        model.put("pageTitle", "Timeline");
        model.put("user", user);
        List<Message> messages = service.getUserFullTimelineMessages(user);
        model.put("messages", messages);
        return "timeline";
    }

    /*
             * Displays the latest messages of all users.
             */
    @GetMapping("/public")
    public String loadPublicTimeline(HttpServletRequest req, Map model , HttpServletResponse res){
        User user = getAuthenticatedUser(req);
        model.put("pageTitle", "Public Timeline");
        model.put("user", user);
        List<Message> messages = service.getPublicTimelineMessages();
        model.put("messages", messages);
        return "timeline";
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        return (User)request.getSession().getAttribute(USER_SESSION_ID);
    }
}
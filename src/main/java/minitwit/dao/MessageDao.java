package minitwit.dao;

import minitwit.model.Message;
import minitwit.model.User;

import java.util.List;

public interface MessageDao {
	List<Message> getUserTimelineMessages(User user);
	
	List<Message> getUserFullTimelineMessages(User user);
	
	List<Message> getPublicTimelineMessages();
	
	void insertMessage(Message m);
}

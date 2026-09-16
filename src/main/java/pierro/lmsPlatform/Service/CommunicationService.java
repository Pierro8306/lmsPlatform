package pierro.lmsPlatform.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Entity.Communication.Event;
import pierro.lmsPlatform.Entity.Communication.Member;
import pierro.lmsPlatform.Entity.Communication.Notification;
import pierro.lmsPlatform.Repository.Notification.EventRepository;
import pierro.lmsPlatform.Repository.Notification.MemberRepository;
import pierro.lmsPlatform.Repository.Notification.NotificationRepository;
import pierro.lmsPlatform.Repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommunicationService {
    EventRepository eventRepository;
    MemberRepository memberRepository;
    NotificationRepository notificationRepository;
    UserRepository userRepository;

    public void createNotification(String title, String description) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setDescription(description);
        notificationRepository.save(notification);
    }
    public void createEvent(String title, String description, LocalDate date) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setDate(date);
        eventRepository.save(event);
    }
    @Transactional
    public void joinEvent(Long eventId, Long id_member) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        User user = userRepository.findById(id_member)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id_member));
        if (memberRepository.existsByEventAndUser(event, user)) {
            throw new RuntimeException("User already joined this event");
        }
        Member member = new Member();
        member.setEvent(event);
        member.setUser(user);
        memberRepository.save(member);
    }
    @Transactional
    public void leaveEvent(Long eventId, Long userId) {
        Member member = memberRepository.findByEventIdAndUserId(eventId, userId);
        memberRepository.delete(member);
    }
    public List<Notification> getNotifications() {
        return notificationRepository.findAll();
    }
    public List<Event> getEvents() {
        return eventRepository.findAll();
    }
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
    }

    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
    }
    @Transactional
    public List<Member> getMembersByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        return new ArrayList<>(event.getMembers());
    }
}

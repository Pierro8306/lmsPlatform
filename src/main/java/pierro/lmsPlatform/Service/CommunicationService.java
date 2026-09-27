package pierro.lmsPlatform.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Entity.Communication.Event;
import pierro.lmsPlatform.Entity.Communication.Member;
import pierro.lmsPlatform.Entity.Communication.Notification;
import pierro.lmsPlatform.Repository.Notification.EventRepository;
import pierro.lmsPlatform.Repository.Notification.MemberRepository;
import pierro.lmsPlatform.Repository.Notification.NotificationRepository;
import pierro.lmsPlatform.Repository.Auth.UserRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunicationService {
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final IdObfuscator idObfuscator;

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
    public void joinEvent(String evtId, Long userId) {
        Long eventId = idObfuscator.decode(evtId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (memberRepository.existsByEventAndUser(event, user)) {
            throw new RuntimeException("User already joined this event");
        }

        Member member = new Member();
        member.setEvent(event);
        member.setUser(user);
        memberRepository.save(member);
    }

    @Transactional
    public void leaveEvent(String eventIdH, Long userId) {
        Long eventId = idObfuscator.decode(eventIdH);
        Member member = memberRepository.findByEventIdAndUserId(eventId, userId);
        if (member != null) {
            memberRepository.delete(member);
        }
    }

    public List<Notification> getNotifications() {
        return notificationRepository.findAll();
    }

    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(String Id) {
        Long eventId = idObfuscator.decode(Id);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
    }

    public Notification getNotificationById(String Id) {
        Long notificationId = idObfuscator.decode(Id);
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
    }

    @Transactional
    public List<Member> getMembersByEvent(String Id) {
        Long eventId = idObfuscator.decode(Id);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        return new ArrayList<>(event.getMembers());
    }
}

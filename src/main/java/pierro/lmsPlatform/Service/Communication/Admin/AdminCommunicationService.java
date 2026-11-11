package pierro.lmsPlatform.Service.Communication.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.Communication.EventRequest;
import pierro.lmsPlatform.DTO.Request.Communication.NotificationRequest;
import pierro.lmsPlatform.DTO.Response.Communication.MemberResponse;
import pierro.lmsPlatform.Entity.Communication.Event;
import pierro.lmsPlatform.Entity.Communication.Notification;
import pierro.lmsPlatform.Repository.Notification.EventRepository;
import pierro.lmsPlatform.Repository.Notification.MemberRepository;
import pierro.lmsPlatform.Repository.Notification.NotificationRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;
import pierro.lmsPlatform.Security.Exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCommunicationService {
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final IdObfuscator idObfuscator;

    @Transactional
    public void createNotification(NotificationRequest req) {
        Notification n = new Notification();
        n.setTitle(req.getTitle().trim());
        n.setDescription(req.getDescription() == null ? null : req.getDescription().trim());
        notificationRepository.save(n);
    }

    @Transactional
    public void updateNotification(String idHash, NotificationRequest req) {
        Notification n = findNotification(idObfuscator.decode(idHash));
        n.setTitle(req.getTitle().trim());
        n.setDescription(req.getDescription());
    }

    @Transactional
    public void deleteNotification(String idHash) {
        notificationRepository.delete(findNotification(idObfuscator.decode(idHash)));
    }

    @Transactional
    public void createEvent(EventRequest req) {
        Event e = new Event();
        e.setTitle(req.getTitle().trim());
        e.setDescription(req.getDescription() == null ? null : req.getDescription().trim());
        e.setDate(req.getDate());
        eventRepository.save(e);
    }

    @Transactional
    public void updateEvent(String idHash, EventRequest req) {
        Event e = findEvent(idObfuscator.decode(idHash));
        e.setTitle(req.getTitle().trim());
        e.setDescription(req.getDescription());
        e.setDate(req.getDate());
    }

    @Transactional
    public void deleteEvent(String idHash) {
        eventRepository.delete(findEvent(idObfuscator.decode(idHash)));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembersByEvent(String idHash) {
        Long eventId = idObfuscator.decode(idHash);
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Không tìm thấy sự kiện");
        }
        return memberRepository.findByEvent_Id(eventId).stream()
                .map(m -> new MemberResponse(
                        idObfuscator.encode(m.getUser().getId()),
                        m.getUser().getUsername(),
                        m.getUser().getEmail()))
                .toList();
    }

    private Notification findNotification(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự kiện"));
    }
}


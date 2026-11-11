package pierro.lmsPlatform.service.communication.Student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.response.Communication.EventResponse;
import pierro.lmsPlatform.dto.response.Communication.NotificationResponse;
import pierro.lmsPlatform.entity.communication.Event;
import pierro.lmsPlatform.entity.communication.Member;
import pierro.lmsPlatform.entity.communication.Notification;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.repository.notification.EventRepository;
import pierro.lmsPlatform.repository.notification.MemberRepository;
import pierro.lmsPlatform.repository.notification.NotificationRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunicationService {
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final IdObfuscator idObfuscator;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications() {
        return notificationRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(String idHash) {
        return notificationRepository.findById(idObfuscator.decode(idHash))
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEvents() {
        return eventRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(String idHash) {
        return toDto(findEvent(idObfuscator.decode(idHash)));
    }

    @Transactional
    public void joinEvent(String eventIdHash, Long userId) {
        Long eventId = requireEvent(eventIdHash);
        if (memberRepository.existsByEvent_IdAndUser_Id(eventId, userId)) {
            throw new ConflictException("Bạn đã tham gia sự kiện này rồi");
        }
        Member member = new Member();
        member.setEvent(eventRepository.getReferenceById(eventId));
        member.setUser(userRepository.getReferenceById(userId));
        memberRepository.save(member);
    }

    @Transactional
    public void leaveEvent(String eventIdHash, Long userId) {
        memberRepository.deleteByEvent_IdAndUser_Id(idObfuscator.decode(eventIdHash), userId);
    }

    private Long requireEvent(String idHash) {
        Long id = idObfuscator.decode(idHash);
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy sự kiện");
        }
        return id;
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự kiện"));
    }

    private EventResponse toDto(Event e) {
        return new EventResponse(idObfuscator.encode(e.getId()), e.getTitle(), e.getDescription(), e.getDate());
    }

    private NotificationResponse toDto(Notification n) {
        return new NotificationResponse(idObfuscator.encode(n.getId()), n.getTitle(), n.getDescription());
    }
}
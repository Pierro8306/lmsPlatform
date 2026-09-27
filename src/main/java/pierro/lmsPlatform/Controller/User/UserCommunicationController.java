package pierro.lmsPlatform.Controller.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.CommunicationService;

import java.util.Objects;

@RestController
@RequestMapping("/api/user/communications")
@RequiredArgsConstructor
public class UserCommunicationController {
    private final CommunicationService communicationService;

    @GetMapping("/notifications")
    public ResponseEntity<ResponseObject> getNotifications() {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy danh sách thông báo thành công", communicationService.getNotifications()));
    }

    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<ResponseObject> getNotificationById(@PathVariable String notificationId) {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy chi tiết thông báo thành công", communicationService.getNotificationById(notificationId)));
    }

    @GetMapping("/events")
    public ResponseEntity<ResponseObject> getEvents() {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy danh sách sự kiện thành công", communicationService.getEvents()));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<ResponseObject> getEventById(@PathVariable String eventId) {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy chi tiết sự kiện thành công", communicationService.getEventById(eventId)));
    }

    @PostMapping("/events/{eventId}/join")
    public ResponseEntity<ResponseObject> joinEvent(@PathVariable String eventId) {
        Long currentUserId = (Long) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        communicationService.joinEvent(eventId, currentUserId);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Tham gia sự kiện thành công", null));
    }

    @DeleteMapping("/events/{eventId}/leave")
    public ResponseEntity<ResponseObject> leaveEvent(@PathVariable String eventId) {
        Long currentUserId = (Long) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        communicationService.leaveEvent(eventId, currentUserId);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Rời khỏi sự kiện thành công", null));
    }
}
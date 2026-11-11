package pierro.lmsPlatform.controller.student;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.service.communication.Student.CommunicationService;

@RestController
@RequestMapping("/api/student/communications")
@RequiredArgsConstructor
public class StudentCommunicationController {
    private final CommunicationService communicationService;

    @GetMapping("/notifications")
    public ResponseEntity<ResponseObject> getNotifications() {
        return ResponseObject.ok("Lấy danh sách thông báo thành công", communicationService.getNotifications());
    }

    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<ResponseObject> getNotificationById(@PathVariable String notificationId) {
        return ResponseObject.ok("Lấy chi tiết thông báo thành công", communicationService.getNotificationById(notificationId));
    }

    @GetMapping("/events")
    public ResponseEntity<ResponseObject> getEvents() {
        return ResponseObject.ok("Lấy danh sách sự kiện thành công", communicationService.getEvents());
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<ResponseObject> getEventById(@PathVariable String eventId) {
        return ResponseObject.ok("Lấy chi tiết sự kiện thành công", communicationService.getEventById(eventId));
    }

    @PostMapping("/events/{eventId}/join")
    public ResponseEntity<ResponseObject> joinEvent(@PathVariable String eventId,
                                                  @AuthenticationPrincipal Long userId) {
        communicationService.joinEvent(eventId, userId);
        return ResponseObject.ok("Tham gia sự kiện thành công");
    }

    @DeleteMapping("/events/{eventId}/leave")
    public ResponseEntity<ResponseObject> leaveEvent(@PathVariable String eventId,
                                                   @AuthenticationPrincipal Long userId) {
        communicationService.leaveEvent(eventId, userId);
        return ResponseObject.ok("Rời khỏi sự kiện thành công");
    }
}

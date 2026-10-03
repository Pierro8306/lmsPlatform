package pierro.lmsPlatform.Controller.Admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Request.Communication.EventRequest;
import pierro.lmsPlatform.DTO.Request.Communication.NotificationRequest;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.Communication.Admin.AdminCommunicationService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/communications")
@RequiredArgsConstructor
public class AdminCommunicationController {
    private final AdminCommunicationService communicationService;

    @PostMapping("/notifications")
    public ResponseEntity<ResponseObject> createNotification(@RequestParam String title, @RequestParam String description) {
        communicationService.createNotification(title, description);
        return ResponseObject.ok("Tạo thông báo thành công");
    }

    @PostMapping("/events")
    public ResponseEntity<ResponseObject> createEvent(@RequestParam String title, @RequestParam String description, @RequestParam LocalDateTime date) {
        communicationService.createEvent(title, description, date.toLocalDate());
        return ResponseObject.ok("Tạo sự kiện thành công");
    }

    @PutMapping("/notifications/{notificationId}")
    public ResponseEntity<ResponseObject> updateNotification(@PathVariable String notificationId,
                                                             @Valid @RequestBody NotificationRequest request) {
        communicationService.updateNotification(notificationId, request);
        return ResponseObject.ok("Cập nhật thông báo thành công");
    }

    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<ResponseObject> deleteNotification(@PathVariable String notificationId) {
        communicationService.deleteNotification(notificationId);
        return ResponseObject.ok("Xoá thông báo thành công");
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<ResponseObject> updateEvent(@PathVariable String eventId,
                                                      @Valid @RequestBody EventRequest request) {
        communicationService.updateEvent(eventId, request);
        return ResponseObject.ok("Cập nhật sự kiện thành công");
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<ResponseObject> deleteEvent(@PathVariable String eventId) {
        communicationService.deleteEvent(eventId);
        return ResponseObject.ok("Xoá sự kiện thành công");
    }

    @GetMapping("/events/{eventId}/members")
    public ResponseEntity<ResponseObject> getMembersByEvent(@PathVariable String eventId) {
        return ResponseObject.ok("Lấy danh sách thành viên tham gia sự kiện thành công", communicationService.getMembersByEvent(eventId));
    }
}

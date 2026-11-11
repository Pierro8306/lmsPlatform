package pierro.lmsPlatform.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.dto.request.Communication.EventRequest;
import pierro.lmsPlatform.dto.request.Communication.NotificationRequest;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.service.communication.Admin.AdminCommunicationService;

@RestController
@RequestMapping("/api/admin/communications")
@RequiredArgsConstructor
public class AdminCommunicationController {
    private final AdminCommunicationService communicationService;

    @PostMapping("/notifications")
    public ResponseEntity<ResponseObject> createNotification(@Valid @RequestBody NotificationRequest request) {
        communicationService.createNotification(request);
        return ResponseObject.ok("Tạo thông báo thành công");
    }

    @PostMapping("/events")
    public ResponseEntity<ResponseObject> createEvent(@Valid @RequestBody EventRequest request) {
        communicationService.createEvent(request);
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


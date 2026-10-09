package com.example.petclinic.controller.api;

import com.example.petclinic.exception.AppointmentAccessException;
import jakarta.servlet.http.HttpSession;

/** Uses the team's session contract until StaffAccess is available on develop. */
final class AppointmentAccess {
    static final String OWNER_KEY = "myOwnerId";
    private AppointmentAccess() { }

    static Long owner(HttpSession session, Long requestedOwner) {
        // A client cannot set session attributes by passing query/body parameters.
        if (Boolean.TRUE.equals(session.getAttribute("isStaff"))) {
            if (requestedOwner == null || requestedOwner < 1) {
                throw new AppointmentAccessException("กรุณาเลือกเจ้าของสัตว์เลี้ยงก่อนจัดการนัดหมาย");
            }
            return requestedOwner;
        }
        Object selected = session.getAttribute(OWNER_KEY);
        if (!(selected instanceof Long ownerId) || ownerId < 1) {
            throw new AppointmentAccessException("กรุณาค้นหาเจ้าของด้วยเบอร์โทรก่อนจัดการนัดหมาย");
        }
        if (requestedOwner != null && !ownerId.equals(requestedOwner)) {
            throw new AppointmentAccessException("ไม่สามารถจัดการนัดหมายหรือสัตว์เลี้ยงของเจ้าของคนอื่นได้");
        }
        return ownerId;
    }
}

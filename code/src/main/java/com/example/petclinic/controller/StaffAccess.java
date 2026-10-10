package com.example.petclinic.controller;

import com.example.petclinic.exception.ForbiddenException;

import jakarta.servlet.http.HttpSession;

// ตรวจสิทธิ์เจ้าหน้าที่ที่ Backend (ใช้ร่วมกันทั้งหน้าเว็บและ REST API)
// ระบบไม่มี Login: เจ้าหน้าที่ใส่รหัส 8 หลักที่หน้า /owners/staff แล้วจำไว้ใน session
public final class StaffAccess {

    public static final String SESSION_KEY = "isStaff";
    public static final String MY_OWNER_ID = "myOwnerId";

    private StaffAccess() {
    }

    // ใส่รหัสเจ้าหน้าที่แล้วหรือยัง
    public static boolean isStaff(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(SESSION_KEY));
    }

    // ยังไม่ใส่รหัส -> 403 Forbidden
    public static void requireStaff(HttpSession session) {
        if (!isStaff(session)) {
            throw new ForbiddenException("รายการนี้ทำได้เฉพาะเจ้าหน้าที่");
        }
    }

    // เจ้าหน้าที่ได้ทุกแฟ้ม, ลูกค้าได้เฉพาะแฟ้มที่ค้นเจอด้วยเบอร์ตัวเอง
    public static boolean isOwnerOrStaff(HttpSession session, Long ownerId) {
        return isStaff(session) || (ownerId != null && ownerId.equals(session.getAttribute(MY_OWNER_ID)));
    }

    // ไม่ใช่เจ้าของและไม่ใช่เจ้าหน้าที่ -> 403 Forbidden
    public static void requireOwnerOrStaff(HttpSession session, Long ownerId) {
        if (!isOwnerOrStaff(session, ownerId)) {
            throw new ForbiddenException("ดูหรือแก้ไขได้เฉพาะข้อมูลของตัวเอง");
        }
    }
}
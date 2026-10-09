package com.example.petclinic.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.petclinic.controller.StaffAccess;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// ส่งค่า isStaff ให้ "ทุกหน้าเว็บ" ของทุกโมดูล (ไม่ใช่แค่หน้า Owner)
// navbar กลาง (fragments/layout.html) ใช้ค่านี้เลือกแสดงเมนูเฉพาะเจ้าหน้าที่
@ControllerAdvice(annotations = Controller.class)
public class WebModelAdvice {

    @ModelAttribute("isStaff")
    public boolean addIsStaff(HttpServletRequest request) {
        // getSession(false) = ไม่สร้าง session ใหม่ให้คนที่ยังไม่เคยเข้าเว็บ
        HttpSession session = request.getSession(false);
        return session != null && StaffAccess.isStaff(session);
    }
}
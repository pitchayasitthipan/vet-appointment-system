package com.example.petclinic.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;

import com.example.petclinic.domain.entity.Pet;
import com.example.petclinic.domain.entity.PetOwner;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

// เงื่อนไขค้นหาสัตว์เลี้ยงทั้งคลินิก (หน้า My Pets โหมดเจ้าหน้าที่)
// ค้นที่ฐานข้อมูล จึงเจอทุกตัว ไม่ใช่เฉพาะหน้าที่เปิดอยู่
public final class PetSpecifications {

    // ประเภทที่มีปุ่มกรองของตัวเอง ที่เหลือนับเป็น "อื่น ๆ" (Other)
    private static final List<String> MAIN_SPECIES = List.of("Dog", "Cat");

    // คำภาษาไทยที่หน้าเว็บแสดง -> ค่าที่เก็บในฐานข้อมูล (พิมพ์ "สุนัข" ก็ค้นเจอ Dog)
    private static final Map<String, String> THAI_SPECIES = Map.of(
            "สุนัข", "Dog",
            "แมว", "Cat");

    private PetSpecifications() {
    }

    // keyword: ชื่อสัตว์ / สายพันธุ์ / ประเภท / ชื่อหรือนามสกุลเจ้าของ (ไม่สนตัวพิมพ์เล็กใหญ่)
    // species: Dog, Cat หรือ Other (ว่าง = ทุกประเภท)
    public static Specification<Pet> search(String keyword, String species) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (species != null && !species.isBlank()) {
                if ("Other".equalsIgnoreCase(species)) {
                    conditions.add(cb.not(root.get("species").in(MAIN_SPECIES)));
                } else {
                    conditions.add(cb.equal(root.get("species"), species));
                }
            }

            if (keyword != null && !keyword.isBlank()) {
                String text = keyword.trim().toLowerCase(Locale.ROOT);
                String like = "%" + text + "%";
                Join<Pet, PetOwner> owner = root.join("petOwner");

                List<Predicate> matches = new ArrayList<>(List.of(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("breed"), "")), like),
                        cb.like(cb.lower(root.get("species")), like),
                        cb.like(cb.lower(owner.get("firstName")), like),
                        cb.like(cb.lower(owner.get("lastName")), like)));

                THAI_SPECIES.forEach((thai, value) -> {
                    if (thai.contains(text)) {
                        matches.add(cb.equal(root.get("species"), value));
                    }
                });

                conditions.add(cb.or(matches.toArray(Predicate[]::new)));
            }

            return cb.and(conditions.toArray(Predicate[]::new));
        };
    }
}
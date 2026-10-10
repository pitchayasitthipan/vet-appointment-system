package com.example.petclinic.controller.web;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petclinic.controller.StaffAccess;
import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.service.PetOwnerService;
import com.example.petclinic.service.PetService;
import com.example.petclinic.service.StaffPasscodeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

// หน้าเว็บเจ้าของสัตว์เลี้ยง แบ่ง 2 ฝั่ง
// 1. ลูกค้า: ค้นหาด้วยเบอร์ตัวเองก่อน -> จำ ownerId ไว้ -> ดูแฟ้มตัวเองได้ (แก้ไข/ลบไม่ได้)
// 2. เจ้าหน้าที่: ใส่รหัส 8 หลัก -> ดู/แก้ไข/ลบแฟ้มของทุกคนได้
@Controller
@RequestMapping("/owners")
public class PetOwnerWebController {

    private static final String MY_OWNER_ID = "myOwnerId"; // session: ลูกค้าค้นเจอเบอร์ตัวเองแล้ว

    private final PetOwnerService petOwnerService;

    // ใช้ดึงรายการสัตว์เลี้ยงของเจ้าของมาแสดงในแฟ้ม (โมดูล Pet)
    private final PetService petService;

    // รหัสเจ้าหน้าที่ 8 หลัก (ใช้รหัสเดียวทั้งคลินิก)
    // อ่านรหัสจาก clinic.staff-passcode ซึ่งกำหนดผ่าน Environment Variable
    private final StaffPasscodeService staffPasscodeService;

    public PetOwnerWebController(PetOwnerService petOwnerService,
            PetService petService,
            StaffPasscodeService staffPasscodeService) {
        this.petOwnerService = petOwnerService;
        this.petService = petService;
        this.staffPasscodeService = staffPasscodeService;
    }

    // ฝั่งลูกค้า: หน้าค้นหาข้อมูลของฉันด้วยเบอร์โทร
    @GetMapping
    public String searchByPhone(@RequestParam(required = false) String phone, HttpSession session, Model model) {
        model.addAttribute("phone", phone);

        // A new phone search must never retain access to the previous owner's record.
        if (phone != null) {
            session.removeAttribute(MY_OWNER_ID);
        }

        if (phone != null && !phone.isBlank()) {
            try {
                // เจอเบอร์ -> แสดงข้อมูลของเจ้าของคนนั้น
                PetOwnerResponseDTO owner = petOwnerService.getPetOwnerByPhone(phone.trim());
                session.setAttribute(MY_OWNER_ID, owner.getOwnerId());
                model.addAttribute("owner", owner);
                model.addAttribute("pets", petService.getPetsByOwnerId(owner.getOwnerId()));
            } catch (ResourceNotFoundException e) {
                // ไม่เจอเบอร์ -> แสดงปุ่มไปหน้าลงทะเบียนใหม่
                model.addAttribute("notFound", true);
            }
        }
        return "petowner/search";
    }

    // ฝั่งเจ้าหน้าที่: หน้ารายชื่อทั้งหมด (พร้อม Pagination) +
    // ค้นหาด้วยเบอร์โทร
    // ซ้าย = รายชื่อ, ขวา = ข้อมูลพรีวิวของคนที่เลือก (selected = ownerId)
    // ต้องใส่รหัสเจ้าหน้าที่ก่อน ถ้ายังไม่ใส่ -> แสดงหน้ากรอกรหัส
    @GetMapping("/staff")
    public String listPetOwners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long selected,
            HttpSession session,
            Model model) {

        if (!isStaff(session)) {
            return "petowner/staff-lock";
        }

        List<PetOwnerResponseDTO> owners;

        if (phone != null && !phone.isBlank()) {
            // ค้นหาด้วยเบอร์: เบอร์ห้ามซ้ำ
            try {
                owners = List.of(petOwnerService.getPetOwnerByPhone(phone.trim()));
            } catch (ResourceNotFoundException e) {
                owners = List.of();
            }
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", owners.size());
        } else {
            Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<PetOwnerResponseDTO> ownerPage = petOwnerService.getAllPetOwners(pageable);
            owners = ownerPage.getContent();
            model.addAttribute("totalPages", ownerPage.getTotalPages());
            model.addAttribute("totalItems", ownerPage.getTotalElements());
        }

        // คนที่แสดงฝั่งขวา: ถ้ากดเลือกมา ใช้คนนั้น ถ้าไม่ได้เลือก ใช้คนแรกในหน้า
        PetOwnerResponseDTO selectedOwner = null;
        for (PetOwnerResponseDTO owner : owners) {
            if (selectedOwner == null || owner.getOwnerId().equals(selected)) {
                selectedOwner = owner;
            }
        }

        model.addAttribute("owners", owners);
        model.addAttribute("selectedOwner", selectedOwner);
        // สัตว์เลี้ยงของคนที่เลือก (แสดงในกล่องข้อมูลย่อฝั่งขวา)
        if (selectedOwner != null) {
            model.addAttribute("pets", petService.getPetsByOwnerId(selectedOwner.getOwnerId()));
        }
        model.addAttribute("phone", phone);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "petowner/list";
    }

    // ตรวจรหัสเจ้าหน้าที่: ถูก -> จำไว้ใน session
    // จนกว่าจะออกจากระบบหรือปิดเบราว์เซอร์
    @PostMapping("/staff/unlock")
    public String unlockStaff(@RequestParam String passcode,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String clientKey = request.getRemoteAddr();

        StaffPasscodeService.Result result =
                staffPasscodeService.verify(clientKey, passcode);

        if (result.locked()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "ลองรหัสผิดเกินจำนวนครั้งที่กำหนด กรุณารออีก "
                            + result.remainingMinutes() + " นาที");
            return "redirect:/owners/staff";
        }

        if (result.success()) {
            session.setAttribute(StaffAccess.SESSION_KEY, true);
            return "redirect:/owners/staff";
        }

        redirectAttributes.addFlashAttribute(
                "errorMessage", "รหัสเจ้าหน้าที่ไม่ถูกต้อง");
        return "redirect:/owners/staff";
    }

    // ออกจากระบบเจ้าหน้าที่ -> กลับไปหน้ากรอกรหัส
    @PostMapping("/staff/logout")
    public String logoutStaff(HttpSession session) {
        session.removeAttribute(StaffAccess.SESSION_KEY);
        return "redirect:/owners/staff";
    }

    // 2. หน้าแสดงรายละเอียด (Detail)
    @GetMapping("/{id}")
    public String showOwnerDetail(@PathVariable Long id, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        if (!canAccess(session, id)) {
            return denyAccess(redirectAttributes);
        }
        try {
            PetOwnerResponseDTO owner = petOwnerService.getPetOwnerById(id);
            model.addAttribute("owner", owner);
            model.addAttribute("pets", petService.getPetsByOwnerId(id));
            return "petowner/detail";
        } catch (ResourceNotFoundException e) {
            // ไม่พบข้อมูล -> กลับไปหน้ารายชื่อ พร้อมแจ้งเตือน
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/owners";
        }
    }

    // 3. หน้าฟอร์มเพิ่มข้อมูลใหม่ (Create Form)
    // ถ้าค้นหาเบอร์แล้วไม่เจอ จะส่ง ?phone= มาด้วย -> ก็จะกรอกเบอร์ให้อัตโนมัติ
    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) String phone, Model model) {
        PetOwnerRequestDTO requestDTO = new PetOwnerRequestDTO();
        requestDTO.setPhone(phone);
        model.addAttribute("ownerRequest", requestDTO);
        model.addAttribute("isEdit", false);
        return "petowner/form";
    }

    // 4. หน้าฟอร์มแก้ไขข้อมูล (Edit Form)
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        if (!isStaff(session)) {
            return staffOnly(redirectAttributes);
        }
        PetOwnerResponseDTO existingOwner;
        try {
            existingOwner = petOwnerService.getPetOwnerById(id);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/owners";
        }

        // แปลง ResponseDTO กลับเป็น RequestDTO เพื่อ bind เข้ากับ form
        PetOwnerRequestDTO requestDTO = new PetOwnerRequestDTO();
        requestDTO.setFirstName(existingOwner.getFirstName());
        requestDTO.setLastName(existingOwner.getLastName());
        requestDTO.setEmail(existingOwner.getEmail());
        requestDTO.setPhone(existingOwner.getPhone());
        requestDTO.setAddress(existingOwner.getAddress());
        requestDTO.setEmergencyContactName(existingOwner.getEmergencyContactName());
        requestDTO.setEmergencyContactPhone(existingOwner.getEmergencyContactPhone());

        model.addAttribute("ownerId", id);
        model.addAttribute("owner", existingOwner); // ใช้แสดงเลขแฟ้ม + วันลงทะเบียน
        model.addAttribute("ownerRequest", requestDTO);
        model.addAttribute("isEdit", true);
        return "petowner/form";
    }

    // 5. บันทึกข้อมูลใหม่
    // next = "pet" เมื่อกดปุ่ม "บันทึกและเพิ่มสัตว์เลี้ยง"
    @PostMapping
    public String createOwner(
            @Valid @ModelAttribute("ownerRequest") PetOwnerRequestDTO requestDTO,
            BindingResult bindingResult,
            @RequestParam(required = false) String next,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "petowner/form";
        }

        try {
            PetOwnerResponseDTO savedOwner = petOwnerService.createPetOwner(requestDTO);
            if (!isStaff(session)) {
                // ลูกค้าลงทะเบียนเอง -> จำไว้ว่าเป็นแฟ้มของเบราว์เซอร์นี้
                session.setAttribute(MY_OWNER_ID, savedOwner.getOwnerId());
            }
            if ("pet".equals(next)) {
                // ส่ง ownerId ต่อให้หน้าเพิ่มสัตว์เลี้ยง
                return "redirect:/pets/new?ownerId=" + savedOwner.getOwnerId();
            }
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกข้อมูลเรียบร้อยแล้ว");
            return "redirect:/owners/" + savedOwner.getOwnerId();
        } catch (DuplicateResourceException e) { // อีเมล หรือ เบอร์โทรซ้ำ
            model.addAttribute("errorMessage", e.getMessage());
            addDuplicatePhoneOwner(requestDTO.getPhone(), null, model);
            model.addAttribute("isEdit", false);
            return "petowner/form";
        }
    }

    // 6. บันทึกการแก้ไขข้อมูล
    @PostMapping("/{id}")
    public String updateOwner(
            @PathVariable Long id,
            @Valid @ModelAttribute("ownerRequest") PetOwnerRequestDTO requestDTO,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isStaff(session)) {
            return staffOnly(redirectAttributes);
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("ownerId", id);
            model.addAttribute("owner", petOwnerService.getPetOwnerById(id));
            model.addAttribute("isEdit", true);
            return "petowner/form";
        }

        try {
            petOwnerService.updatePetOwner(id, requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "แก้ไขข้อมูลเรียบร้อยแล้ว");
            return "redirect:/owners/" + id;
        } catch (DuplicateResourceException | ResourceNotFoundException e) { // อีเมล/เบอร์ซ้ำ หรือไม่พบ Id
            model.addAttribute("errorMessage", e.getMessage());
            addDuplicatePhoneOwner(requestDTO.getPhone(), id, model);
            model.addAttribute("ownerId", id);
            model.addAttribute("isEdit", true);
            return "petowner/form";
        }
    }

    // 7. ลบข้อมูล: เฉพาะเจ้าหน้าที่
    @PostMapping("/{id}/delete")
    public String deleteOwner(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isStaff(session)) {
            return staffOnly(redirectAttributes);
        }
        try {
            petOwnerService.deletePetOwner(id);
            redirectAttributes.addFlashAttribute("successMessage", "ลบข้อมูลเรียบร้อยแล้ว");
        } catch (ResourceNotFoundException | DuplicateResourceException e) { // ไม่พบ Id หรือ ยังมีสัตว์เลี้ยงอยู่
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owners/staff";
    }

    // เช็กว่าใส่รหัสเจ้าหน้าที่แล้วหรือยัง
    private boolean isStaff(HttpSession session) {
        return StaffAccess.isStaff(session);
    }

    // เจ้าหน้าที่ได้ทุกแฟ้ม, ลูกค้าได้เฉพาะแฟ้มที่ค้นเจอด้วยเบอร์ตัวเอง
    private boolean canAccess(HttpSession session, Long ownerId) {
        return isStaff(session) || ownerId.equals(session.getAttribute(MY_OWNER_ID));
    }

    // ไม่มีสิทธิ์ -> กลับไปหน้าค้นหาด้วยเบอร์ พร้อมแจ้งเตือน
    private String denyAccess(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "กรุณาค้นหาด้วยเบอร์โทรศัพท์ของคุณก่อน");
        return "redirect:/owners";
    }

    // แก้ไข/ลบ เฉพาะเจ้าหน้าที่ -> พาไปหน้ากรอกรหัส
    private String staffOnly(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "การแก้ไขและลบข้อมูลทำได้เฉพาะเจ้าหน้าที่");
        return "redirect:/owners/staff";
    }

    // ถ้าเบอร์นี้มีเจ้าของคนอื่นใช้แล้ว ส่งข้อมูลคนนั้นไปแสดงกล่อง "เปิดแฟ้มเดิม"
    // ในฟอร์ม
    private void addDuplicatePhoneOwner(String phone, Long currentOwnerId, Model model) {
        try {
            PetOwnerResponseDTO other = petOwnerService.getPetOwnerByPhone(phone);
            if (!other.getOwnerId().equals(currentOwnerId)) {
                model.addAttribute("duplicateOwner", other);
            }
        } catch (ResourceNotFoundException e) {
            // ไม่มีใครใช้เบอร์นี้ -> error มาจากอีเมลซ้ำ แสดงแค่ errorMessage
        }
    }
}
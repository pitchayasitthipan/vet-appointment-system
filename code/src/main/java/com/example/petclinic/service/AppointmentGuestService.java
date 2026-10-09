package com.example.petclinic.service;

import java.util.List;
import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.exception.*;
import com.example.petclinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AppointmentGuestService {
    private final AppointmentGuestOwnerRepository owners;
    private final PetRepository pets;
    public AppointmentGuestService(AppointmentGuestOwnerRepository owners, PetRepository pets) {
        this.owners = owners; this.pets = pets;
    }

    public AppointmentGuestOwnerDTO lookup(String phone) {
        if (phone == null || phone.length() > 20 || !phone.matches("[0-9 -]+")) {
            throw new InvalidAppointmentException("กรุณาระบุเบอร์โทรศัพท์ไทย 9-10 หลัก");
        }
        String normalized = phone.replace(" ", "").replace("-", "");
        if (!normalized.matches("0[0-9]{8,9}")) {
            throw new InvalidAppointmentException("กรุณาระบุเบอร์โทรศัพท์ไทย 9-10 หลักขึ้นต้นด้วย 0");
        }
        List<PetOwner> found = owners.findByNormalizedPhone(normalized);
        if (found.isEmpty()) throw new ResourceNotFoundException("ไม่พบเบอร์โทรศัพท์นี้ กรุณาลงทะเบียนเจ้าของใหม่");
        if (found.size() > 1) {
            throw new DuplicateResourceException("เบอร์โทรศัพท์นี้เชื่อมกับหลายแฟ้ม กรุณาติดต่อคลินิกเพื่อตรวจสอบข้อมูล");
        }
        PetOwner owner = found.get(0);
        return new AppointmentGuestOwnerDTO(owner.getOwnerId(), owner.getFirstName(), owner.getLastName());
    }

    public List<AppointmentGuestPetDTO> pets(Long ownerId) {
        if (ownerId == null || ownerId < 1) throw new InvalidAppointmentException("รหัสเจ้าของต้องเป็นจำนวนเต็มบวก");
        if (!owners.existsById(ownerId)) throw new ResourceNotFoundException("ไม่พบเจ้าของสัตว์เลี้ยง");
        return pets.findByPetOwnerOwnerIdOrderByPetNameAsc(ownerId).stream()
            .map(p -> new AppointmentGuestPetDTO(p.getPetId(), p.getPetName())).toList();
    }
}

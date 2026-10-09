package com.example.petclinic.service;

import java.util.List;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.exception.InvalidAppointmentException;
import com.example.petclinic.repository.AppointmentPetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AppointmentGuestService {
    private final PetOwnerService owners;
    private final AppointmentPetRepository pets;
    public AppointmentGuestService(PetOwnerService owners, AppointmentPetRepository pets) {
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
        return identity(owners.getPetOwnerByPhone(normalized));
    }

    public AppointmentGuestOwnerDTO owner(Long ownerId) {
        if (ownerId == null || ownerId < 1) throw new InvalidAppointmentException("รหัสเจ้าของต้องเป็นจำนวนเต็มบวก");
        return identity(owners.getPetOwnerById(ownerId));
    }

    private AppointmentGuestOwnerDTO identity(PetOwnerResponseDTO owner) {
        return new AppointmentGuestOwnerDTO(owner.getOwnerId(), owner.getFirstName(), owner.getLastName());
    }

    public List<AppointmentGuestPetDTO> pets(Long ownerId) {
        owner(ownerId);
        return pets.findByPetOwnerOwnerIdOrderByPetNameAsc(ownerId).stream()
            .map(p -> new AppointmentGuestPetDTO(p.getPetId(), p.getPetName())).toList();
    }
}

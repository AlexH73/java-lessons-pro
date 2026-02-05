package de.ait.javalessonspro.repositories;

import de.ait.javalessonspro.model.CarDocumentOs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarDocumentOsRepository  extends JpaRepository<CarDocumentOs, Long> {
    List<CarDocumentOs> findAllByCarId(Long carId);
}

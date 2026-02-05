package de.ait.javalessonspro.repositories;

import de.ait.javalessonspro.model.ClientDocumentDb;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientDocumentDbRepository extends JpaRepository<ClientDocumentDb, Long> {

    List<ClientDocumentDb> findAllByClientEmail(String clientEmail);
}

package de.ait.javalessonspro.repositories;

import de.ait.javalessonspro.model.CandidateDocumentOs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 07.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
public interface CandidateDocumentOsRepository extends JpaRepository<CandidateDocumentOs, Long> {
    List<CandidateDocumentOs> findAllByCandidateEmail(String email);

    long countByCandidateEmail(String email);

    void deleteAllByCandidateEmail(String email);
}

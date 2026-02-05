package de.ait.javalessonspro.service;

import de.ait.javalessonspro.enums.ClientDocumentType;
import de.ait.javalessonspro.model.ClientDocumentDb;
import de.ait.javalessonspro.repositories.ClientDocumentDbRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientDocumentDbService {

    private final ClientDocumentDbRepository repository;

    public ClientDocumentDb uploadClientDocument(
            String clientEmail, ClientDocumentType docType, MultipartFile file){
        if(clientEmail == null || clientEmail.isBlank()){
            log.error("Invalid client email: {}", clientEmail);
            throw new IllegalArgumentException("Invalid client email");
        }
        if(file == null || file.isEmpty()){
            log.error("File is null or empty");
            throw new IllegalArgumentException("File is empty");
        }

        try {
            byte[] data = file.getBytes();
            ClientDocumentDb doc = new ClientDocumentDb(
                    clientEmail,
                    docType,
                    file.getContentType(),
                    file.getSize(),
                    file.getOriginalFilename(),
                    data);

            ClientDocumentDb savedDoc = repository.save(doc);
            log.info("Client document with id {} saved", savedDoc.getId());
            return savedDoc;
        }
        catch (IOException exception){
            log.error("Error reading file", exception);
            throw new RuntimeException("Error reading file", exception);
        }

    }

    public List<ClientDocumentDb> getAllClientDocuments(String clientEmail){
        return repository.findAllByClientEmail(clientEmail);
    }

    public ClientDocumentDb getClientDocument(Long id){
        return repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Document with id " + id + " not found")
        );
    }

}

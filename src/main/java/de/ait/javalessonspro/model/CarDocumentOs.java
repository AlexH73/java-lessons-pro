package de.ait.javalessonspro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.ait.javalessonspro.enums.CarDocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "car_documents_os")
@Getter
@Setter
@NoArgsConstructor
public class CarDocumentOs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    @JsonIgnore
    private Car car;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    private CarDocumentType docType;

    @Column(name = "original_filename", nullable = false)
    private String originalFileName;

    @Column(name = "stored_filename", nullable = false)
    private String storedFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    public CarDocumentOs(Car car,
                         String storagePath,
                         Long size,
                         String contentType,
                         String storedFileName,
                         String originalFileName,
                         CarDocumentType docType) {
        this.car = car;
        this.storagePath = storagePath;
        this.size = size;
        this.contentType = contentType;
        this.storedFileName = storedFileName;
        this.originalFileName = originalFileName;
        this.docType = docType;
    }
}

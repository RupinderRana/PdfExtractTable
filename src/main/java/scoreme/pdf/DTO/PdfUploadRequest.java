package scoreme.pdf.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfUploadRequest {
    private String fileName;
    private boolean consolidateToOneFile;
}
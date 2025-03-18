package scoreme.pdf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfFileInfo {
    private String fileName;
    private String filePath;
    private int pageCount;
    private List<TableData> tables;
}
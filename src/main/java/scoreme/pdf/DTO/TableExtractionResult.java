package scoreme.pdf.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import scoreme.pdf.entity.TableData;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableExtractionResult {
    private boolean success;
    private String message;
    private List<TableData> tables;
}
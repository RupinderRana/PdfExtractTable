package scoreme.pdf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableData {
    private String tableName;
    private List<String> headers;
    private List<List<String>> rows;
    private int pageNumber;
    private int tableNumber;
}
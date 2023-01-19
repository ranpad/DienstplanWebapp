package at.kaindorf.dienstplan.bl;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Excel {
    public static void main(String[] args) throws IOException {

    }

    public String returnExcelRows() {
        List<List<String>> rows = null;
        /*List<String> testList = new ArrayList<>();
        testList.add("hallo");
        rows.add(testList);*/
        try {
            // Open the Excel file
            File excelFile = new File("src/main/resources/Excel/Wichtige_Dinge_Dienstplan.xlsx");
            Workbook workbook = WorkbookFactory.create(excelFile);

            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);
            rows = new ArrayList<>();

            // Iterate through the rows and add the cell values to the list
            for (Row row : sheet) {
                int cnt = 0;
                List<String> rowValues = new ArrayList<>();
                for (Cell cell : row) {
                    int cellIndex = cell.getColumnIndex();
                    //cell soll immer ungleich cnt sein wenn in dieser Zelle kein Wert steht
                    if (cell.getColumnIndex() != cnt){
                        System.out.println("im if");
                        rowValues.add("Blank");
                    }else{
                        System.out.println("nicht im if");
                        rowValues.add(cell.toString());
                    }
                    cnt++;
                }
                rows.add(rowValues);
            }

            workbook.close();
            rows.forEach(r -> System.out.println(r + "\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert rows != null;
        return rows.toString();
    }
}
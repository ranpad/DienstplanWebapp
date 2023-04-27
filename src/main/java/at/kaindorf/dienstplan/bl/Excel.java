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
        List<String> weekdays = new ArrayList<>();
        List<Mitarbeiter> mitarbeiterList = new ArrayList<>();
        File excelFile = null;

        String[] jsonstring;

        try {
            excelFile = new File("src/main/resources/Excel/Wichtige_Dinge_Dienstplan.xlsx");
            Workbook workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            rows = new ArrayList<>();

            int cellIndex;
            int minusColumIndex = 0;

            for (Row row : sheet) {
                int cnt = 0;
                List<String> rowValues = new ArrayList<>();
                for (Cell cell : row) {
                    cellIndex = cell.getColumnIndex();
                    //cell soll immer ungleich cnt sein wenn in dieser Zelle kein Wert steht
                    if (cellIndex - minusColumIndex != cnt) {
                        for (int i = cellIndex - cnt; i > 0; i--) {
                            rowValues.add("Blank");
                        }
                        cnt = cellIndex;
                        rowValues.add(cell.toString());
                    } else {
                        rowValues.add(cell.toString());
                    }
                    cnt++;
                }
                mitarbeiterList.add(new Mitarbeiter(rowValues.get(1), rowValues.get(0), 0.0, 0.0,
                        null, rowValues.subList(2, rowValues.size())));

                rows.add(rowValues);
            }

            workbook.close();

            weekdays = mitarbeiterList.get(1).getCalenderDays();
            // Die ersten zwei Zeilen entfernen da das keine Mitarbeiter sind
            mitarbeiterList = mitarbeiterList.subList(2, mitarbeiterList.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writeToExcel(rows);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert rows != null;
        return rows.toString();
    }
    public void writeToExcel(List<List<String>> rows) throws IOException {
        String filePath = "src/main/resources/Excel/FertigerPlan.xlsx";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        int rowIndex = 0;
        for (List<String> row : rows) {
            Row newRow = sheet.createRow(rowIndex++);
            int columnIndex = 0;
            for (String value : row) {
                Cell cell = newRow.createCell(columnIndex++);
                cell.setCellValue(value);
            }
        }
        FileOutputStream outputStream = new FileOutputStream(filePath);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}

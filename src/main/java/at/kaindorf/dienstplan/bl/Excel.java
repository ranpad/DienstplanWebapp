package at.kaindorf.dienstplan.bl;

import at.kaindorf.dienstplan.pojos.Mitarbeiter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Excel {
    public static void main(String[] args) throws IOException {

    }

    public String returnExcelRows() {
        List<List<String>> rows = null;
        List<Mitarbeiter> mitarbeiterList = new ArrayList<>();
        File excelFile = null;

        try {
            excelFile = new File("src/main/resources/Excel/Wichtige_Dinge_Dienstplan.xlsx");
            Workbook workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            rows = new ArrayList<>();

            int cellIndex;
            int minusColumIndex = 0;

            for (Row row : sheet) {
                String firstname;
                String lastname;
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
                firstname = rowValues.get(1);
                lastname = rowValues.get(0);
                mitarbeiterList.add(new Mitarbeiter(firstname, lastname, 0.0, 0.0,
                        null, rowValues.subList(2, rowValues.size())));

                rows.add(rowValues);
            }
            workbook.close();

            // Die ersten zwei einträge entfernen da das keine Mitarbeiter sind
            mitarbeiterList = mitarbeiterList.subList(2, mitarbeiterList.size());
            rows.forEach(r -> System.out.println(r + "\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        createNewExcelFile(excelFile);
        //writeContentInExcelList(rows);
        assert rows != null;
        return rows.toString();
    }

    public void writeContentInExcelList(List<List<String>> rows){
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Hello, World!");

        try (FileOutputStream outputStream = new FileOutputStream("src/main/resources/Excel/Wichtige_Dinge_Dienstplan.xlsx")) {
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createNewExcelFile(File excelFile){
        Path newFile = Path.of("src/main/resources/Excel/FertigerPlan.xlsx");

        try {
            Files.deleteIfExists(newFile);
            Files.copy(excelFile.toPath(), newFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

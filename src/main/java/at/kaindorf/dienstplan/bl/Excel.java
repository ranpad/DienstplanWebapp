package at.kaindorf.dienstplan.bl;

import at.kaindorf.dienstplan.pojos.Mitarbeiter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

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

        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);


        int rowIndex = 0;
        for (List<String> row : rows) {
            Row newRow = sheet.createRow(rowIndex++);
            int columnIndex = 0;
            for (String value : row) {
                Cell cell = newRow.createCell(columnIndex++);
                //Prüft ob auf Value gespeicherter Wert eine Zahl ist
                if (value != null && value.matches("\\b\\d+")){
                    cell.setCellValue(Double.parseDouble(value));
                }else{
                    cell.setCellValue(value);
                }

                if(value != null && (value.equals("F") || value.equals("U"))){
                    cell.setCellStyle(cellStyle);
                }


            }
        }

        autoSizeColumns(sheet, 40);
        formatColumns(workbook, sheet, font);

        FileOutputStream outputStream = new FileOutputStream(filePath);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public static void autoSizeColumns(Sheet sheet, int numberOfColumns) {
        for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
        }
    }

    public void formatColumns(Workbook workbook, Sheet sheet, Font font){
        List<String> saturdayList = new ArrayList<>();
        List<String> sundayList = new ArrayList<>();

        CellStyle saturdayStyle = workbook.createCellStyle();
        saturdayStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        saturdayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle saturdayStyleRed = workbook.createCellStyle();
        saturdayStyleRed.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        saturdayStyleRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        saturdayStyleRed.setFont(font);

        CellStyle sundayStyle = workbook.createCellStyle();
        sundayStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        sundayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle sundayStyleRed = workbook.createCellStyle();
        sundayStyleRed.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        sundayStyleRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sundayStyleRed.setFont(font);

        Row firstRow = sheet.getRow(0);

        for (Cell cell : firstRow){
            String cellValue = cell.getStringCellValue();
            String columnIndex = String.valueOf(cell.getColumnIndex());
            if (cellValue.equals("Sa.")){
                saturdayList.add(columnIndex);
            }

            if (cellValue.equals("So.")){
                sundayList.add(columnIndex);
            }
        }
        System.out.println(saturdayList);
        System.out.println(sundayList);

        Iterator<Row> rowIterator = sheet.iterator();

        while(rowIterator.hasNext()){
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.iterator();

            while (cellIterator.hasNext()){
                Cell cell = cellIterator.next();
                String cellColumnIndex = String.valueOf(cell.getColumnIndex());
                String cellValue = null;

                if (cell.getCellType() == CellType.STRING){
                    cellValue = cell.getStringCellValue();
                }
                if (cell.getCellType() == CellType.NUMERIC){
                    cellValue = String.valueOf(cell.getNumericCellValue());
                }

                if (saturdayList.contains(cellColumnIndex)){
                    cell.setCellStyle(saturdayStyle);
                    if (cellValue != null && (cellValue.equals("F") || cellValue.equals("U"))){
                        cell.setCellStyle(saturdayStyleRed);
                    }
                }
                if (sundayList.contains(cellColumnIndex)){
                    cell.setCellStyle(sundayStyle);
                    if (cellValue != null && (cellValue.equals("F") || cellValue.equals("U"))){
                        cell.setCellStyle(sundayStyleRed);
                    }
                }

            }
        }
    }
}

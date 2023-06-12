package at.kaindorf.dienstplan.bl;

import at.kaindorf.dienstplan.pojos.Mitarbeiter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Excel {


    public static void main(String[] args) throws IOException {

    }

    public void startProgramm(List<List<String>> rows) throws IOException {
        List<Mitarbeiter> mitarbeiterList = new ArrayList<>();
        Map<Date, List<Mitarbeiter>> schichtplan = new HashMap<>();
        mitarbeiterList = createMitarbeiterList(rows);

        schichtplan = erstelleSchichtplan(mitarbeiterList);
        rows = insertInRows(schichtplan, rows, mitarbeiterList);

        writeToExcel(rows);
    }

    public List<List<String>> insertInRows(Map<Date, List<Mitarbeiter>> schichtplan, List<List<String>> rows, List<Mitarbeiter> alleMitarbeiter){

        int countRow = 0;
        int countField;
        int countCurrentField;
        int countCurrentRow;
        int currentDay;
        String lastName = null;
        Date datum = null;
        boolean isMitarbeiterWorking = false;
        List<Mitarbeiter> mitarbeiterList = new ArrayList<>();
        Mitarbeiter mitarbeiter = null;
        List<List<String>> newRows = new ArrayList<>();
        List<String> newRow = new ArrayList<>();
        Map<Date, List<Mitarbeiter>> sortedSchichtplan = sortMap(schichtplan);
        for (List<String> row : rows) {
            countField = 0;
            countCurrentField = 0;
            countCurrentRow = 0;
            currentDay = 1;
            newRow = new ArrayList<>();
            //Erste Zwei Zeilen Skippen
            if (countRow < 2) {
                newRows.add(row);
                countRow++;
                continue;
            }

            for (String field : row){
                isMitarbeiterWorking = false;
                mitarbeiterList = new ArrayList<>();
                //Erste Zwei Felder Skippen
                if (countField < 2) {
                    lastName = field;
                    newRow.add(countCurrentField, field);
                    countField++;
                    countCurrentField++;
                    continue;
                }

                for (Map.Entry<Date, List<Mitarbeiter>> entry : sortedSchichtplan.entrySet()) {
                    datum = entry.getKey();
                    int tag = datum.getDate();

                    if (tag == currentDay) {
                        mitarbeiterList = entry.getValue();
                        break;
                    }
                }

                for (Mitarbeiter m : mitarbeiterList){
                    if (m.getLastname().equals(lastName)) {
                        isMitarbeiterWorking = true;
                        mitarbeiter = m;
                        break;
                    }
                }

                if (!isMitarbeiterWorking){
                    newRow.add(countCurrentField, field);
                }

                if (isMitarbeiterWorking){
                    if(mitarbeiter.getCalenderDays().get(countCurrentField - 2) != null && mitarbeiter.getCalenderDays().get(countCurrentField - 2).equals("ND")){
                        newRow.add(countCurrentField, "ND");
                    }else{
                        newRow.add(countCurrentField, "TD");
                    }
                }
                currentDay++;
                countCurrentField++;
            }
            newRows.add(newRow);
        }

        return newRows;
    }

    public Map<Date, List<Mitarbeiter>> sortMap(Map<Date, List<Mitarbeiter>> schichtplan){

        LinkedHashMap<Date, List<Mitarbeiter>> sortedSchichtplan = schichtplan.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return sortedSchichtplan;
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
                mitarbeiterList.add(new Mitarbeiter(rowValues.get(0), rowValues.get(1), 0,
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

    public static Map<Date, List<Mitarbeiter>> erstelleSchichtplan(List<Mitarbeiter> mitarbeiterListe) {
        Map<Date, List<Mitarbeiter>> schichtplan = new HashMap<>();
        Map<Mitarbeiter, Integer> nachtschichtZaehler = new HashMap<>();
        List<String> calendardays = new ArrayList<>();
        for (Mitarbeiter mitarbeiter : mitarbeiterListe) {
            nachtschichtZaehler.put(mitarbeiter, 0);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int monat = calendar.get(Calendar.MONTH);
        while (calendar.get(Calendar.MONTH) == monat) {
            Date datum = calendar.getTime();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            // Reset assigned hours for each worker at the beginning of the week
            if (dayOfWeek == Calendar.MONDAY) {
                for (Mitarbeiter mitarbeiter : mitarbeiterListe) {
                    mitarbeiter.resetAssignedHours();
                }
            }

            if (istWerktag(datum)) {
                // Update the methods to assign workers based on their available hours
                List<Mitarbeiter> nachtdienstMitarbeiter = wähleNachtdienstMitarbeiter(mitarbeiterListe, datum, schichtplan);
                List<Mitarbeiter> workersForDate = new ArrayList<>();
                workersForDate.addAll(nachtdienstMitarbeiter);
                schichtplan.put(datum, nachtdienstMitarbeiter);
                List<Mitarbeiter> tagdienstMitarbeiter = new ArrayList<>();;
                tagdienstMitarbeiter = wähleTagdienstMitarbeiter(mitarbeiterListe, datum, schichtplan);

                // Update the assigned hours for each worker
                for (Mitarbeiter mitarbeiter : nachtdienstMitarbeiter) {
                    mitarbeiter.addAssignedHours(8); // Assuming an 8-hour night shift
                    System.out.println(mitarbeiter.getAssignedHours());
                }
                for (Mitarbeiter mitarbeiter : tagdienstMitarbeiter) {
                    mitarbeiter.addAssignedHours(8); // Assuming an 8-hour day shift
                    System.out.println(mitarbeiter.getAssignedHours());
                }

                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(datum);

                int tag = calendar2.get(Calendar.DAY_OF_MONTH) - 1;

                workersForDate.addAll(tagdienstMitarbeiter);
                for (Mitarbeiter mitarbeiter : workersForDate) {
                    if(nachtdienstMitarbeiter.contains(mitarbeiter)){
                        calendardays=mitarbeiter.getCalenderDays();
                        calendardays.remove(tag);
                        calendardays.add(tag, "ND");
                        mitarbeiter.setCalenderDays(calendardays);
                    }
                }
                schichtplan.put(datum, workersForDate);
            }
            else {
                // Update the methods to assign workers based on their available hours
                List<Mitarbeiter> nachtdienstMitarbeiter = wähleNachtdienstMitarbeiter2(mitarbeiterListe, datum, schichtplan);

                List<Mitarbeiter> workersForDate = new ArrayList<>();
                workersForDate.addAll(nachtdienstMitarbeiter);
                schichtplan.put(datum, nachtdienstMitarbeiter);
                List<Mitarbeiter> tagdienstMitarbeiter = new ArrayList<>();;

                tagdienstMitarbeiter = wähleTagdienstMitarbeiterFreitag(mitarbeiterListe, datum, schichtplan);

                // Update the assigned hours for each worker
                for (Mitarbeiter mitarbeiter : nachtdienstMitarbeiter) {
                    mitarbeiter.addAssignedHours(8); // Assuming an 8-hour night shift
                    System.out.println(mitarbeiter.getAssignedHours());
                }
                for (Mitarbeiter mitarbeiter : tagdienstMitarbeiter) {
                    mitarbeiter.addAssignedHours(8); // Assuming an 8-hour day shift
                    System.out.println(mitarbeiter.getAssignedHours());
                }

                workersForDate.addAll(tagdienstMitarbeiter);
                schichtplan.put(datum, workersForDate);
            }

            calendar.add(Calendar.DATE, 1);
        }
        printSchichtplan(schichtplan);
        return schichtplan;
    }



    public static boolean isEmployeeAssigned(Date date, Mitarbeiter employee, Map<Date, List<Mitarbeiter>> shiftPlan) {
        System.out.println("Checking employee assignment for: " + employee + " on date: " + date);
        if (shiftPlan.containsKey(date)) {
            List<Mitarbeiter> employeesOnDate = shiftPlan.get(date);
            System.out.println("Employees on date: " + employeesOnDate);
            boolean isAssigned = employeesOnDate.contains(employee);
            System.out.println("Employee is assigned: " + isAssigned);
            return isAssigned;
        }
        System.out.println("Date not found in shiftPlan");
        return false;
    }
    public static List<Mitarbeiter> wähleTagdienstMitarbeiterFreitag(List<Mitarbeiter> mitarbeiterListe, Date datum, Map<Date, List<Mitarbeiter>> schichtplan) {
        List<Mitarbeiter> verfügbareMitarbeiter = filtereVerfügbareMitarbeiter(mitarbeiterListe, datum, schichtplan);

        List<Mitarbeiter> tagdienstMitarbeiter = new ArrayList<>();
        int schwesternZähler = 0;
        int pfaZähler = 0;
        int paZähler = 0;

        for (Mitarbeiter mitarbeiter : verfügbareMitarbeiter) {

                if (isEmployeeAssigned(datum, mitarbeiter, schichtplan) == false) {
                    if (schwesternZähler < 2 && mitarbeiter.getPosition().equals("Schwester")) {
                        tagdienstMitarbeiter.add(mitarbeiter);
                        schwesternZähler++;
                    } else if (pfaZähler < 2 && mitarbeiter.getPosition().equals("PFA")) {
                        tagdienstMitarbeiter.add(mitarbeiter);
                        pfaZähler++;
                    } else if (paZähler < 2 && mitarbeiter.getPosition().equals("PA")) {
                        tagdienstMitarbeiter.add(mitarbeiter);
                        paZähler++;
                    }

                    if (schwesternZähler == 2 && (pfaZähler == 2 || paZähler == 2)) {
                        break;
                    }
                }
        }

        // If we have 2 Schwestern and 2 PFA, we don't need the PA
        if (schwesternZähler == 2 && pfaZähler == 2) {
            tagdienstMitarbeiter.removeIf(mitarbeiter -> mitarbeiter.getPosition().equals("PA"));
        }
        // If we have 2 Schwestern and 2 PA, we don't need the PFA
        else if (schwesternZähler == 2 && paZähler == 2) {
            tagdienstMitarbeiter.removeIf(mitarbeiter -> mitarbeiter.getPosition().equals("PFA"));
        }

        return tagdienstMitarbeiter;
    }

    private static List<Mitarbeiter> wähleNachtdienstMitarbeiter(List<Mitarbeiter> mitarbeiterListe, Date datum, Map<Date, List<Mitarbeiter>> schichtplan) {
        List<Mitarbeiter> nachtdienstMitarbeiter = new ArrayList<>();
        int nachtdienstZähler = 0;

        for (Mitarbeiter mitarbeiter : mitarbeiterListe) {
            if (mitarbeiter.getAssignedHours() + 8 <= 40) {
                if (istMitarbeiterVerfügbar(mitarbeiter, datum, schichtplan)) {
                    nachtdienstMitarbeiter.add(mitarbeiter);
                    nachtdienstZähler++;

                    if (nachtdienstZähler == 2) {
                        break;
                    }
                }
            }

        }

        return nachtdienstMitarbeiter;
    }

    private static List<Mitarbeiter> wähleNachtdienstMitarbeiter2(List<Mitarbeiter> mitarbeiterListe, Date datum, Map<Date, List<Mitarbeiter>> schichtplan) {
        List<Mitarbeiter> nachtdienstMitarbeiter = new ArrayList<>();
        int nachtdienstZähler = 0;

        for (Mitarbeiter mitarbeiter : mitarbeiterListe) {
            if (istMitarbeiterVerfügbar(mitarbeiter, datum, schichtplan)) {
                nachtdienstMitarbeiter.add(mitarbeiter);
                nachtdienstZähler++;

                if (nachtdienstZähler == 2) {
                    break;
                }
            }
        }

        return nachtdienstMitarbeiter;
    }
    public static List<Mitarbeiter> wähleTagdienstMitarbeiter(List<Mitarbeiter> mitarbeiterListe, Date datum, Map<Date, List<Mitarbeiter>> schichtplan) {
        List<Mitarbeiter> verfügbareMitarbeiter = filtereVerfügbareMitarbeiter(mitarbeiterListe, datum, schichtplan);
        List<Mitarbeiter> tagdienstMitarbeiter = new ArrayList<>();
        int schwesternZähler = 0;
        int pfaZähler = 0;
        int paZähler = 0;
        int mamaZähler = 0;

        for (Mitarbeiter mitarbeiter : verfügbareMitarbeiter) {
                if (isEmployeeAssigned(datum, mitarbeiter, schichtplan) == false) {

                    if (mitarbeiter.getAssignedHours() + 8 <= 40) {
                        if (schwesternZähler < 2 && mitarbeiter.getPosition().equals("Schwester")) {
                            tagdienstMitarbeiter.add(mitarbeiter);
                            schwesternZähler++;
                        } else if (pfaZähler < 2 && mitarbeiter.getPosition().equals("PFA")) {
                            tagdienstMitarbeiter.add(mitarbeiter);
                            pfaZähler++;
                        } else if (paZähler < 1 && mitarbeiter.getPosition().equals("PA")) {
                            tagdienstMitarbeiter.add(mitarbeiter);
                            paZähler++;
                        } else if (mamaZähler < 1 && mitarbeiter.getPosition().equals("MAMA")) {
                            tagdienstMitarbeiter.add(mitarbeiter);
                            mamaZähler++;
                        }
                    }
                }


                if (schwesternZähler == 2 && (pfaZähler == 2 || (paZähler == 1 && mamaZähler == 1))) {
                    break;
                }
        }

        // If we have 2 Schwestern and 2 PFA, we don't need the PA and MAMA
        if (schwesternZähler == 2 && pfaZähler == 2) {
            tagdienstMitarbeiter.removeIf(mitarbeiter -> mitarbeiter.getPosition().equals("PA") || mitarbeiter.getPosition().equals("MAMA"));
        }
        // If we have 2 Schwestern, 1 PA, and 1 MAMA, we don't need the PFA
        else if (schwesternZähler == 2 && paZähler == 1 && mamaZähler == 1) {
            tagdienstMitarbeiter.removeIf(mitarbeiter -> mitarbeiter.getPosition().equals("PFA"));
        }

        return tagdienstMitarbeiter;
    }



    public static boolean istWerktag(Date datum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datum);
        int tagDerWoche = calendar.get(Calendar.DAY_OF_WEEK);

        return (tagDerWoche >= Calendar.MONDAY && tagDerWoche <= Calendar.FRIDAY);
    }

    public static List<Mitarbeiter> filtereVerfügbareMitarbeiter(List<Mitarbeiter> mitarbeiterListe, Date datum, Map<Date, List<Mitarbeiter>> schichtplan) {
        List<Mitarbeiter> verfügbareMitarbeiter = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datum);

        int tag = calendar.get(Calendar.DAY_OF_MONTH) - 1;

        for (Mitarbeiter mitarbeiter : mitarbeiterListe) {
            if (hatZweiNachtdiensteHintereinander(mitarbeiter, datum, schichtplan)) {
                continue;
            }
            if (mitarbeiter.getCalenderDays().get(tag) != null && mitarbeiter.getCalenderDays().get(tag).equals("U")){
                continue;
            }
            verfügbareMitarbeiter.add(mitarbeiter);
        }

        return verfügbareMitarbeiter;
    }
    public static boolean hatZweiNachtdiensteHintereinander(Mitarbeiter mitarbeiter, Date datum, Map<Date, List<Mitarbeiter>> schichtplan) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datum);

        // Check the previous day
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date previousDay = calendar.getTime();
        boolean previousDayNachtdienst = schichtplan.getOrDefault(previousDay, Collections.emptyList()).contains(mitarbeiter);

        // Check the day before the previous day
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date dayBeforePreviousDay = calendar.getTime();
        boolean dayBeforePreviousDayNachtdienst =schichtplan.getOrDefault(dayBeforePreviousDay, Collections.emptyList()).contains(mitarbeiter);

        return previousDayNachtdienst && dayBeforePreviousDayNachtdienst;
    }

    public static boolean istMitarbeiterVerfügbar(Mitarbeiter mitarbeiter, Date datum, Map<Date, List<Mitarbeiter>> schichtplan) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datum);

        // Überprüfen der vorherigen beiden Tage
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date vorherigerTag1 = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date vorherigerTag2 = calendar.getTime();

        List<Mitarbeiter> vorherigeMitarbeiter1 = schichtplan.get(vorherigerTag1);
        List<Mitarbeiter> vorherigeMitarbeiter2 = schichtplan.get(vorherigerTag2);

        if (vorherigeMitarbeiter1 != null && vorherigeMitarbeiter1.contains(mitarbeiter)) {
            return false;
        }

        if (vorherigeMitarbeiter2 != null && vorherigeMitarbeiter2.contains(mitarbeiter)) {
            return false;
        }

        return true;
    }

    public List<Mitarbeiter> createMitarbeiterList(List<List<String>> rows){
        int count = 0;
        List<Mitarbeiter> mitarbeiterList = new ArrayList<>();

        for (List<String> row : rows) {
            if (count < 2) {
                count++;
                continue;
            }
            mitarbeiterList.add(new Mitarbeiter(row.get(0), row.get(1), 0, row.get(32), row.subList(2, 32)));
        }

        return mitarbeiterList;
    }

    public static void printSchichtplan(Map<Date, List<Mitarbeiter>> shiftPlan) {
        // Sort the shiftPlan map by date (keys)
        LinkedHashMap<Date, List<Mitarbeiter>> sortedShiftPlan = shiftPlan.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        // Iterate through the sorted map and print the schichtplan
        for (Map.Entry<Date, List<Mitarbeiter>> entry : sortedShiftPlan.entrySet()) {
            Date date = entry.getKey();
            List<Mitarbeiter> employees = entry.getValue();
            System.out.println("Date: " + date);
            for (Mitarbeiter mitarbeiter : employees) {
                int remainingHours=mitarbeiter.getRemainingHours();
                System.out.println("  " + mitarbeiter.getFirstname() + " (remaining hours: " + remainingHours + ")");
            }
            System.out.println();
        }
    }
    public static void druckeMitarbeiterProTag(Date datum, List<Mitarbeiter> mitarbeiterListe, SimpleDateFormat dateFormat, Map<Date, List<Mitarbeiter>> schichtplan) {
        System.out.println(dateFormat.format(datum) + ":");

        for (Mitarbeiter mitarbeiter : mitarbeiterListe) {
            if (schichtplan.get(datum).contains(mitarbeiter)) {
                String dienstTyp = istMitarbeiterInNachtdienst(datum, mitarbeiter, schichtplan, mitarbeiterListe) ? "Nachtdienst" : "Tagdienst";
                System.out.println("\t" + mitarbeiter.getFirstname() + " (" + mitarbeiter.getPosition() + ", " + dienstTyp + ")");
            }
        }
    }

    public static boolean istMitarbeiterInNachtdienst(Date datum, Mitarbeiter mitarbeiter, Map<Date, List<Mitarbeiter>> schichtplan, List<Mitarbeiter> mitarbeiterListe) {
        List<Mitarbeiter> nachtdienstMitarbeiter = wähleNachtdienstMitarbeiter(mitarbeiterListe, datum, schichtplan);
        return nachtdienstMitarbeiter.contains(mitarbeiter);
    }
}

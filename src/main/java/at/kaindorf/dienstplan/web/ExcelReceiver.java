package at.kaindorf.dienstplan.web;
import at.kaindorf.dienstplan.bl.Excel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.aspose.cells.CellsFactory;
import com.aspose.cells.Color;
import com.aspose.cells.JsonLayoutOptions;
import com.aspose.cells.JsonUtility;
import com.aspose.cells.License;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;

@RestController
public class ExcelReceiver {

        @PostMapping("/api/excel/post")
        public ResponseEntity<?> handleJsonUpload(@RequestBody List<List<String>> jsonString) {
            Excel excel = new Excel();
            try {
                excel.writeToExcel(jsonString);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok().build();
        }

    @GetMapping("/api/excel/get")
    public ResponseEntity<?> downloadExcel() {

        // Path to your Excel file
        Path path = Paths.get("src/main/resources/Excel/FertigerPlan.xlsx");

        ByteArrayResource resource = null;
        try {
            // Read the bytes of the Excel file
            byte[] bytes = Files.readAllBytes(path);
            resource = new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        // Set the headers for the response
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=FertigerPlan.xlsx");
        // Send the Excel file to the frontend
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(resource);
    }
}

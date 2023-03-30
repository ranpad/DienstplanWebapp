package at.kaindorf.dienstplan.web;
import at.kaindorf.dienstplan.bl.Excel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
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

        @PostMapping("/api/excel")
        public ResponseEntity<?> handleJsonUpload(@RequestBody List<List<String>> jsonString) {
            Excel excel = new Excel();

            try {
                excel.writeToExcel(jsonString);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            return ResponseEntity.ok().build();
        }

}

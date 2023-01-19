package at.kaindorf.dienstplan.web;

import at.kaindorf.dienstplan.bl.Excel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DienstplanWebappApplication {

    private Excel excel = new Excel();
    public static void main(String[] args) {
        SpringApplication.run(DienstplanWebappApplication.class, args);
    }

    @GetMapping("/excel")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
        return excel.returnExcelRows();
    }
}

// @ts-ignore
import * as XLSX from 'xlsx';

function getExcelList() {

    fetch('http://localhost:8080/api/excel/get')
        .then(res => {
            if (!res.ok) {
                alert("GET request failed! HTTP-Status: " + res.status);
                throw Error("fetch failed!");
            }
            console.log(res);
            alert("GET request succeeded!");
            return res.json();
        })
        .then(res => {
            console.log("HIER IST DIE LISTE")
            console.log(res);
        })
        .catch(err => console.log(err));
}


function pushFile(blob: Blob) {
    console.log("working");

    const fileReader = new FileReader();
    fileReader.readAsArrayBuffer(blob);

    fileReader.onload = (event: ProgressEvent<FileReader>) => {
        const data = event.target.result;
        const workbook = XLSX.read(data, { type: 'binary' });

        // Convert the Excel file to a JSON object
        const sheetName = workbook.SheetNames[0];
        const sheet = workbook.Sheets[sheetName];

        const jsonData = XLSX.utils.sheet_to_json(sheet, { header:1 });

        console.log(jsonData);

        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(jsonData)
        }
        fetch('http://localhost:8080/api/excel/post', requestOptions)
            .then(response => {
                alert('File uploaded successfully!');
            })
            .catch(error => {
                alert('File upload failed.');
                console.error(error);
            });
    };
}

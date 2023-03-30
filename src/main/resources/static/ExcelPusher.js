"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var XLSX = require("xlsx");
// Read the Excel file
function pushFile(blob) {
    console.log("working");
    var fileReader = new FileReader();
    fileReader.readAsArrayBuffer(blob);
    fileReader.onload = function (event) {
        var data = event.target.result;
        var workbook = XLSX.read(data, { type: 'binary' });
        // Convert the Excel file to a JSON object
        var sheetName = workbook.SheetNames[0];
        var sheet = workbook.Sheets[sheetName];
        var jsonData = XLSX.utils.sheet_to_json(sheet, { header: 1 });
        console.log(jsonData);
        var requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(jsonData)
        };
        fetch('http://localhost:8080/api/excel', requestOptions)
            .then(function (response) {
            alert('File uploaded successfully!');
        })
            .catch(function (error) {
            alert('File upload failed.');
            console.error(error);
        });
    };
}

"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
// @ts-ignore
var XLSX = require("xlsx");
var getExcelList = function () {
    var url = '/api/excel/get';
    fetch(url)
        .then(function (res) {
        if (!res.ok) {
            document.getElementById('errorMsg').innerHTML = "GET request failed! HTTP-Status: " + res.status;
            throw Error("fetch failed!");
        }
        document.getElementById('errorMsg').innerHTML = "GET request succeeded!";
        return res.json();
    })
        .then(function (res) {
        console.log("HIER IST DIE LISTE");
        console.log(res);
    })
        .catch(function (err) { return console.log(err); });
};
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

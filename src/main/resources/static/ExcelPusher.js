"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
// @ts-ignore
var XLSX = require("xlsx");
function getExcelList() {
    fetch('http://localhost:8080/api/excel/get')
        .then(function (res) {
        if (!res.ok) {
            alert("GET request failed! HTTP-Status: " + res.status);
            throw Error("fetch failed!");
        }
        alert("GET request succeeded!");
        return res.blob();
    })
        .then(function (blob) {
            // Create a temporary URL for the Blob
            var url = URL.createObjectURL(blob);

            // Prompt the user to save the file
            var fileName = "FertigerPlan.xlsx";
            var a = document.createElement("a");
            a.href = url;
            a.download = fileName;

            // Display the prompt
            var event = document.createEvent("MouseEvents");
            event.initEvent("click", true, false);
            a.dispatchEvent(event);

            // Clean up the temporary URL
            setTimeout(function() {
                URL.revokeObjectURL(url);
            }, 1000);
        })
        .catch(function (err) { return console.log(err); });
}
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
        fetch('http://localhost:8080/api/excel/post', requestOptions)
            .then(function (response) {
            alert('File uploaded successfully!');
        })
            .catch(function (error) {
            alert('File upload failed.');
            console.error(error);
        });
    };
}

import * as XLSX from 'xlsx';

// Read the Excel file



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
        fetch('http://localhost:8080/api/excel', requestOptions)
            .then(response => {
                alert('File uploaded successfully!');
            })
            .catch(error => {
                alert('File upload failed.');
                console.error(error);
            });
    };
}
